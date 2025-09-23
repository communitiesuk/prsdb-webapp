import 'dotenv/config'
import Papa from 'papaparse'
import fs from 'fs'

const BASE_URL = process.env.PLAUSIBLE_BASE_URL || 'https://plausible.io'
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

const QUERY = {
    site_id: SITE_ID,
    date_range: '30d',
    metrics: ['visitors', 'pageviews', 'bounce_rate'],
    dimensions: ['visit:country_name', 'visit:city_name'],
    filters: [['is_not', 'visit:country_name', ['']]]
}

export async function queryPlausible(query) {
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

;( async () => {
    try {
        const data = await queryPlausible(QUERY)
        const csv = Papa.unparse(data.results)
        console.log(JSON.stringify(data))
        console.log(csv)
        fs.writeFile('scripts/Plausible/Outputs/Output.csv', csv, (err) => {
            if (err) {
                console.error('Error writing to CSV file:', err);
            } else {
                console.log(`CSV data written to Output.csv`);
            }
        });
    } catch (e) {
        console.error(e.stack || Sring(e))
        process.exit(1)
    }
})()