import assert from 'node:assert/strict';
import {test} from 'node:test';
import {environment} from '../../../scripts/performance-tests/nft-shared.mjs';
import {cloneForScanWorker} from '../../../scripts/performance-tests/nft-scan-worker.mjs';

function workerTasks() {
  const app = {
    family: 'prsdb-webapp-nft', status: 'ACTIVE',
    containerDefinitions: [{
      name: 'prsdb-webapp',
      image: `448120078528.dkr.ecr.eu-west-2.amazonaws.com/prsdb-webapp:candidate-${'a'.repeat(40)}`,
      environment: [
        {name: 'SPRING_PROFILES_ACTIVE', value: 'default,nft'},
        {name: 'RDS_URL', value: 'jdbc:postgresql://nft-db/database'},
        {name: 'RDS_USERNAME', value: 'nft-user'},
        {name: 'AWS_QUARANTINE_BUCKET', value: 'prsdb-quarantine-nft'},
        {name: 'S3_SAFE_BUCKET_KEY', value: 'prsdb-uploaded-files-nft'},
      ],
      secrets: [{name: 'RDS_PASSWORD', valueFrom: 'nft-password'}],
    }],
  };
  const worker = structuredClone(app);
  Object.assign(worker, {
    family: 'prsdb-async-worker-nft', taskDefinitionArn: 'baseline-worker', revision: 4,
    taskRoleArn: 'arn:aws:iam::448120078528:role/nft-async-worker-ecs-task',
    executionRoleArn: 'arn:aws:iam::448120078528:role/nft-ecs-task-execution',
  });
  worker.containerDefinitions[0].name = 'prsdb-async-worker';
  worker.containerDefinitions[0].environment[0].value = 'web-server-deactivated,email-worker,nft';
  worker.containerDefinitions[0].image = 'older-worker-image';
  return {app, worker};
}

test('scan-only clone preserves baseline and worker roles but uses the exact candidate image', () => {
  const {app, worker} = workerTasks();
  const original = structuredClone(worker);
  const copy = cloneForScanWorker(worker, app, {runId: '123-1', expectedSha: 'a'.repeat(40)});
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
    assert.throws(() => cloneForScanWorker(tasks.worker, tasks.app,
      {runId: '123-1', expectedSha: 'a'.repeat(40)}));
  }
});
