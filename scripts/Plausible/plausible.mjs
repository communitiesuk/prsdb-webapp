import 'dotenv/config'
import Papa from 'papaparse'
import fs from 'fs'
import path from 'path'

const BASE_URL = process.env.PLAUSIBLE_BASE_URL
const API_KEY = process.env.PLAUSIBLE_API_KEY
const SITE_ID = process.env.PLAUSIBLE_SITE_ID



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

const INPUT_QUERIES_PATH = path.join('Inputs', 'inputQueries.json')
const inputQueries = JSON.parse(fs.readFileSync(INPUT_QUERIES_PATH, 'utf8'))

function replaceSiteId(obj, siteId) {
    if (Array.isArray(obj)) {
        return obj.map(item => replaceSiteId(item, siteId));
    } else if (obj && typeof obj === 'object') {
        const newObj = {};
        for (const key in obj) {
            if (obj[key] === 'SITE_ID') {
                newObj[key] = siteId;
            } else {
                newObj[key] = replaceSiteId(obj[key], siteId);
            }
        }
        return newObj;
    }
    return obj;
}

async function runAllQueries() {
    for (const [queryName, query] of Object.entries(inputQueries)) {
        try {
            const queryWithSiteId = replaceSiteId(query, SITE_ID)
            const data = await queryPlausible(queryWithSiteId)
            const mappedData = mapResultsToNamedFields(data)
            const csv = Papa.unparse(mappedData)
            const outputPath = path.join('Outputs', `${queryName}.csv`)
            fs.writeFileSync(outputPath, csv)
            console.log(`CSV data written to ${outputPath}`)
        } catch (e) {
            console.error(`Error running query '${queryName}':`, e.stack || String(e))
        }
    }
}

runAllQueries()
