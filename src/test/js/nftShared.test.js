import assert from 'node:assert/strict';
import {test} from 'node:test';
import {appOrigin, cloneForSimulator, councilOrigin, environment} from '../../../scripts/performance-tests/nft-shared.mjs';
import * as shared from '../../../scripts/performance-tests/nft-shared.mjs';

function nftTask() {
  return {
    family: 'prsdb-webapp-nft',
    taskDefinitionArn: 'arn:aws:ecs:eu-west-2:448120078528:task-definition/prsdb-webapp-nft:1',
    containerDefinitions: [{
      name: 'prsdb-webapp',
      environment: [
        {name: 'SPRING_PROFILES_ACTIVE', value: 'default,nft'},
        {name: 'GOV_UK_PAY_MOCK_BASE_URL', value: 'https://old.example.test'},
        {name: 'EPC_REGISTER_CLIENT_ID', value: 'old-id'},
        {name: 'PERFORMANCE_TEST_INTERNAL_ACCESS_PUBLIC_ORIGIN', value: 'https://old.example.test'},
      ],
      secrets: [
        {name: 'EPC_REGISTER_CLIENT_SECRET', valueFrom: 'secret-reference'},
        {name: 'PERFORMANCE_TEST_INTERNAL_ACCESS_PUBLIC_ORIGIN', valueFrom: 'old-origin-reference'},
      ],
    }],
  };
}

test('per-run app clone enables controlled payment, EPC and council mocks without changing NFT baseline', () => {
  const task = nftTask();
  const original = structuredClone(task);

  const cloned = cloneForSimulator(task);
  const container = cloned.containerDefinitions[0];
  const env = environment(container);

  assert.equal(container.environment.find(value => value.name === 'SPRING_PROFILES_ACTIVE').value,
    'default,nft,one-login-simulator,performance-test');
  assert.equal(env.GOV_UK_PAY_MOCK_BASE_URL, `${appOrigin}/local/gov-uk-pay`);
  assert.equal(env.EPC_REGISTER_CLIENT_ID, 'performance-test');
  assert.equal(env.EPC_REGISTER_CLIENT_SECRET, 'performance-test');
  assert.equal(env.PERFORMANCE_TEST_INTERNAL_ACCESS_PUBLIC_ORIGIN, councilOrigin,
    'Council callback origin was not replaced by the approved NFT origin');
  assert.equal(container.secrets.some(value => value.name === 'EPC_REGISTER_CLIENT_SECRET'), false);
  assert.equal(container.secrets.some(value => value.name === 'PERFORMANCE_TEST_INTERNAL_ACCESS_PUBLIC_ORIGIN'), false);
  assert.deepEqual(task, original);
});

test('per-run clone rejects an unexpected baseline profile', () => {
  const task = nftTask();
  task.containerDefinitions[0].environment[0].value = 'default,nft,performance-test';

  assert.throws(() => cloneForSimulator(task), /Unexpected app baseline profile/);
});

function workerTasks() {
  const app = nftTask();
  app.status = 'ACTIVE';
  app.containerDefinitions[0].image = `448120078528.dkr.ecr.eu-west-2.amazonaws.com/prsdb-webapp:candidate-${'a'.repeat(40)}`;
  app.containerDefinitions[0].environment.push(
    {name: 'RDS_URL', value: 'jdbc:postgresql://nft-db/database'},
    {name: 'RDS_USERNAME', value: 'nft-user'},
    {name: 'AWS_QUARANTINE_BUCKET', value: 'prsdb-quarantine-nft'},
    {name: 'S3_SAFE_BUCKET_KEY', value: 'prsdb-uploaded-files-nft'},
  );
  app.containerDefinitions[0].secrets.push({name: 'RDS_PASSWORD', valueFrom: 'nft-password'});
  const worker = structuredClone(app);
  Object.assign(worker, {
    family: 'prsdb-async-worker-nft', taskDefinitionArn: 'baseline-worker', revision: 4,
    taskRoleArn: 'arn:aws:iam::448120078528:role/nft-async-worker-ecs-task',
    executionRoleArn: 'arn:aws:iam::448120078528:role/nft-ecs-task-execution',
  });
  worker.containerDefinitions[0].name = 'prsdb-async-worker';
  worker.containerDefinitions[0].environment[0].value = 'web-server-deactivated,email-worker,nft';
  worker.containerDefinitions[0].environment = worker.containerDefinitions[0].environment.filter(value =>
    ['SPRING_PROFILES_ACTIVE', 'RDS_URL', 'RDS_USERNAME', 'AWS_QUARANTINE_BUCKET', 'S3_SAFE_BUCKET_KEY'].includes(value.name));
  worker.containerDefinitions[0].secrets = [{name: 'RDS_PASSWORD', valueFrom: 'nft-password'}];
  worker.containerDefinitions[0].image = 'older-worker-image';
  return {app, worker};
}

test('scan-only clone preserves baseline and worker roles but uses the exact candidate image', () => {
  const {app, worker} = workerTasks();
  const original = structuredClone(worker);
  const copy = shared.cloneForScanWorker(worker, app, {runId: '123-1', expectedSha: 'a'.repeat(40)});
  assert.equal(copy.family, 'prsdb-async-worker-nft-performance-123-1');
  assert.equal(copy.containerDefinitions[0].image, app.containerDefinitions[0].image);
  assert.equal(environment(copy.containerDefinitions[0]).SPRING_PROFILES_ACTIVE, 'web-server-deactivated,scan-result-worker,nft');
  assert.equal(environment(copy.containerDefinitions[0]).SCAN_RESULT_QUEUE_NAME, 'nft-prsdb-scan-result-queue');
  assert.equal(copy.taskRoleArn, original.taskRoleArn);
  assert.equal(copy.taskDefinitionArn, undefined);
  assert.deepEqual(worker, original);
});

test('scan-only clone fails closed for ambiguous profiles, images, roles and database destinations', () => {
  for (const change of [
    ({worker}) => { worker.containerDefinitions[0].environment[0].value = 'local,email-worker'; },
    ({worker}) => { worker.taskRoleArn = 'unapproved-role'; },
    ({app}) => { app.containerDefinitions[0].image = 'unapproved-image'; },
    ({worker}) => { worker.containerDefinitions[0].command = ['override']; },
    ({worker}) => { worker.containerDefinitions[0].environment.find(value => value.name === 'RDS_URL').value = 'different-db'; },
    ({worker}) => { worker.containerDefinitions[0].environment.push({name: 'SCAN_RESULT_QUEUE_NAME', value: 'production-queue'}); },
  ]) {
    const tasks = workerTasks();
    change(tasks);
    assert.throws(() => shared.cloneForScanWorker(tasks.worker, tasks.app,
      {runId: '123-1', expectedSha: 'a'.repeat(40)}));
  }
});
