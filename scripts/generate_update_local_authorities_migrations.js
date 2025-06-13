const fs = require('fs');
const csv = require('csv-parser');

const outputDirectory = './output';
const results = [];
const inputFilePath = '../src/main/resources/data/local_authorities/local_authorities.csv';
const outputUpsertMigrationFilePath = `${outputDirectory}/draft_upsert_local_authorities_migration.sql`;
const outputSelectLAsToBeDeletedFilePath = `${outputDirectory}/select_all_local_authorities_to_be_deleted.sql`;
const outputDeleteMigrationFilePath = `${outputDirectory}/draft_delete_local_authorities_migration.sql`;

fs.createReadStream(inputFilePath)
    .pipe(csv())
    .on('data', (data) => results.push(data))
    .on('end', () => {
        if (results.length === 0) {
            console.log('No data found in CSV file.');
            return;
        }

        const insertValues = results
            .filter(row => row['ACCOUNT_TYPE_NAME'].startsWith('English'))
            .map(row => {
            // SQL escape any single quotes
            const custodianCode = row['AUTH_CODE'].replace(/'/g, "''");
            const name = row['ACCOUNT_NAME'].replace(/'/g, "''");
            return `('${custodianCode}', '${name}')`;
        });

        const retainValues = results
            .filter(row => row['ACCOUNT_TYPE_NAME'].startsWith('English'))
            .map(row => {
                // SQL escape any single quotes
                const custodianCode = row['AUTH_CODE'].replace(/'/g, "''");

                return `'${custodianCode}'`;
            });

        const insertStatement = "INSERT INTO local_authority (custodian_code, name)\n"
            + `VALUES ${insertValues.join(',\n       ')}\n` // Indent the resulting file nicely
            + "ON CONFLICT (custodian_code) DO UPDATE SET name = EXCLUDED.name;";

        const selectStatement = `SELECT * FROM local_authority WHERE custodian_code NOT IN (${retainValues.join(', ')});`;

        const deleteStatement = `DELETE FROM local_authority WHERE custodian_code NOT IN (${retainValues.join(', ')});`;

        if (!fs.existsSync(outputDirectory)) {
            fs.mkdirSync(outputDirectory);
        }

        fs.writeFile(outputUpsertMigrationFilePath, insertStatement, (err) => {
            if (err) {
                console.error('Error writing to SQL file:', err);
            } else {
                console.log(`SQL insert statements written to ${outputUpsertMigrationFilePath}`);
            }
        });

        fs.writeFile(outputSelectLAsToBeDeletedFilePath, selectStatement, (err) => {
            if (err) {
                console.error('Error writing to SQL file:', err);
            } else {
                console.log(`SQL select statement written to ${outputSelectLAsToBeDeletedFilePath
                }`);
            }
        });

        fs.writeFile(outputDeleteMigrationFilePath, deleteStatement, (err) => {
            if (err) {
                console.error('Error writing to SQL file:', err);
            } else {
                console.log(`SQL delete statement written to ${outputDeleteMigrationFilePath}`);
            }
        });
    });