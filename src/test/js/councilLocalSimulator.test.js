import assert from 'node:assert/strict';
import test from 'node:test';
import {buildCouncilConfiguration, requireInteractiveAuthorization} from '../../../scripts/performance-tests/council-local-simulator.mjs';
import {cloneForSimulator} from '../../../scripts/performance-tests/nft-shared.mjs';

const existing = {
  simulatorUrl: 'http://127.0.0.1:19090',
  clientConfiguration: {
    clientId: 'prsdb-performance-local',
    publicKey: 'existing-client-public-key',
    publicKeySource: 'STATIC',
    token_auth_method: 'private_key_jwt',
    idTokenSigningAlgorithm: 'ES256',
    redirectUrls: ['http://localhost:18080/login/oauth2/code/one-login'],
  },
};

test('performance mocks only change the temporary NFT task without needing council or hosted EPC services', () => {
  const task = {family: 'prsdb-webapp-nft', containerDefinitions: [{
    name: 'prsdb-webapp', environment: [{name: 'SPRING_PROFILES_ACTIVE', value: 'default,nft'}],
    secrets: [{name: 'EPC_REGISTER_CLIENT_SECRET', valueFrom: 'real-secret-reference'}],
  }]};
  const container = cloneForSimulator(task, true).containerDefinitions[0];
  const environment = Object.fromEntries(container.environment.map(({name, value}) => [name, value]));
  assert.equal(environment.SPRING_PROFILES_ACTIVE, 'default,nft,one-login-simulator,performance-test');
  assert.ok(!('PERFORMANCE_TEST_EPC_BASE_URL' in environment));
  assert.ok(!('PERFORMANCE_TEST_COUNCIL_SIMULATOR_URL' in environment));
  assert.equal(environment.GOV_UK_PAY_MOCK_BASE_URL, 'https://nft.register-home-to-rent.test.communities.gov.uk/local/gov-uk-pay');
  assert.equal(environment.EPC_REGISTER_CLIENT_SECRET, 'performance-test');
  assert.deepEqual(container.secrets, []);
  assert.equal(task.containerDefinitions[0].environment[0].value, 'default,nft');
  assert.equal(cloneForSimulator(task).containerDefinitions[0].environment[0].value, 'default,nft,one-login-simulator');
  assert.throws(() => cloneForSimulator(task, {epcBaseUrl: 'https://external.example.test'}));
});

test('council bootstrap isolates RS256 internal access from the existing ES256 One Login client', () => {
  const {simulatorConfiguration, springConfiguration} = buildCouncilConfiguration(existing, {
    simulatorUrl: 'http://127.0.0.1:19091',
    appUrl: 'http://localhost:18080',
  });
  assert.equal(existing.clientConfiguration.idTokenSigningAlgorithm, 'ES256');
  assert.equal(simulatorConfiguration.clientConfiguration.idTokenSigningAlgorithm, 'RS256');
  assert.equal(simulatorConfiguration.clientConfiguration.publicKey, 'existing-client-public-key');
  assert.equal(simulatorConfiguration.clientConfiguration.token_auth_method, 'private_key_jwt');
  assert.deepEqual(simulatorConfiguration.clientConfiguration.redirectUrls, [
    'http://localhost:18080/local-council/login/oauth2/code/internal-access',
  ]);
  assert.equal(springConfiguration['spring.security.oauth2.client.registration.internal-access.client-authentication-method'], 'private_key_jwt');
  for (const [key, value] of Object.entries(springConfiguration)) {
    if (key.includes('.provider.') && !key.endsWith('user-name-attribute')) assert.ok(value.startsWith('http://127.0.0.1:19091/'), key);
    assert.ok(!key.includes('one-login'), 'Existing One Login settings must not be changed');
  }
  assert.equal(springConfiguration['spring.security.oauth2.client.registration.internal-access.redirect-uri'],
    'http://localhost:18080/local-council/login/oauth2/code/internal-access');
});

test('council bootstrap refuses non-loopback dependencies and missing existing client public key', () => {
  for (const simulatorUrl of ['https://example.com', 'http://127.0.0.1:19091/extra', 'http://user@localhost:19091']) {
    assert.throws(() => buildCouncilConfiguration(existing, {simulatorUrl, appUrl: 'http://localhost:18080'}));
  }
  assert.throws(() => buildCouncilConfiguration(existing, {
    simulatorUrl: 'http://127.0.0.1:19090', appUrl: 'http://localhost:18080',
  }), /separate/i);
  assert.throws(() => buildCouncilConfiguration(existing, {
    simulatorUrl: 'http://localhost:19090', appUrl: 'http://localhost:18080',
  }), /separate/i);
  assert.throws(() => buildCouncilConfiguration({clientConfiguration: {}}, {
    simulatorUrl: 'http://127.0.0.1:19091', appUrl: 'http://localhost:18080',
  }), /public key/i);
});

test('council bootstrap rejects automatic authorization instead of silently using the default subject', () => {
  assert.throws(() => requireInteractiveAuthorization(302, 'Found. Redirecting'), /INTERACTIVE_MODE=true/);
  assert.throws(() => requireInteractiveAuthorization(200, '<h1>Unexpected page</h1>'), /interactive/i);
  assert.doesNotThrow(() => requireInteractiveAuthorization(200,
    '<form method="post"><input name="sub"><input name="authCode"><input name="state"></form>'));
  assert.doesNotThrow(() => requireInteractiveAuthorization(200,
    "<form method='post'><input name='sub'><input name='authCode'><input name='state'></form>"));
  for (const name of ['sub', 'authCode', 'state']) {
    assert.throws(() => requireInteractiveAuthorization(200,
      '<form method="post"><input name="sub"><input name="authCode"><input name="state"></form>'
        .replace(`name="${name}"`, `name="${name}-unexpected"`)), /interactive/i);
  }
});
