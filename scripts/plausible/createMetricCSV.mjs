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

async function getValueFromCSV(csvData, pagePath, metric) {
    const row = csvData.find(record => record["event:page"] === pagePath);
    if (!row) return null;
    if (metric === 'pageviews') {
        if (row["pageviews"] !== undefined) return Number(row["pageviews"]);
        if (row["pageviews_sum"] !== undefined) return Number(row["pageviews_sum"]);
    } else if (metric === 'visitors') {
        if (row["visitors"] !== undefined) return Number(row["visitors"]);
        if (row["visitors_sum"] !== undefined) return Number(row["visitors_sum"]);
    }
    return null;
}

export async function createPageViewCSV() {

    const metric = 'pageviews';
    const totalCSV = await readCSV(path.resolve('outputs/pageViews/total_page_views.csv'));
    const total = totalCSV.length > 0 ? (totalCSV[0]["pageviews"]) : '';

    const complianceCSV = await readCSV(path.resolve('processed_journey_data/outputs/pageViews/page_views/landlord_add_compliance_information_pageviews_sum.csv'));
    const addCompliance = await getValueFromCSV(complianceCSV, '/landlord/add-compliance-information', metric);

    const pageViewsCSV = await readCSV(path.resolve('outputs/pageViews/page_views.csv'));
    const llDashboard = await getValueFromCSV(pageViewsCSV, '/landlord/dashboard', metric);
    const llRegStart = await getValueFromCSV(pageViewsCSV, '/landlord/register-as-a-landlord/start', metric);
    const propRegStart = await getValueFromCSV(pageViewsCSV, '/landlord/register-property', metric);
    const lcDashboard = await getValueFromCSV(pageViewsCSV, '/local-council/dashboard', metric);
    const lcUserRegStart = await getValueFromCSV(pageViewsCSV, '/local-council/register-local-council-user/landing-page', metric);
    const propSearch = await getValueFromCSV(pageViewsCSV, '/local-council/search/property', metric);
    const llSearch = await getValueFromCSV(pageViewsCSV, '/local-council/search/landlord', metric);

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

    const csv = Papa.unparse(output);
    await fs.writeFile(path.join(outDir, 'pageViews.csv'), csv);
    console.log(`User experience page views metrics CSV created at ${path.join(outDir, 'pageViews.csv')}`);
}

export async function createCompletionRateCSV() {

    const metric = 'visitors';
    const llRegCSV = await readCSV(path.resolve('processed_journey_data/outputs/visitors/visitors/landlord_register_as_a_landlord.csv'));
    const llRegStart = await getValueFromCSV(llRegCSV, '/landlord/register-as-a-landlord/start', metric);
    const llRegConf = await getValueFromCSV(llRegCSV, '/landlord/register-as-a-landlord/confirmation', metric);
    const llRegRate = (llRegStart === 0 || llRegStart === null) ? null : (llRegConf === 0 || llRegConf === null) ? 0 : ((llRegStart && llRegConf) ? ((llRegConf / llRegStart) * 100).toFixed(2) : null);

    const propRegCSV = await readCSV(path.resolve('processed_journey_data/outputs/visitors/visitors/landlord_register_property.csv'));
    const propRegStart = await getValueFromCSV(propRegCSV, '/landlord/register-property', metric);
    const propRegConf = await getValueFromCSV(propRegCSV, '/landlord/register-property/confirmation', metric);
    const propRegRate = (propRegStart === 0 || propRegStart === null) ? null : (propRegConf === 0 || propRegConf === null) ? 0 : ((propRegStart && propRegConf) ? ((propRegConf / propRegStart) * 100).toFixed(2) : null);

    const complianceCSV = await readCSV(path.resolve('processed_journey_data/outputs/visitors/visitors/landlord_add_compliance_information_visitors_sum.csv'));
    const complianceStart = await getValueFromCSV(complianceCSV, '/landlord/add-compliance-information', metric);
    const complianceConf = await getValueFromCSV(complianceCSV, '/landlord/add-compliance-information/confirmation', metric);
    const complianceRate = (complianceStart === 0 || complianceStart === null) ? null : (complianceConf === 0 || complianceConf === null) ? 0 : ((complianceStart && complianceConf) ? ((complianceConf / complianceStart) * 100).toFixed(2) : null);

    const lcUserCSV = await readCSV(path.resolve('processed_journey_data/outputs/visitors/visitors/local_council_register_local_council_user.csv'));
    const lcUserStart = await getValueFromCSV(lcUserCSV, '/local-council/register-local-council-user/landing-page', metric);
    const lcUserConf = await getValueFromCSV(lcUserCSV, '/local-council/register-local-council-user/confirmation', metric);
    const lcUserRate = (lcUserStart === 0 || lcUserStart === null) ? null : (lcUserConf === 0 || lcUserConf === null) ? 0 : ((lcUserStart && lcUserConf) ? ((lcUserConf / lcUserStart) * 100).toFixed(2) : null);

    const output =[
        {
            "LL registration": llRegRate,
            "Property registration": propRegRate,
            "Add compliance": complianceRate,
            "LC user registration": lcUserRate
        }
        ]
    ;

    const outDir = path.resolve('userExperienceMetrics');
    await fs.mkdir(outDir, { recursive: true });

    const csv = Papa.unparse(output);
    await fs.writeFile(path.join(outDir, 'completionRate.csv'), csv);
    console.log(`User experience completion rate metrics CSV created at ${path.join(outDir, 'completionRate.csv')}`);
}
