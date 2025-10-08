import 'dotenv/config'
import Papa from 'papaparse'
import fs from 'fs'
import path from 'path'
import { Command } from 'commander'
import { processJourneyData } from './process_journey_data.mjs';

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
  .parse(process.argv);

const options = program.opts();
const INPUTS_DIR = path.join('inputs')
let OUTPUTS_DIR
if (options.save) {
    const timestamp = new Date().toISOString().replace(/[:.]/g, '-').replace('T', '_').split('Z')[0]
    OUTPUTS_DIR = path.join('saved', timestamp)
} else {
    OUTPUTS_DIR = path.join('outputs')
}

function clearOutputsDir() {
    // Only clear if not saving to 'saved'
    if (!options.save && fs.existsSync('outputs')) {
        for (const entry of fs.readdirSync('outputs')) {
            const entryPath = path.join('outputs', entry)
            if (fs.lstatSync(entryPath).isDirectory()) {
                fs.rmSync(entryPath, { recursive: true, force: true })
            } else {
                fs.unlinkSync(entryPath)
            }
        }
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
                    if (query.dimensions[0] === 'event:page') {
                        await processJourneyData(query.metrics[0], outputSubdir);
                        console.log(`Processed journey data for query '${queryName}'`);
                    }
                }
            } catch (e) {
                console.error(`Error running query '${queryName}' in file '${inputFile}':`, e.stack || String(e))
            }
        }
    }
}

runPlausibleScript()
