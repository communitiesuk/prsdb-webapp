#!/usr/bin/env node
import assert from 'node:assert/strict';
import { createHash } from 'node:crypto';
import { readFile, realpath } from 'node:fs/promises';
import { spawn } from 'node:child_process';
import { resolve, sep } from 'node:path';
import { loadAllocationExports } from './local-registration-dependencies.mjs';

const options = Object.fromEntries(process.argv.slice(2).map(argument => {
  const index = argument.indexOf('=');
  assert.ok(index > 2, 'Use --fixture-path=PATH --certificate-path=PATH --application-jar=PATH (or --classpath-file=PATH) --config-file=PATH --working-directory=PATH');
  return [argument.substring(2, index), argument.substring(index + 1)];
}));
assert.ok(Object.keys(options).every(key => ['fixture-path', 'certificate-path', 'classpath-file', 'application-jar', 'config-file', 'working-directory', 'duration-seconds', 'java'].includes(key)));
for (const name of ['fixture-path', 'certificate-path', 'config-file', 'working-directory']) assert.ok(options[name], `Missing --${name}`);
assert.ok(Boolean(options['classpath-file']) !== Boolean(options['application-jar']), 'Provide exactly one --classpath-file or --application-jar');
const directory = await realpath(options['working-directory']);
const config = await realpath(options['config-file']);
const application = options['application-jar'] ? ['-jar', await realpath(options['application-jar'])] :
  ['-cp', (await readFile(options['classpath-file'], 'utf8')).trim(), 'uk.gov.communities.prsdb.webapp.PrsdbWebappApplicationKt'];
assert.ok(application[1].length > 0 && !application[1].includes('\n'));
const allocations = await loadAllocationExports(options['fixture-path']);
const subjects = allocations.filter(row => row.subject !== undefined).map(row => row.subject);
const propertyIds = allocations.filter(row => row.propertyOwnershipId !== undefined).map(row => row.propertyOwnershipId);
assert.ok(allocations.every(row => row.subject !== undefined || row.propertyOwnershipId !== undefined));
assert.equal(new Set(subjects).size, subjects.length);
assert.ok(subjects.every(subject => typeof subject === 'string' && /^[A-Za-z0-9:._-]+$/.test(subject)));
assert.equal(new Set(propertyIds).size, propertyIds.length);
assert.ok(propertyIds.every(id => Number.isSafeInteger(id) && id > 0));
const expectedBytes = await readFile(options['certificate-path']);
assert.ok(expectedBytes.subarray(0, 5).toString() === '%PDF-', 'Only the explicitly prepared synthetic PDF can be scanned');
const digest = value => createHash('sha256').update(value).digest('hex');
const expectedDigest = digest(expectedBytes);
const seconds = Number(options['duration-seconds'] ?? 300);
assert.ok(Number.isInteger(seconds) && seconds >= 1 && seconds <= 1800);
const deadline = Date.now() + seconds * 1000;
assert.ok(process.env.DOCKER_HOST?.startsWith('unix://'), 'DOCKER_HOST must explicitly identify the local Colima/Docker Unix socket');
const dockerEnvironment = { ...process.env };
let child;
let stopping = false;
for (const signal of ['SIGINT', 'SIGTERM']) process.on(signal, () => { stopping = true; if (child) child.kill('SIGTERM'); });

async function run(command, args, { env = process.env, cwd = directory, timeout = 180000 } = {}) {
  return await new Promise((accept, reject) => {
    child = spawn(command, args, { env, cwd, stdio: ['ignore', 'pipe', 'pipe'] });
    let output = '';
    child.stdout.on('data', data => { output += data; });
    child.stderr.on('data', data => { output += data; });
    const timer = setTimeout(() => child?.kill('SIGTERM'), timeout);
    child.on('error', error => { clearTimeout(timer); child = undefined; reject(error); });
    child.on('exit', code => {
      clearTimeout(timer); child = undefined;
      if (code === 0) accept(output); else reject(new Error(`${command} failed (${code}): ${output.slice(-8000)}`));
    });
  });
}

async function query(sql) {
  return await run('docker', ['exec', '-e', 'PGOPTIONS=-c default_transaction_read_only=on -c statement_timeout=15000',
    'prsdb-gatling-corrected-20261105', 'psql', '-U', 'postgres', '-d', 'prsdblocal', '-At', '-v', 'ON_ERROR_STOP=1', '-c', sql],
  { env: dockerEnvironment, timeout: 20000 });
}

const containerEnvironment = JSON.parse(await run('docker', ['inspect', '--format', '{{json .Config.Env}}',
  'prsdb-gatling-corrected-20261105'], { env: dockerEnvironment }));
const password = containerEnvironment.find(value => value.startsWith('POSTGRES_PASSWORD='))?.substring('POSTGRES_PASSWORD='.length);
assert.ok(password, 'The pinned isolated PostgreSQL container must provide its local password');

const sqlSubjects = subjects.length ? subjects.map(subject => `'${subject}'`).join(',') : 'NULL';
const sqlPropertyIds = propertyIds.length ? propertyIds.join(',') : 'NULL';
const sql = `SELECT coalesce(json_agg(pending), '[]'::json) FROM (
 SELECT DISTINCT f.id, f.object_key AS "objectKey", f.e_tag AS "eTag", f.version_id AS "versionId"
 FROM file_upload f JOIN virus_scan_callback v ON v.file_upload_id=f.id
 WHERE f.status=0 AND f.file_name='performance-certificate.pdf' AND f.extension='pdf'
   AND f.created_date >= current_timestamp - interval '1 day'
    AND ((v.encoded_callback_data::jsonb ->> 'propertyOwnershipId')::bigint IN (${sqlPropertyIds})
      OR v.encoded_callback_data::jsonb ->> 'userId' IN (${sqlSubjects})
     OR EXISTS (
       SELECT 1 FROM property_ownership p JOIN ownership_link ol ON ol.landlordship_id=p.id
       JOIN landlord l ON l.id=ol.landlord_id
       WHERE p.id=(v.encoded_callback_data::jsonb ->> 'propertyOwnershipId')::bigint
         AND l.individual_subject_identifier IN (${sqlSubjects})
     ))
 ORDER BY f.id LIMIT 10
) pending`;

console.log(`Local real scan-processor watching ${subjects.length} allocated actors and ${propertyIds.length} explicit properties for ${seconds}s`);
while (!stopping && Date.now() < deadline) {
  const pending = JSON.parse(await query(sql));
  for (const upload of pending) {
    assert.ok(Number.isSafeInteger(upload.id) && upload.id > 0);
    assert.ok(typeof upload.objectKey === 'string' && /^certificateUpload\.[A-Za-z0-9._/-]+$/.test(upload.objectKey));
    assert.ok(!upload.objectKey.includes('..') && typeof upload.eTag === 'string' && typeof upload.versionId === 'string');
    const cleanKey = upload.objectKey.replaceAll(/[:<>"?*&\\,]/g, '');
    const quarantine = await realpath(resolve(directory, '.local-uploads', cleanKey));
    assert.ok(quarantine.startsWith(resolve(directory, '.local-uploads') + sep));
    assert.equal(digest(await readFile(quarantine)), expectedDigest, 'Refusing to mark unrecognised file bytes safe');
    const environment = { ...process.env, SPRING_DATASOURCE_PASSWORD: password,
      SCAN_RESULT_STATUS: 'NO_THREATS_FOUND', S3_OBJECT_KEY: upload.objectKey,
      S3_OBJECT_ETAG: upload.eTag, S3_OBJECT_VERSION_ID: upload.versionId,
      S3_QUARANTINE_BUCKET_KEY: 'local-registration-quarantine',
    };
    const output = await run(options.java ?? 'java', [...application,
      `--spring.config.additional-location=file:${config}`,
      '--spring.profiles.active=local,web-server-deactivated,scan-processor',
      '--spring.datasource.url=jdbc:postgresql://localhost:55433/prsdblocal',
      '--spring.datasource.username=postgres', '--spring.sql.init.mode=never', '--spring.flyway.enabled=false',
      '--management.cloudwatch.metrics.export.namespace=local-registration', '--management.cloudwatch.metrics.export.enabled=false',
      '--notify.api-key=local-registration-00000000-0000-0000-0000-000000000000-00000000-0000-0000-0000-000000000000',
      '--os.api-key=local-registration', '--os.downloads.base-url=http://127.0.0.1:19092',
      '--spring.jpa.hibernate.ddl-auto=none', '--spring.docker.compose.enabled=false',
      '--aws.s3.quarantineBucket=local-registration-quarantine', '--spring.main.web-application-type=none',
    ], { env: environment });
    assert.ok(output.includes('Virus scan result processed successfully.'), 'The production scan processor did not confirm success');
    const status = (await query(`SELECT status FROM file_upload WHERE id=${upload.id}`)).trim();
    assert.equal(status, '1', 'Actual scanner did not persist SCANNED');
    const safeFile = await realpath(resolve(directory, '.local-uploads', 'safe', cleanKey));
    assert.equal(digest(await readFile(safeFile)), expectedDigest, 'Actual scanner did not copy the synthetic file safely');
    console.log(`Production scan-processor verified upload ${upload.id}: safe file bytes and persisted SCANNED`);
  }
  if (!stopping) await new Promise(accept => setTimeout(accept, 1000));
}
