import 'dotenv/config'
import Papa from 'papaparse'
import fs from 'fs'
import path from 'path'
import { Command } from 'commander'
import { processJourneyData } from './process_journey_data.mjs';
import {createCompletionRateCSV, createPageViewCSV} from './createMetricCSV.mjs';
import { createJourneyCompletionInputs } from './createJourneyCompletionInputs.mjs';
import readline from 'readline';

const API_KEY = process.env.PLAUSIBLE_API_KEY
const BASE_URL = "https://plausible.io/api/v2/query"


if (!API_KEY) {
  console.error('Please provide a Plausible API key in the PLAUSIBLE_API_KEY environment variable.')
  process.exit(1)
}

async function queryPlausible(query) {
    const res = await fetch (BASE_URL, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${API_KEY}`
        },
        body: JSON.stringify(query)
    })
    const text = await res.text()
    if (!res.ok) throw new Error(`Plausible API error ${res.status}: ${text}`)
    try {
        return JSON.parse(text)
    } catch (e) {
        throw new Error(`Failed to parse Plausible API response: ${e.message}`)
    }
}

function mapResultsToNamedFields(data) {
    const metrics = data.query.metrics || [];
    const dimensions = data.query.dimensions || [];
    return data.results.map(row => {
        const obj = {};
        metrics.forEach((metric, i) => {
            obj[metric] = row.metrics[i];
        });
        dimensions.forEach((dimension, i) => {
            obj[dimension] = row.dimensions[i];
        });
        return obj;
    });
}

const program = new Command();
program
  .option('--all', 'Process all input files')
  .option('--input-file <filename>', 'Process a specific input file')
  .option('--clear', 'Clear the outputs directory before running')
  .option('--save', 'Save processed output in the saved directory (not cleared)')
  .option('--force', 'Force run even if it is not Thursday (skip prompt)')
  .option('--from <date>', 'Override every query date range to start at this date (YYYY-MM-DD)')
  .option('--to <date>', 'End date for --from (YYYY-MM-DD); defaults to today')
  .parse(process.argv);

const options = program.opts();

const DATE_RE = /^\d{4}-\d{2}-\d{2}$/;

function isValidIsoDate(s) {
    if (typeof s !== 'string' || !DATE_RE.test(s)) return false;
    const [y, m, d] = s.split('-').map(Number);
    const dt = new Date(Date.UTC(y, m - 1, d));
    return dt.getUTCFullYear() === y && dt.getUTCMonth() === m - 1 && dt.getUTCDate() === d;
}

function todayLocalIso() {
    const d = new Date();
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

function resolveDateRangeOverride() {
    if (!options.from) {
        if (options.to) {
            console.error('--to requires --from to be specified as well.');
            process.exit(1);
        }
        return null;
    }
    if (!isValidIsoDate(options.from)) {
        console.error(`Invalid --from date "${options.from}". Expected a valid calendar date in YYYY-MM-DD format.`);
        process.exit(1);
    }
    const to = options.to ?? todayLocalIso();
    if (!isValidIsoDate(to)) {
        console.error(`Invalid --to date "${options.to}". Expected a valid calendar date in YYYY-MM-DD format.`);
        process.exit(1);
    }
    if (to < options.from) {
        console.error(`--to (${to}) must not be earlier than --from (${options.from}).`);
        process.exit(1);
    }
    return [options.from, to];
}

const DATE_RANGE_OVERRIDE = resolveDateRangeOverride();

if (DATE_RANGE_OVERRIDE) {
    console.log(`Date range overridden for all queries: ${DATE_RANGE_OVERRIDE[0]} to ${DATE_RANGE_OVERRIDE[1]}`)
}
const INPUTS_DIR = path.join('inputs')
let OUTPUTS_DIR
if (options.save) {
    const timestamp = new Date().toISOString().replace(/[:.]/g, '-').replace('T', '_').split('Z')[0]
    OUTPUTS_DIR = path.join('saved', timestamp)
} else {
    OUTPUTS_DIR = path.join('outputs')
}

function clearDir(dirPath) {
    if (fs.existsSync(dirPath)) {
        for (const entry of fs.readdirSync(dirPath)) {
            const entryPath = path.join(dirPath, entry)
            if (fs.lstatSync(entryPath).isDirectory()) {
                fs.rmSync(entryPath, { recursive: true, force: true })
            } else {
                fs.unlinkSync(entryPath)
            }
        }
    }
}

function clearOutputsDir() {
    // Only clear if not saving to 'saved'
    if (!options.save) {
        clearDir('outputs')
        clearDir('processed_journey_data')
        clearDir('userExperienceMetrics')
    }
}

function getInputFiles() {
    if (options.all && options.inputFile) {
        console.error('Please specify only one of --all or --input-file.')
        process.exit(1)
    }
    if (options.all) {
        return fs.readdirSync(INPUTS_DIR).filter(f => f.endsWith('.json'))
    }
    if (options.inputFile) {
        if (!fs.existsSync(path.join(INPUTS_DIR, options.inputFile))) {
            console.error(`Input file ${options.inputFile} does not exist in ${INPUTS_DIR}`)
            process.exit(1)
        }
        return [options.inputFile]
    }
    console.error('Please specify either --all or --input-file <filename>.')
    process.exit(1)
}

async function runPlausibleScript() {
    if (options.clear) {
        clearOutputsDir()
        if (!options.save) {
            console.log('Outputs directory cleared.')
        }
    }
    const inputFiles = getInputFiles()
    for (const inputFile of inputFiles) {
        const inputQueries = JSON.parse(fs.readFileSync(path.join(INPUTS_DIR, inputFile), 'utf8'))
        const outputSubdir = path.join(OUTPUTS_DIR, path.basename(inputFile, '.json'))
        if (!fs.existsSync(outputSubdir)) {
            fs.mkdirSync(outputSubdir, { recursive: true })
        }
        for (const [queryName, query] of Object.entries(inputQueries)) {
            try {
                if (DATE_RANGE_OVERRIDE) {
                    query.date_range = DATE_RANGE_OVERRIDE
                }
                if (!query.include.total_rows) {
                    console.log('Please include total rows in the query. Instructions are in the readme')
                    process.exit(1);
                }
                const data = await queryPlausible(query)
                if (data.meta.total_rows >= 10000) {
                    console.error(`Warning: Query '${queryName}' in file '${inputFile}' returned ${data.meta.total_rows} rows, which exceeds the 10,000 row limit. Consider refining your query.`);
                    process.exit(1);
                }
                const mappedData = mapResultsToNamedFields(data)
                const csv = Papa.unparse(mappedData)
                const outputPath = path.join(outputSubdir, `${queryName}.csv`)
                fs.writeFileSync(outputPath, csv)
                console.log(`CSV data written to ${outputPath}`)
                if (query.dimensions) {
                    if (query.dimensions.includes('event:page')) {

                        await processJourneyData(query.metrics, outputSubdir);
                        console.log(`Processed journey data for query '${queryName}'`);
                    }
                }
            } catch (e) {
                console.error(`Error running query '${queryName}' in file '${inputFile}':`, e.stack || String(e))
            }
        }
    }
    createPageViewCSV();
    createCompletionRateCSV()
    createJourneyCompletionInputs()
}

async function checkThursdayOrPrompt() {
    const today = new Date();
    // getDay() returns 4 for Thursday (0=Sunday, 1=Monday, ...)
    if (today.getDay() !== 4 && !options.force) {
        const rl = readline.createInterface({
            input: process.stdin,
            output: process.stdout
        });

        const YELLOW = '\x1b[33m';
        const RESET = '\x1b[0m';

        await new Promise((resolve) => {
            rl.question(`${YELLOW} It is not thursday! Have you updated your queries for the correct date range? (y/n) ${RESET}`, (answer) => {
                rl.close();
                if (answer.trim().toLowerCase() !== 'y') {
                    console.log('Exiting script.');
                    process.exit(0);
                }
                resolve();
            });
        });
    }
}

(async () => {
    await checkThursdayOrPrompt();
    await runPlausibleScript();
})();
