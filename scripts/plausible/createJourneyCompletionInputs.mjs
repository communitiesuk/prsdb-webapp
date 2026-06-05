import fs from 'fs';
import path from 'path';
import Papa from 'papaparse';

// Transforms the raw Plausible outputs into the two CSVs that
// ../estimate-journey-completion-time.mjs consumes:
//   transitions.csv : from_page,to_page,count   (from the custom "Flow" event, grouped by referrer/currentUrl)
//   dwell.csv       : page,dwell                 (from time_on_page per event:page)
// It also copies the committed journey node lists alongside them and prints ready-to-run commands.

const OUTPUT_DIR = path.resolve('userExperienceMetrics', 'journeyCompletionTime');

// Raw inputs produced by plausible.mjs from inputs/transitions.json and inputs/dwell_times.json.
const TRANSITIONS_CSV = path.resolve('outputs', 'transitions', 'flow_transitions_14d.csv');
const DWELL_CSV = path.resolve('outputs', 'dwell_times', 'dwelling_time_14d.csv');

const NODE_LISTS = [
  {
    name: 'landlord-registration',
    file: path.resolve('..', 'journey-nodes', 'landlord-registration.txt'),
    start: '/landlord/register-as-a-landlord/start',
    end: '/landlord/register-as-a-landlord/confirmation',
  },
  {
    name: 'property-registration',
    file: path.resolve('..', 'journey-nodes', 'property-registration.txt'),
    start: '/landlord/register-property',
    end: '/landlord/register-property/confirmation',
  },
];

const JOURNEY_PREFIX = '/landlord/register';

function readCsv(filePath) {
  if (!fs.existsSync(filePath)) return null;
  return Papa.parse(fs.readFileSync(filePath, 'utf8'), { header: true }).data.filter((row) => Object.keys(row).length > 0);
}

function writeTransitions(rows) {
  const mapped = rows
    .map((row) => ({
      from_page: row['event:props:referrer'],
      to_page: row['event:props:currentUrl'],
      count: row['events'],
    }))
    .filter((row) => row.from_page && row.to_page && row.count != null && row.count !== '');
  fs.writeFileSync(path.join(OUTPUT_DIR, 'transitions.csv'), Papa.unparse(mapped, { columns: ['from_page', 'to_page', 'count'] }));
  return mapped.length;
}

function writeDwell(rows) {
  const mapped = rows
    .filter((row) => row['event:page'] && row['event:page'].startsWith(JOURNEY_PREFIX))
    .map((row) => ({ page: row['event:page'], dwell: row['time_on_page'] }))
    .filter((row) => row.dwell != null && row.dwell !== '');
  fs.writeFileSync(path.join(OUTPUT_DIR, 'dwell.csv'), Papa.unparse(mapped, { columns: ['page', 'dwell'] }));
  return mapped.length;
}

export function createJourneyCompletionInputs() {
  const transitionsRaw = readCsv(TRANSITIONS_CSV);
  const dwellRaw = readCsv(DWELL_CSV);

  if (!transitionsRaw) {
    console.warn(`Skipping journey completion inputs: ${TRANSITIONS_CSV} not found. Run with inputs/transitions.json included.`);
    return;
  }
  if (!dwellRaw) {
    console.warn(`Skipping journey completion inputs: ${DWELL_CSV} not found. Run with inputs/dwell_times.json included.`);
    return;
  }

  fs.mkdirSync(OUTPUT_DIR, { recursive: true });

  const transitionCount = writeTransitions(transitionsRaw);
  const dwellCount = writeDwell(dwellRaw);

  for (const node of NODE_LISTS) {
    if (fs.existsSync(node.file)) {
      fs.copyFileSync(node.file, path.join(OUTPUT_DIR, `${node.name}-nodes.txt`));
    }
  }

  console.log(`Journey completion inputs written to ${OUTPUT_DIR} (${transitionCount} transitions, ${dwellCount} dwell rows).`);
  console.log('Run the estimator with, for example:');
  for (const node of NODE_LISTS) {
    console.log(
      `  node ../estimate-journey-completion-time.mjs ` +
        `--nodes "${path.join(OUTPUT_DIR, `${node.name}-nodes.txt`)}" ` +
        `--transitions "${path.join(OUTPUT_DIR, 'transitions.csv')}" ` +
        `--dwell "${path.join(OUTPUT_DIR, 'dwell.csv')}" ` +
        `--start ${node.start} --end ${node.end}`,
    );
  }
}
