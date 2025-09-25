import 'dotenv/config'
import Papa from 'papaparse'
import fs from 'fs'
import path from 'path'

const API_KEY = process.env.PLAUSIBLE_API_KEY
const SITE_ID = "prod.register-home-to-rent.communities.gov.uk"
const BASE_URL = "https://plausible.io/api/v2/query"


if (!API_KEY) {
  console.error('Please provide a Plausible API key in the PLAUSIBLE_API_KEY environment variable.')
  process.exit(1)
}

if (!SITE_ID) {
  console.error('Please provide a Plausible site ID in the PLAUSIBLE_SITE_ID environment variable.')
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

const INPUTS_DIR = path.join('inputs')
const inputFiles = fs.readdirSync(INPUTS_DIR).filter(f => f.endsWith('.json'))

async function runAllQueries() {
    for (const inputFile of inputFiles) {
        const inputQueries = JSON.parse(fs.readFileSync(path.join(INPUTS_DIR, inputFile), 'utf8'))
        const outputSubdir = path.join('outputs', path.basename(inputFile, '.json'))
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
            } catch (e) {
                console.error(`Error running query '${queryName}' in file '${inputFile}':`, e.stack || String(e))
            }
        }
    }
}

runAllQueries()
