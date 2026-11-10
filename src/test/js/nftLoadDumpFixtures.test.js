import assert from 'node:assert/strict';
import test from 'node:test';
import {exportJourneyFixtures, exportRegistrationFixtures} from '../../../scripts/performance-tests/nft-load-dump.mjs';

const environment = {
  FLYWAY_USER: 'readonly-user',
  FLYWAY_PASSWORD: 'test-password',
  FLYWAY_URL: 'jdbc:postgresql://localhost:5432/prsdb',
};

function registrationFixtures(count = 456) {
  return Array.from({length: count}, (_, index) => ({
    subject: `urn:fdc:gov.uk:2022:registration-${index}`,
    address: `${index + 1} Fixture Road, FA1 1AA`,
    postcode: 'FA1 1AA',
    building: String(index + 1),
    localCouncilId: 1,
    uprn: 900000 + index,
  }));
}

test('exports 455 registration allocations plus one spare as a private read-only file', async () => {
  const fixtures = registrationFixtures();
  let savedContent;
  let savedOptions;
  let queryEnvironment;

  const exported = await exportRegistrationFixtures('/tmp/registration-fixtures.json', {
    aws: async () => ({Account: '448120078528'}),
    env: environment,
    executeCommand: async (_command, _args, options) => {
      queryEnvironment = options.env;
      return {stdout: JSON.stringify(fixtures)};
    },
    save: async (_path, content, options) => {
      savedContent = content;
      savedOptions = options;
    },
    log: () => {},
  });

  assert.equal(exported.length, 456);
  assert.equal(JSON.parse(savedContent).length, 456);
  assert.equal(savedOptions.mode, 0o600);
  assert.equal(queryEnvironment.PGOPTIONS, '-c default_transaction_read_only=on');
});

test('rejects insufficient or duplicate registration fixture allocations', async () => {
  for (const fixtures of [
    registrationFixtures(455),
    registrationFixtures().map((fixture, index) => index === 455 ? {...fixture, subject: 'urn:fdc:gov.uk:2022:registration-0'} : fixture),
    registrationFixtures().map((fixture, index) => index === 455 ? {...fixture, uprn: 900000} : fixture),
    registrationFixtures().map((fixture, index) => index === 455 ? {...fixture, address: '1 Fixture Road, FA1 1AA'} : fixture),
  ]) {
    await assert.rejects(
      exportRegistrationFixtures('/tmp/registration-fixtures.json', {
        aws: async () => ({Account: '448120078528'}),
        env: environment,
        executeCommand: async () => ({stdout: JSON.stringify(fixtures)}),
        save: async () => assert.fail('invalid fixture export must not be written'),
        log: () => {},
      }),
    );
  }
});

function familyFixtures(family, count = 28) {
  return Array.from({length: count}, (_, index) => {
    const common = {
      propertyOwnershipId: 10000 + index,
      address: `${index + 1} Journey Road, FA1 1AA`,
    };
    if (family === 'agent') return {ownerSubject: `urn:owner:${index}`, ...common};
    if (family === 'joint') return {ownerSubject: `urn:joint:${index}`, ...common};
    return {
      action: 'deregister-property',
      subject: `urn:offboarding:${index}`,
      ...common,
    };
  });
}

test('exports isolated agent, joint and property deregistration fixture pools for the ramp', async () => {
  for (const family of ['agent', 'joint', 'offboarding']) {
    const fixtures = familyFixtures(family);
    let savedOptions;
    let queryEnvironment;

    const exported = await exportJourneyFixtures(family, `/tmp/${family}-fixtures.json`, {
      aws: async () => ({Account: '448120078528'}),
      env: environment,
      executeCommand: async (_command, _args, options) => {
        queryEnvironment = options.env;
        return {stdout: JSON.stringify(fixtures)};
      },
      save: async (_path, _content, options) => { savedOptions = options; },
      log: () => {},
    });

    assert.equal(exported.length, 28);
    assert.equal(savedOptions.mode, 0o600);
    assert.equal(queryEnvironment.PGOPTIONS, '-c default_transaction_read_only=on');
  }
});

test('rejects undersized or shared fixtures for agent, joint and property deregistration ramps', async () => {
  for (const family of ['agent', 'joint', 'offboarding']) {
    const invalid = [
      [familyFixtures(family, 27), /Journey fixtures need 28/],
      [familyFixtures(family).map((fixture, index) => index === 27
        ? {...fixture, propertyOwnershipId: familyFixtures(family)[0].propertyOwnershipId}
        : fixture), /Journey fixtures contain shared actors or records/],
    ];
    for (const [fixtures, error] of invalid) {
      await assert.rejects(exportJourneyFixtures(family, `/tmp/${family}-fixtures.json`, {
        aws: async () => ({Account: '448120078528'}),
        env: environment,
        executeCommand: async () => ({stdout: JSON.stringify(fixtures)}),
        save: async () => assert.fail('invalid fixture export must not be written'),
        log: () => {},
      }), error);
    }
  }
});
