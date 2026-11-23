import {account, environment, region, registrationCopy, requireCondition} from './nft-shared.mjs';

export const scanQueueName = 'nft-prsdb-scan-result-queue';
export const scanQueueUrl = `https://sqs.${region}.amazonaws.com/${account}/${scanQueueName}`;
export const scanQueueArn = `arn:aws:sqs:${region}:${account}:${scanQueueName}`;

export function cloneForScanWorker(task, appTask, {runId, expectedSha}) {
  requireCondition(/^\d+-\d+$/.test(runId ?? '') && /^[a-f0-9]{40}$/.test(expectedSha ?? ''),
    'Exact candidate worker ownership and revision are required');
  requireCondition(task.family === 'prsdb-async-worker-nft' && task.status === 'ACTIVE' &&
    appTask.family === 'prsdb-webapp-nft' && appTask.status === 'ACTIVE',
  'Unexpected candidate worker task identity');
  requireCondition(task.taskRoleArn === `arn:aws:iam::${account}:role/nft-async-worker-ecs-task` &&
    task.executionRoleArn === `arn:aws:iam::${account}:role/nft-ecs-task-execution`,
  'Unexpected candidate worker roles');
  const worker = task.containerDefinitions?.[0];
  const app = appTask.containerDefinitions?.find(value => value.name === 'prsdb-webapp');
  requireCondition(task.containerDefinitions?.length === 1 && worker?.name === 'prsdb-async-worker' &&
    !worker.command?.length && !worker.entryPoint?.length && !worker.environmentFiles?.length,
  'Unexpected candidate worker container');
  const workerEnv = environment(worker);
  const appEnv = environment(app ?? {});
  const profiles = workerEnv.SPRING_PROFILES_ACTIVE?.split(',').map(value => value.trim()).sort().join(',');
  requireCondition(['email-worker,nft,web-server-deactivated', 'email-worker,nft,scan-result-worker,web-server-deactivated']
    .includes(profiles), 'Unexpected candidate worker profiles');
  requireCondition(app?.image?.startsWith(`${account}.dkr.ecr.${region}.amazonaws.com/`) &&
    app.image.endsWith(`-${expectedSha}`), 'Candidate worker image is not the deployed candidate');
  const names = [...(worker.environment ?? []), ...(worker.secrets ?? [])].map(value => value.name);
  requireCondition(new Set(names).size === names.length &&
    !names.some(name => /^(SPRING_|SCAN_RESULT_|AWS_ENDPOINT_URL)/.test(name) &&
      !['SPRING_PROFILES_ACTIVE', 'SCAN_RESULT_QUEUE_NAME'].includes(name)) &&
    !names.some(name => ['JAVA_TOOL_OPTIONS', 'JDK_JAVA_OPTIONS', 'JAVA_OPTS'].includes(name)) &&
    !(worker.secrets ?? []).some(value => ['SPRING_PROFILES_ACTIVE', 'SCAN_RESULT_QUEUE_NAME'].includes(value.name)) &&
    (!workerEnv.SCAN_RESULT_QUEUE_NAME || workerEnv.SCAN_RESULT_QUEUE_NAME === scanQueueName),
  'Unexpected candidate worker configuration override');
  for (const name of ['RDS_URL', 'RDS_USERNAME', 'AWS_QUARANTINE_BUCKET', 'S3_SAFE_BUCKET_KEY']) {
    requireCondition(appEnv[name] && workerEnv[name] === appEnv[name], 'Candidate worker destination mismatch');
  }
  requireCondition(workerEnv.AWS_QUARANTINE_BUCKET === 'prsdb-quarantine-nft' &&
    workerEnv.S3_SAFE_BUCKET_KEY === 'prsdb-uploaded-files-nft', 'Candidate worker upload destination is not NFT');
  const password = container => container.secrets?.find(value => value.name === 'RDS_PASSWORD')?.valueFrom;
  requireCondition(password(app) && password(worker) === password(app), 'Candidate worker database secret mismatch');
  const copy = registrationCopy(task);
  copy.family = `prsdb-async-worker-nft-performance-${runId}`;
  const container = copy.containerDefinitions[0];
  container.image = app.image;
  container.environment = container.environment.filter(value =>
    !['SPRING_PROFILES_ACTIVE', 'SCAN_RESULT_QUEUE_NAME'].includes(value.name));
  container.environment.push(
    {name: 'SPRING_PROFILES_ACTIVE', value: 'web-server-deactivated,scan-result-worker,nft'},
    {name: 'SCAN_RESULT_QUEUE_NAME', value: scanQueueName},
  );
  return copy;
}
