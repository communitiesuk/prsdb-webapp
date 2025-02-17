const fs = require('fs');
const csv = require('csv-parser');

const results = [];
const inputFilePath = '../src/main/resources/data/local_authorities/local_authorities.csv';
const outputFilePath = './output/draft_upsert_local_authorities.sql';

fs.createReadStream(inputFilePath)
    .pipe(csv())
    .on('data', (data) => results.push(data))
    .on('end', () => {
        if (results.length === 0) {
            console.log('No data found in CSV file.');
            return;
        }

        const values = results.map(row => {
            // SQL escape any single quotes
            const custodianCode = row['AUTH_CODE'].replace(/'/g, "''");
            const name = row['ACCOUNT_NAME'].replace(/'/g, "''");
            return `('${custodianCode}', '${name}')`;
        });

        const insertStatement = "INSERT INTO local_authority (custodian_code, name)\n"
            + `VALUES ${values.join(',\n       ')}\n` // Indent the resulting file nicely
            + "ON CONFLICT (custodian_code) DO UPDATE SET name = EXCLUDED.name;";

        fs.writeFile(outputFilePath, insertStatement, (err) => {
            if (err) {
                console.error('Error writing to SQL file:', err);
            } else {
                console.log(`SQL insert statements written to ${outputFilePath}`);
            }
        });
    });