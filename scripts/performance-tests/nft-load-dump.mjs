import {execFile} from 'node:child_process';
import {promisify} from 'node:util';
import {mkdtemp, rm, stat, statfs} from 'node:fs/promises';
import {tmpdir} from 'node:os';
import {join, resolve} from 'node:path';
import {pathToFileURL} from 'node:url';
import {account, awsCommand, region, requireCondition} from './nft-shared.mjs';

const execute = promisify(execFile);
const bucket = 'prsdb-seed-data-nft';
const key = 'seed-data.dump';
const postgresImage = 'postgres:16';
const database = 'prsdb';
const registrationLandlordSubject = 'urn:fdc:gov.uk:2022:mGHDySEVfCsvfvc6lVWf6Qt9Dv0ZxPQWKoEzcjnBlUo';

function nonNegativeInteger(env, name, defaultValue) {
  const value = env[name] || String(defaultValue);
  requireCondition(/^\d+$/.test(value), `${name} must be a non-negative integer`);
  const parsed = Number(value);
  requireCondition(Number.isSafeInteger(parsed), `${name} must be a non-negative integer`);
  return parsed;
}

function datasetRequirements(env) {
  return {
    minimumCompleted: nonNegativeInteger(env, 'NFT_SEED_PROPERTIES', 2450000),
    minimumIncomplete: nonNegativeInteger(env, 'NFT_SEED_INCOMPLETE_PROPERTIES', 272223),
    minimumFreeAddresses: nonNegativeInteger(env, 'NFT_LOAD_MINIMUM_FREE_ADDRESSES', 1),
  };
}

async function run(command, args, options = {}) {
  try {
    return await execute(command, args, {
      ...options,
      timeout: options.timeout ?? 3600000,
      maxBuffer: options.maxBuffer ?? 4 * 1024 * 1024,
    });
  } catch (error) {
    throw new Error(`${command} failed (status ${error.code ?? 'unknown'})`);
  }
}

function postgresEnvironment(env = process.env) {
  requireCondition(env.FLYWAY_USER && env.FLYWAY_PASSWORD, 'NFT database credentials are required');
  requireCondition(
    env.FLYWAY_URL === 'jdbc:postgresql://localhost:5432/prsdb',
    'NFT database must be accessed through the local SSM tunnel',
  );
  return {
    PGPASSWORD: env.FLYWAY_PASSWORD,
    PGHOST: '127.0.0.1',
    PGPORT: '5432',
    PGUSER: env.FLYWAY_USER,
    PGDATABASE: database,
  };
}

async function downloadDump({bucket: objectBucket, key: objectKey, versionId, ifMatch, destinationPath}) {
  const args = [
    's3api',
    'get-object',
    '--region',
    region,
    '--no-cli-pager',
    '--bucket',
    objectBucket,
    '--key',
    objectKey,
  ];
  if (versionId) args.push('--version-id', versionId);
  else args.push('--if-match', ifMatch);
  args.push(destinationPath);
  await run('aws', args);
}

function postgresDockerArgs(environment, volumeArgs, postgresArgs) {
  return [
    'run',
    '--rm',
    '--network',
    'host',
    ...Object.entries(environment).flatMap(([name]) => ['--env', name]),
    ...volumeArgs,
    postgresImage,
    ...postgresArgs,
  ];
}

async function inspectArchive(archivePath) {
  await run('docker', postgresDockerArgs({}, ['--volume', `${archivePath}:/seed-data.dump:ro`], [
    'pg_restore',
    '--list',
    '/seed-data.dump',
  ]));
}

async function runPsql(environment, sql) {
  const {stdout} = await run('docker', postgresDockerArgs(environment, [], [
    'psql',
    '--no-psqlrc',
    '--set',
    'ON_ERROR_STOP=1',
    '--tuples-only',
    '--no-align',
    '--command',
    sql,
  ]), {env: {...process.env, ...environment}});
  return stdout.trim();
}

async function restoreDatabase(archivePath, environment) {
  await runPsql(environment, 'DROP SCHEMA public CASCADE; CREATE SCHEMA public AUTHORIZATION CURRENT_USER;');
  await run('docker', postgresDockerArgs(environment, ['--volume', `${archivePath}:/seed-data.dump:ro`], [
    'pg_restore',
    '--clean',
    '--if-exists',
    '--exit-on-error',
    '--no-owner',
    '--no-acl',
    '--dbname',
    database,
    '/seed-data.dump',
  ]), {env: {...process.env, ...environment}});
}

async function migrateDatabase(env = process.env) {
  await run('./gradlew', ['flywayValidate', 'flywayMigrate', '--no-daemon', '--console=plain'], {
    cwd: process.cwd(),
    env,
  });
}

async function verifyDatabase(environment, requirements) {
  const counts = await runPsql(
    environment,
    `
      SELECT
        (SELECT count(*) FROM property_ownership),
        (SELECT count(*) FROM landlord_incomplete_properties),
        (SELECT count(*) FROM landlord WHERE individual_subject_identifier = '${registrationLandlordSubject}'),
        (SELECT count(*) FROM address a
          WHERE a.id BETWEEN 9100000001 AND 9100000005
            AND upper(a.postcode) = 'EG1 2AA'
            AND a.local_council_id IS NOT NULL
            AND a.is_active
            AND a.uprn IS NOT NULL
            AND NOT EXISTS (
              SELECT 1 FROM property_ownership po WHERE po.is_active AND po.address_id = a.id
            )),
        (SELECT count(*) FROM address a
          WHERE a.local_council_id IS NOT NULL
            AND a.is_active
            AND a.uprn IS NOT NULL
            AND a.id NOT BETWEEN 9100000001 AND 9100000005
            AND NOT EXISTS (
              SELECT 1 FROM property_ownership po WHERE po.is_active AND po.address_id = a.id
            ))
    `,
  );
  const [completed, incomplete, fixtureLandlord, lookupAddresses, freeAddresses] = counts.split('|').map(Number);
  requireCondition(
    completed >= requirements.minimumCompleted,
    'Restored dataset has fewer completed registrations than expected',
  );
  requireCondition(
    incomplete >= requirements.minimumIncomplete,
    'Restored dataset has fewer incomplete registrations than expected',
  );
  requireCondition(fixtureLandlord > 0, 'Required Gatling landlord is missing from the restored dataset');
  requireCondition(lookupAddresses >= 5, 'Required registration lookup addresses are missing from the restored dataset');
  requireCondition(
    freeAddresses >= requirements.minimumFreeAddresses,
    'Restored dataset has too few eligible free addresses for registration journeys',
  );
  await runPsql(environment, 'ANALYZE;');
  return {completed, incomplete, lookupAddresses, freeAddresses};
}

export async function loadSeededDataset(config, {
  aws = awsCommand,
  download = downloadDump,
  inspectArchive: inspect = inspectArchive,
  restoreDatabase: restore = restoreDatabase,
  migrateDatabase: migrate = migrateDatabase,
  verifyDatabase: verify = verifyDatabase,
  createTempDir = prefix => mkdtemp(join(tmpdir(), prefix)),
  removeTempDir = directory => rm(directory, {recursive: true, force: true}),
  fileStat = stat,
  fileStatFs = statfs,
  env = process.env,
  log = console.log,
}) {
  requireCondition(config?.target === 'nft', 'Seed dump loading is NFT only');
  requireCondition(config.account === account, 'Unexpected NFT AWS account configuration');
  requireCondition(config.bucket === bucket && config.key === key, 'Unexpected NFT seed dump location');
  const requirements = datasetRequirements(env);
  const environment = postgresEnvironment(env);
  requireCondition((await aws('sts', 'get-caller-identity')).Account === account, 'Wrong NFT AWS account');

  const metadata = await aws('s3api', 'head-object', {Bucket: bucket, Key: key});
  const size = Number(metadata.ContentLength);
  const etag = metadata.ETag;
  const versionId = metadata.VersionId && metadata.VersionId !== 'null' ? metadata.VersionId : undefined;
  requireCondition(Number.isSafeInteger(size) && size > 0 && etag, 'NFT seed dump is empty or missing');

  const temporaryDirectory = await createTempDir('nft-seed-dump-');
  const archivePath = resolve(temporaryDirectory, 'seed-data.dump');
  try {
    const disk = await fileStatFs(temporaryDirectory);
    const availableBytes = disk.bavail * disk.bsize;
    requireCondition(availableBytes >= size, 'Insufficient runner disk space for NFT seed dump');

    await download({
      bucket,
      key,
      versionId,
      ifMatch: versionId ? undefined : etag,
      destinationPath: archivePath,
    });
    const archive = await fileStat(archivePath);
    requireCondition(archive.size === size, 'Downloaded NFT seed dump size does not match S3 metadata');
    await inspect(archivePath);
    log(
      `Validated NFT seed dump: s3://${bucket}/${key}, version ${versionId ?? 'unversioned'}, ETag ${etag}, size ${size} bytes.`,
    );

    await restore(archivePath, environment);
    await migrate(env);
    const counts = await verify(environment, requirements);
    if (counts) {
      log(
        `Restored dataset counts: ${counts.completed} completed, ${counts.incomplete} incomplete, ` +
          `${counts.lookupAddresses} reserved lookup addresses, ${counts.freeAddresses} free eligible addresses.`,
      );
    }

    return {object: {versionId, etag, size}, counts};
  } finally {
    await removeTempDir(temporaryDirectory);
  }
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  try {
    await loadSeededDataset({
      target: process.env.DEPLOYMENT_ENVIRONMENT,
      account: process.env.AWS_ACCOUNT_ID,
      bucket: process.env.NFT_SEED_DUMP_BUCKET,
      key: process.env.NFT_SEED_DUMP_KEY,
    });
    console.log('NFT seed dump restored, migrated, and verified.');
  } catch (error) {
    console.log(`NFT seed dump load details: ${error.message}`);
    console.error('NFT seed dump load failed.');
    process.exitCode = 1;
  }
}
