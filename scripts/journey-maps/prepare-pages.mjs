// Usage: node scripts/journey-maps/prepare-pages.mjs [source] (copies the maps into docs/journey-maps, which GitHub Pages publishes)
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const NO_INDEX = '<meta name="robots" content="noindex">';

const repoRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..", "..");
const source = path.resolve(process.argv[2] ?? path.join(repoRoot, "build", "journey-maps"));
const pagesRoot = path.join(repoRoot, "docs");
const target = path.join(pagesRoot, "journey-maps");

const escapeHtml = (value) =>
    String(value ?? "").replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[c]);

const journeys = fs.existsSync(source)
    ? fs
          .readdirSync(source, { withFileTypes: true })
          .filter((entry) => entry.isDirectory() && fs.existsSync(path.join(source, entry.name, "manifest.json")))
          .map((entry) => ({ name: entry.name, manifest: JSON.parse(fs.readFileSync(path.join(source, entry.name, "manifest.json"), "utf8")) }))
    : [];
if (journeys.length === 0) {
    console.error(`No journey maps found in ${source}. Run ./gradlew generateJourneyMap first.`);
    process.exit(1);
}

// Journeys that are no longer generated disappear from the site
fs.rmSync(target, { recursive: true, force: true });
fs.mkdirSync(target, { recursive: true });

for (const journey of journeys) {
    const destination = path.join(target, journey.name);
    fs.cpSync(path.join(source, journey.name), destination, { recursive: true });
    const viewer = path.join(destination, "index.html");
    if (fs.existsSync(viewer)) {
        const html = fs
            .readFileSync(viewer, "utf8")
            .replace("<head>", `<head>\n    ${NO_INDEX}`)
            // Mermaid stores each arrow's coordinates in Base64, which the detect-secrets hook reports; the viewer does not use them
            .replace(/ data-points="[^"]*"/g, "");
        fs.writeFileSync(viewer, html);
    }
}

// Without this file GitHub Pages runs the site through Jekyll, which renders the Markdown files and skips some paths
fs.writeFileSync(path.join(pagesRoot, ".nojekyll"), "");

const rows = journeys
    .sort((a, b) => a.manifest.journey.title.localeCompare(b.manifest.journey.title))
    .map(({ name, manifest }) => {
        const pages = manifest.nodes.filter((node) => node.kind !== "decision").length;
        const screenshots = manifest.nodes.reduce((total, node) => total + node.variants.length, 0);
        const commit = manifest.journey.gitCommit ? ` (${escapeHtml(manifest.journey.gitCommit)})` : "";
        return (
            `<tr><td><a href="${escapeHtml(name)}/index.html">${escapeHtml(manifest.journey.title)}</a></td>` +
            `<td>${pages}</td><td>${screenshots}</td><td>${manifest.gaps.length}</td><td>${manifest.errors.length}</td>` +
            `<td>${escapeHtml(manifest.journey.generatedAt.slice(0, 10))}${commit}</td></tr>`
        );
    });
fs.writeFileSync(
    path.join(target, "index.html"),
    `<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    ${NO_INDEX}
    <title>Journey maps</title>
    <style>
        body { font-family: Arial, Helvetica, sans-serif; color: #0b0c0c; margin: 0; }
        header { padding: 12px 24px; border-bottom: 4px solid #1d70b8; }
        h1 { font-size: 24px; margin: 0; }
        main { padding: 16px 24px; }
        p { color: #505a5f; }
        table { border-collapse: collapse; font-size: 16px; }
        th, td { border-bottom: 1px solid #b1b4b6; padding: 8px 12px; text-align: left; }
        th:not(:first-child), td:not(:first-child) { text-align: right; }
        a { color: #1d70b8; }
    </style>
</head>
<body>
<header><h1>Journey maps</h1></header>
<main>
    <p>Every page and branch of each journey, walked automatically in the integration test harness. Select a journey to open its map.</p>
    <table>
        <thead><tr><th>Journey</th><th>Pages</th><th>Screenshots</th><th>Gaps</th><th>Errors</th><th>Generated</th></tr></thead>
        <tbody>
${rows.map((row) => `        ${row}`).join("\n")}
        </tbody>
    </table>
</main>
</body>
</html>
`,
);

console.log(`Copied ${journeys.length} journey maps into ${path.relative(repoRoot, target)}.`);
console.log("To publish them, commit the docs folder and push the branch that GitHub Pages deploys from.");
