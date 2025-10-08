import fs from 'fs/promises';
import path from 'path';
import Papa from 'papaparse';

async function readCSV(filePath) {
    try {
        const data = await fs.readFile(filePath, 'utf8');
        return Papa.parse(data, { header: true }).data;
    } catch (e) {
        return [];
    }
}

function getValueFromCSV(csvData, pagePath) {
    // Try to find the row with the matching page path
    const row = csvData.find(r => r["event:page"] === pagePath || r["page"] === pagePath);
    if (!row) return null;
    if (row["pageviews"] !== undefined) return row["pageviews"];
    if (row["Total"] !== undefined) return row["Total"];
    return null;
}

export async function createUserExperienceMetricsCSV() {
    const headings = [
        "Total",
        "LL dashboard",
        "LL registration start page",
        "Property registration start page",
        "Add compliance start page",
        "LC dashboard",
        "LC user registration start",
        "Property search",
        "Landlord search"
    ];

    // Read total page views
    const totalCSV = await readCSV(path.resolve('outputs/pageViews/total_page_views.csv'));
    const total = totalCSV.length > 0 ? (totalCSV[0]["pageviews"] || Object.values(totalCSV[0])[1]) : '';

    // Read add compliance start page
    const complianceCSV = await readCSV(path.resolve('processed_journey_data/outputs/pageViews/page_views/landlord_add_compliance_information_sum.csv'));
    const addCompliance = getValueFromCSV(complianceCSV, '/landlord/add-compliance-information');

    // Read other page views
    const pageViewsCSV = await readCSV(path.resolve('outputs/pageViews/page_views.csv'));
    const llDashboard = getValueFromCSV(pageViewsCSV, '/landlord/dashboard');
    const llRegStart = getValueFromCSV(pageViewsCSV, '/landlord/register-as-a-landlord/start');
    const propRegStart = getValueFromCSV(pageViewsCSV, '/landlord/register-property');
    const lcDashboard = getValueFromCSV(pageViewsCSV, '/local-council/dashboard');
    const lcUserRegStart = getValueFromCSV(pageViewsCSV, '/local-council/register-local-council-user/landing-page');
    const propSearch = getValueFromCSV(pageViewsCSV, '/local-council/search/property');
    const llSearch = getValueFromCSV(pageViewsCSV, '/local-council/search/landlord');

    // Prepare output data
    const output = [
        {
            "Total": total,
            "LL dashboard": llDashboard,
            "LL registration start page": llRegStart,
            "Property registration start page": propRegStart,
            "Add compliance start page": addCompliance,
            "LC dashboard": lcDashboard,
            "LC user registration start": lcUserRegStart,
            "Property search": propSearch,
            "Landlord search": llSearch
        }
    ];

    // Ensure output directory exists
    const outDir = path.resolve('userExperienceMetrics');
    await fs.mkdir(outDir, { recursive: true });

    // Write CSV
    const csv = Papa.unparse(output, { columns: headings });
    await fs.writeFile(path.join(outDir, 'pageViews.csv'), csv);
    console.log(`User experience page views metrics CSV created at ${path.join(outDir, 'pageViews.csv')}`);
}

// Run the function
createUserExperienceMetricsCSV();
