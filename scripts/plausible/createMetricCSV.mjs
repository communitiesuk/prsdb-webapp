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
    const row = csvData.find(r => r["event:page"] === pagePath || r["page"] === pagePath);
    if (!row) return null;
    if (row["pageviews"] !== undefined) return row["pageviews"];
    if (row["Total"] !== undefined) return row["Total"];
    return null;
}

async function getVisitorCount(csvData, pagePath) {
    const row = csvData.find(r => r["event:page"] === pagePath || r["page"] === pagePath);
    if (!row) return null;
    if (row["visitors"] !== undefined) return Number(row["visitors"]);
    if (row["Total"] !== undefined) return Number(row["Total"]);
    const keys = Object.keys(row);
    for (const key of keys) {
        if (key !== "event:page" && key !== "page" && !isNaN(Number(row[key]))) {
            return Number(row[key]);
        }
    }
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

    const totalCSV = await readCSV(path.resolve('outputs/pageViews/total_page_views.csv'));
    const total = totalCSV.length > 0 ? (totalCSV[0]["pageviews"] || Object.values(totalCSV[0])[1]) : '';

    const complianceCSV = await readCSV(path.resolve('processed_journey_data/outputs/pageViews/page_views/landlord_add_compliance_information_sum.csv'));
    const addCompliance = getValueFromCSV(complianceCSV, '/landlord/add-compliance-information');

    const pageViewsCSV = await readCSV(path.resolve('outputs/pageViews/page_views.csv'));
    const llDashboard = getValueFromCSV(pageViewsCSV, '/landlord/dashboard');
    const llRegStart = getValueFromCSV(pageViewsCSV, '/landlord/register-as-a-landlord/start');
    const propRegStart = getValueFromCSV(pageViewsCSV, '/landlord/register-property');
    const lcDashboard = getValueFromCSV(pageViewsCSV, '/local-council/dashboard');
    const lcUserRegStart = getValueFromCSV(pageViewsCSV, '/local-council/register-local-council-user/landing-page');
    const propSearch = getValueFromCSV(pageViewsCSV, '/local-council/search/property');
    const llSearch = getValueFromCSV(pageViewsCSV, '/local-council/search/landlord');

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

    const outDir = path.resolve('userExperienceMetrics');
    await fs.mkdir(outDir, { recursive: true });

    const csv = Papa.unparse(output, { columns: headings });
    await fs.writeFile(path.join(outDir, 'pageViews.csv'), csv);
    console.log(`User experience page views metrics CSV created at ${path.join(outDir, 'pageViews.csv')}`);
}

export async function createCompletionRateCSV() {
    const headings = [
        "LL registration",
        "Property registration",
        "Add compliance",
        "LC user registration"
    ];

    const llRegCSV = await readCSV(path.resolve('processed_journey_data/outputs/visitors/visitors/landlord_register_as_a_landlord.csv'));
    const llRegStart = await getVisitorCount(llRegCSV, '/landlord/register-as-a-landlord/start');
    const llRegConf = await getVisitorCount(llRegCSV, '/landlord/register-as-a-landlord/confirmation');
    const llRegRate = (llRegStart && llRegConf) ? ((llRegConf / llRegStart) * 100).toFixed(2) : null;

    const propRegCSV = await readCSV(path.resolve('processed_journey_data/outputs/visitors/visitors/landlord_register_property.csv'));
    const propRegStart = await getVisitorCount(propRegCSV, '/landlord/register-property');
    const propRegConf = await getVisitorCount(propRegCSV, '/landlord/register-property/confirmation');
    const propRegRate = (propRegStart && propRegConf) ? ((propRegConf / propRegStart) * 100).toFixed(2) : null;

    const complianceCSV = await readCSV(path.resolve('processed_journey_data/outputs/visitors/visitors/landlord_add_compliance_information_sum.csv'));
    const complianceStart = await getVisitorCount(complianceCSV, '/landlord/add-compliance-information');
    const complianceConf = await getVisitorCount(complianceCSV, '/landlord/add-compliance-information/confirmation');
    const complianceRate = (complianceStart && complianceConf) ? ((complianceConf / complianceStart) * 100).toFixed(2) : null;

    const lcUserCSV = await readCSV(path.resolve('processed_journey_data/outputs/visitors/visitors/local_council_register_local_council_user.csv'));
    const lcUserStart = await getVisitorCount(lcUserCSV, '/local-council/register-local-council-user/landing-page');
    const lcUserConf = await getVisitorCount(lcUserCSV, '/local-council/register-local-council-user/confirmation');
    const lcUserRate = (lcUserStart && lcUserConf) ? ((lcUserConf / lcUserStart) * 100).toFixed(2) : null;

    const output = [
        {
            "LL registration": llRegRate,
            "Property registration": propRegRate,
            "Add compliance": complianceRate,
            "LC user registration": lcUserRate
        }
    ];

    const outDir = path.resolve('userExperienceMetrics');
    await fs.mkdir(outDir, { recursive: true });

    const csv = Papa.unparse(output, { columns: headings });
    await fs.writeFile(path.join(outDir, 'completionRate.csv'), csv);
    console.log(`User experience completion rate metrics CSV created at ${path.join(outDir, 'completionRate.csv')}`);
}
