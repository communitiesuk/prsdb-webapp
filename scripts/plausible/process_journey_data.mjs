import fs from 'fs';
import path from 'path';
import Papa from 'papaparse';

export async function processJourneyData(metric, INPUT_DIR) {
  const OUTPUT_BASE_DIR = path.join('outputs', 'processed_journey_data');
  if (!fs.existsSync(OUTPUT_BASE_DIR)) {
    fs.mkdirSync(OUTPUT_BASE_DIR, { recursive: true });
  }

  const categories = [
    { prefix: '/landlord/register-as-a-landlord', file: 'landlord_register_as_a_landlord.csv' },
    { prefix: '/local-council/register-local-council-user', file: 'local_council_register_local_council_user.csv' },
    { prefix: '/landlord/register-property', file: 'landlord_register_property.csv' },
    { prefix: '/landlord/add-compliance-information', file: 'landlord_add_compliance_information.csv' },
  ];

  const metrics = {
      "visitors": "sum",
      "visits": "sum",
      "pageviews": "sum",
      "bounce_rate": "average",
      "visit_duration": "sum",
      "time_on_page": "average"
  }

  fs.readdirSync(INPUT_DIR).filter(f => f.endsWith('.csv')).forEach(inputFile => {
    const inputPath = path.join(INPUT_DIR, inputFile);
    const outputDir = path.join(OUTPUT_BASE_DIR, INPUT_DIR, path.basename(inputFile, '.csv'));
    if (!fs.existsSync(outputDir)) {
      fs.mkdirSync(outputDir, { recursive: true });
    }
    const csvData = fs.readFileSync(inputPath, 'utf8');
    const { data } = Papa.parse(csvData, { header: true });

    categories.forEach(({ prefix, file }) => {
      const filtered = data.filter(row => row['event:page'] && row['event:page'].startsWith(prefix));
      if (filtered.length > 0) {
        filtered.sort((a, b) => (a['event:page'] || '').localeCompare(b['event:page'] || ''));
        const csvOut = Papa.unparse(filtered);
        fs.writeFileSync(path.join(outputDir, file), csvOut);
      }
    });

    const complianceRows = data.filter(row => row['event:page'] && row['event:page'].startsWith('/landlord/add-compliance-information/'));
    const groupMap = {};
    complianceRows.forEach(row => {
      const parts = row['event:page'].split('/');
      const last = parts[parts.length - 1];
      let key;
      if (/^\d+$/.test(last)) {
        key = '/landlord/add-compliance-information';
      } else {
        key = `/landlord/add-compliance-information/${last}`;
      }
      if (!groupMap[key]) groupMap[key] = [];
      groupMap[key].push(Number(row[metric]));
    });
    let results;
    if (metrics[metric] === 'average') {
      results = Object.entries(groupMap).map(([eventPage, values]) => ({
        'event:page': eventPage,
        [`${metric}_average`]: (values.reduce((a, b) => a + b, 0) / values.length).toFixed(2)
      }));
    } else if (metrics[metric] === 'sum') {
      results = Object.entries(groupMap).map(([eventPage, values]) => ({
        'event:page': eventPage,
        [`${metric}_sum`]: values.reduce((a, b) => a + b, 0)
      }));
    } else {
      results = [];
    }
    results.sort((a, b) => a['event:page'].localeCompare(b['event:page']));
    if (results.length > 0) {
      const csvOut = Papa.unparse(results);
      fs.writeFileSync(path.join(outputDir, `landlord_add_compliance_information_${metrics[metric]}.csv`), csvOut);
      console.log(`Wrote landlord_add_compliance_information_${metrics[metric]}.csv to ${outputDir}`);
    }
  });
    console.log(`Dwell times averaging complete. Outputs saved in ${OUTPUT_BASE_DIR}`);
}
