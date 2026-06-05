// Estimate the average journey-completion time from page-transition (referrer) counts and per-page dwell times.
//
// Given, for a fixed time window:
//   - the list of pages that make up the journey (the "allowed nodes"),
//   - referrer events: for each page, which page the user was on immediately before (page-to-page counts), and
//   - dwell times: mean time-on-page for each page,
// this estimates the expected time for a user to get from a start page to an end page, conditioned on the user
// actually reaching the end page (i.e. completing). Users may drop off from any page.
//
// External entries: any transition whose from_page is NOT in the allowed-node list, but whose to_page IS a journey
// node, is treated as an external entry into that to_page (direct traffic, links from outside the journey, a null
// referrer, etc.). Transitions between two non-journey pages are ignored. This makes per-page arrivals, drop-off,
// and entry volumes directly measurable:
//   arrivals(p)   = external entries into p + internal transitions into p
//   P(p -> q)     = count(p -> q) / arrivals(p)
//   drop-off(p)   = the residual probability mass (arrivals not accounted for by transitions to allowed pages)
//
// Method (absorbing Markov chain + Doob h-transform):
//   Pages are states. "Reaching an end page" is an absorbing "complete" state; drop-off is an absorbing "abandon".
//     1. Completion probability:   h(end) = 1, h(abandon) = 0,  h(p) = Sum_q P(p->q) h(q)
//     2. Completion-weighted time: g(end) = 0,                  g(p) = h(p)*dwell(p) + Sum_q P(p->q) g(q)
//     3. Expected time | completes from p:  T*(p) = g(p) / h(p)
//   Loops (including revisits to the start page) are summed automatically by the linear solve.
//
// Resume rate (cross-session journeys): a user who pauses and later resumes appears in aggregate data as an
// abandonment plus a fresh external entry at a mid-journey page. This biases the average completion time downwards.
// External entries to allowed nodes OTHER than the start node(s) are reported as a "resume rate" so the size of
// this bias is visible. If it is small, ignore it; if large, the headline average should be read as a lower bound.
//
// Identifiability note: aggregate referrer counts give a first-order Markov chain. They cannot, on their own,
// distinguish first-visit behaviour from revisit behaviour at a page. For a task-list journey this is usually fine
// (routing is driven by progress, which the transition counts already capture).
//
// Usage:
//   node estimate-journey-completion-time.mjs \
//     --nodes nodes.txt \
//     --transitions transitions.csv \
//     --dwell dwell.csv \
//     --start /journey/start [--start /journey/other-start] \
//     --end /journey/confirmation [--end /journey/other-end]
//
//   node estimate-journey-completion-time.mjs --demo   # run a built-in worked example (also a self-check)
//
// Input files:
//   nodes.txt       : one allowed journey page per line (blank lines and lines starting with # are ignored)
//   transitions.csv : from_page,to_page,count   page-to-page transition counts (from referrer data). Rows whose
//                                                from_page is not in nodes.txt are external entries into to_page.
//   dwell.csv       : page,dwell                 mean time on page, in seconds (end-page dwell is not needed)

import fs from 'node:fs';
import { parseArgs } from 'node:util';

function parseCsv(text) {
  const rows = [];
  let field = '';
  let record = [];
  let inQuotes = false;
  for (let i = 0; i < text.length; i++) {
    const c = text[i];
    if (inQuotes) {
      if (c === '"') {
        if (text[i + 1] === '"') {
          field += '"';
          i++;
        } else {
          inQuotes = false;
        }
      } else {
        field += c;
      }
    } else if (c === '"') {
      inQuotes = true;
    } else if (c === ',') {
      record.push(field);
      field = '';
    } else if (c === '\n' || c === '\r') {
      if (c === '\r' && text[i + 1] === '\n') i++;
      record.push(field);
      rows.push(record);
      field = '';
      record = [];
    } else {
      field += c;
    }
  }
  if (field !== '' || record.length > 0) {
    record.push(field);
    rows.push(record);
  }
  return rows.filter((r) => r.length > 1 || (r.length === 1 && r[0].trim() !== ''));
}

function readCsvObjects(path) {
  const rows = parseCsv(fs.readFileSync(path, 'utf8'));
  if (rows.length === 0) return [];
  const header = rows[0].map((h) => h.trim());
  return rows.slice(1).map((r) => {
    const o = {};
    header.forEach((h, i) => {
      o[h] = (r[i] ?? '').trim();
    });
    return o;
  });
}

// Solve M x = b for x by Gaussian elimination with partial pivoting. M is n-by-n (row-major), b length n.
function solveLinearSystem(M, b) {
  const n = b.length;
  const a = M.map((row, i) => [...row, b[i]]);
  for (let col = 0; col < n; col++) {
    let pivot = col;
    for (let r = col + 1; r < n; r++) {
      if (Math.abs(a[r][col]) > Math.abs(a[pivot][col])) pivot = r;
    }
    if (Math.abs(a[pivot][col]) < 1e-12) {
      throw new Error(`Singular system at column ${col}; check that every page can reach an end page.`);
    }
    [a[col], a[pivot]] = [a[pivot], a[col]];
    for (let r = 0; r < n; r++) {
      if (r === col) continue;
      const factor = a[r][col] / a[col][col];
      if (factor === 0) continue;
      for (let c = col; c <= n; c++) a[r][c] -= factor * a[col][c];
    }
  }
  return a.map((row, i) => row[n] / row[i]);
}

function readNodesFile(path) {
  return fs
    .readFileSync(path, 'utf8')
    .split(/\r?\n/)
    .map((l) => l.trim())
    .filter((l) => l !== '' && !l.startsWith('#'));
}

function buildModel({ nodes, transitions, dwell, starts, ends }) {
  const allowed = new Set(nodes);
  const endSet = new Set(ends);
  const startSet = new Set(starts);
  const dwellByPage = new Map(dwell.map((r) => [r.page, Number(r.dwell)]));

  const edges = transitions.map((r) => ({ from: r.from_page, to: r.to_page, count: Number(r.count) }));

  const warnings = [];
  const internalIn = new Map(); // internal transitions into p (from an allowed page)
  const externalIn = new Map(); // external entries into p (from a page not in the allowed set)
  const externalSources = new Map(); // from_page (not allowed) -> total count, for a sanity echo
  const journeyNodes = new Set([...allowed, ...starts, ...ends]);

  for (const e of edges) {
    if (allowed.has(e.from)) {
      // End pages are absorbing: ignore any outgoing transitions from them so they do not inflate the
      // arrival denominators of downstream pages (which would create artificial drop-off).
      if (endSet.has(e.from)) continue;
      internalIn.set(e.to, (internalIn.get(e.to) ?? 0) + e.count);
    } else if (journeyNodes.has(e.to)) {
      externalIn.set(e.to, (externalIn.get(e.to) ?? 0) + e.count);
      externalSources.set(e.from, (externalSources.get(e.from) ?? 0) + e.count);
    }
  }

  const arrivalsOf = (p) => (externalIn.get(p) ?? 0) + (internalIn.get(p) ?? 0);

  for (const s of starts) {
    if (!journeyNodes.has(s)) warnings.push(`Start page "${s}" is not in the allowed-node list.`);
    if (arrivalsOf(s) === 0) warnings.push(`No arrivals recorded for start page "${s}"; check the transitions data.`);
  }
  for (const en of ends) {
    if (arrivalsOf(en) === 0) warnings.push(`No arrivals recorded for end page "${en}"; no completions will be counted.`);
  }

  // Transient pages = allowed/start pages that are not end pages. End pages are absorbing.
  const transient = [...journeyNodes].filter((p) => !endSet.has(p));
  const indexOf = new Map(transient.map((p, i) => [p, i]));
  const n = transient.length;

  // Probabilities out of each transient page. Any probability mass not going to an allowed page or an end page
  // (i.e. transitions out of the journey, plus the unobserved residual) is implicit drop-off.
  const probsTo = transient.map(() => new Map());
  const completionProbDirect = new Array(n).fill(0);
  for (const e of edges) {
    const i = indexOf.get(e.from);
    if (i === undefined) continue; // edge originates outside the journey (external entry) or leaves an end page
    const a = arrivalsOf(e.from);
    if (a <= 0) continue;
    const p = e.count / a;
    if (endSet.has(e.to)) {
      completionProbDirect[i] += p;
    } else if (allowed.has(e.to)) {
      probsTo[i].set(e.to, (probsTo[i].get(e.to) ?? 0) + p);
    }
    // transitions to a non-allowed, non-end page leave the journey and are treated as drop-off (not added here)
  }

  // Resume rate: external entries to allowed nodes other than the start node(s) are resumes / deep-links.
  let freshEntries = 0;
  let resumeEntries = 0;
  for (const [page, count] of externalIn) {
    if (startSet.has(page)) freshEntries += count;
    else resumeEntries += count;
  }

  return {
    transient,
    indexOf,
    n,
    probsTo,
    completionProbDirect,
    arrivalsOf,
    externalIn,
    dwellByPage,
    endSet,
    startSet,
    warnings,
    freshEntries,
    resumeEntries,
    externalSources,
  };
}

function solve(model) {
  const { transient, indexOf, n, probsTo, completionProbDirect, dwellByPage } = model;

  // (I - Q) h = completionProbDirect
  const M = Array.from({ length: n }, (_, i) => {
    const row = new Array(n).fill(0);
    row[i] = 1;
    for (const [succ, prob] of probsTo[i]) {
      const j = indexOf.get(succ);
      if (j !== undefined) row[j] -= prob;
    }
    return row;
  });
  const h = solveLinearSystem(M, completionProbDirect);

  // (I - Q) g = h .* dwell    (reuse the same M)
  const dwellTerm = transient.map((p, i) => h[i] * (dwellByPage.get(p) ?? 0));
  const g = solveLinearSystem(
    M.map((row) => [...row]),
    dwellTerm,
  );

  return { h, g };
}

function report(model, starts, ends) {
  const { transient, indexOf, externalIn, startSet } = model;
  const { h, g } = solve(model);

  const lines = [];
  if (model.warnings.length) {
    for (const w of model.warnings) lines.push(`WARNING: ${w}`);
    lines.push('');
  }
  lines.push('Per-start estimates:');
  lines.push('  start page                                          completion%   mean time | completes');
  let weightedNum = 0;
  let weightedDen = 0;
  for (const s of starts) {
    const i = indexOf.get(s);
    if (i === undefined) {
      lines.push(`  ${s.padEnd(50)}  (not found / not a transient page)`);
      continue;
    }
    const hs = h[i];
    const tStar = hs > 1e-12 ? g[i] / hs : null;
    const entryWeight = externalIn.get(s) ?? 0; // genuine fresh starts = external entries to the start page
    if (hs > 1e-12) {
      weightedNum += entryWeight * g[i];
      weightedDen += entryWeight * hs;
    }
    const pct = (hs * 100).toFixed(1).padStart(9);
    const t = tStar === null ? '   no path to an end page' : `${tStar.toFixed(1).padStart(12)} s  (~${(tStar / 60).toFixed(2)} min)`;
    lines.push(`  ${s.padEnd(50)}  ${pct}    ${t}`);
  }

  lines.push('');
  if (weightedDen > 1e-12) {
    const overall = weightedNum / weightedDen;
    lines.push(`Overall expected completion time (weighted by fresh entries that complete):`);
    lines.push(`  ${overall.toFixed(1)} s  (~${(overall / 60).toFixed(2)} min)`);
  } else {
    lines.push('Overall expected completion time: undefined (no start page can reach an end page).');
  }

  // Resume-rate diagnostic: how much external traffic enters mid-journey rather than at a start page.
  const { freshEntries, resumeEntries } = model;
  const totalExternal = freshEntries + resumeEntries;
  lines.push('');
  if (totalExternal > 0) {
    const rate = (resumeEntries / totalExternal) * 100;
    lines.push(`Resume rate: ${rate.toFixed(1)}%  (${resumeEntries} of ${totalExternal} external entries arrived mid-journey)`);
    if (rate >= 10) {
      lines.push('  NOTE: a material share of journeys resume from outside the start page. Cross-session journeys are');
      lines.push('  recorded as an abandonment plus a fast restart, so the average above is biased low - treat it as a');
      lines.push('  lower bound on the true average completion time.');
    }
  } else {
    lines.push('Resume rate: n/a (no external entries detected in the transitions data)');
  }

  lines.push('');
  lines.push(`End pages (completion): ${ends.join(', ')}`);
  lines.push(`Transient pages modelled: ${transient.length}`);
  const externalSrc = [...model.externalSources.entries()].sort((a, b) => b[1] - a[1]);
  if (externalSrc.length) {
    lines.push('');
    lines.push('from_page values treated as external entries (check none are journey pages omitted from --nodes):');
    for (const [src, count] of externalSrc.slice(0, 20)) {
      lines.push(`  ${(src === '' ? '(direct / no referrer)' : src).padEnd(50)} ${count}`);
    }
    if (externalSrc.length > 20) lines.push(`  ... and ${externalSrc.length - 20} more`);
  }
  return { text: lines.join('\n'), h, g };
}

function runDemo() {
  // Flow-consistent worked example. Nodes S (start), H (hub), E (end). External entries arrive from a from_page
  // that is NOT in the allowed-node list: 1000 fresh starts into S and 100 resumes straight into H. Internally
  // everyone goes S->H, then from H either completes (H->E, 700) or loops back to the start (H->S, 200); the
  // remaining H traffic drops off. Hand-computed: completion h(S)=0.6250, mean time|completes ~35.36 s,
  // resume rate = 100/1100 = 9.09%.
  const transitions = [
    { from_page: 'external', to_page: 'S', count: '1000' },
    { from_page: 'external', to_page: 'H', count: '100' },
    { from_page: 'S', to_page: 'H', count: '1000' },
    { from_page: 'H', to_page: 'E', count: '700' },
    { from_page: 'H', to_page: 'S', count: '200' },
  ];
  const dwell = [
    { page: 'S', dwell: '10' },
    { page: 'H', dwell: '20' },
  ];
  const nodes = ['S', 'H', 'E'];
  const starts = ['S'];
  const ends = ['E'];
  const model = buildModel({ nodes, transitions, dwell, starts, ends });
  const { text, h, g } = report(model, starts, ends);
  console.log(text);

  const i = model.indexOf.get('S');
  const tStar = g[i] / h[i];
  const resumeRate = model.resumeEntries / (model.freshEntries + model.resumeEntries);
  const okH = Math.abs(h[i] - 0.625) < 1e-3;
  const okT = Math.abs(tStar - 35.36) < 0.1;
  const okR = Math.abs(resumeRate - 100 / 1100) < 1e-3;
  console.log('');
  console.log(`Self-check: completion=${h[i].toFixed(4)} (expect 0.6250), time=${tStar.toFixed(2)}s (expect ~35.36), resume=${(resumeRate * 100).toFixed(1)}% (expect 9.1%) -> ${okH && okT && okR ? 'PASS' : 'FAIL'}`);
  if (!(okH && okT && okR)) process.exitCode = 1;
}

function main() {
  const { values } = parseArgs({
    options: {
      nodes: { type: 'string' },
      transitions: { type: 'string' },
      dwell: { type: 'string' },
      start: { type: 'string', multiple: true },
      end: { type: 'string', multiple: true },
      demo: { type: 'boolean', default: false },
      help: { type: 'boolean', short: 'h', default: false },
    },
  });

  if (values.help) {
    console.log(fs.readFileSync(new URL(import.meta.url), 'utf8').split('\n').filter((l) => l.startsWith('//')).map((l) => l.replace(/^\/\/ ?/, '')).join('\n'));
    return;
  }

  if (values.demo) {
    runDemo();
    return;
  }

  const missing = ['nodes', 'transitions', 'dwell'].filter((k) => !values[k]);
  if (missing.length || !values.start?.length || !values.end?.length) {
    console.error('Missing required arguments. Need --nodes, --transitions, --dwell, at least one --start and one --end.');
    console.error('Run with --help for usage, or --demo for a worked example.');
    process.exitCode = 1;
    return;
  }

  const nodes = readNodesFile(values.nodes);
  const transitions = readCsvObjects(values.transitions);
  const dwell = readCsvObjects(values.dwell);
  const model = buildModel({ nodes, transitions, dwell, starts: values.start, ends: values.end });
  const { text } = report(model, values.start, values.end);
  console.log(text);
}

main();
