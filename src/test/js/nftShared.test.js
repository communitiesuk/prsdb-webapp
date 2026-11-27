import assert from 'node:assert/strict';
import {test} from 'node:test';
import {appOrigin, cloneForSimulator, councilOrigin, environment} from '../../../scripts/performance-tests/nft-shared.mjs';

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
