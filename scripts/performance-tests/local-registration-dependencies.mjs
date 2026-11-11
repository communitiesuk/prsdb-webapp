#!/usr/bin/env node
import assert from 'node:assert/strict';
import { createServer } from 'node:http';
import { readFile, realpath } from 'node:fs/promises';
import { pathToFileURL } from 'node:url';

const token = 'local-registration-epc-token';
const registrationPath = '/landlord/register-property/payment-return';

function requireLoopback(value) {
  const url = new URL(value);
  assert.equal(url.protocol, 'http:');
  assert.ok(['localhost', '127.0.0.1', '[::1]'].includes(url.hostname), 'Only loopback dependencies are permitted');
  assert.equal(url.username + url.password + url.search + url.hash, '');
  return url;
}

export async function loadAllocationExports(pathList, { read = path => readFile(path, 'utf8'), resolvePath = realpath } = {}) {
  assert.ok(typeof pathList === 'string' && pathList.length > 0, 'A bounded read-only registration allocation export is required');
  const paths = pathList.split(',');
  assert.ok(paths.length <= 3 && paths.every(path => path.length > 0), 'At most three bounded allocation exports');
  const resolved = await Promise.all(paths.map(path => resolvePath(path)));
  assert.equal(new Set(resolved).size, resolved.length, 'Allocation exports must be distinct files');
  const allocations = [];
  for (const path of resolved) {
    const rows = JSON.parse(await read(path));
    assert.ok(Array.isArray(rows) && rows.length > 0 && rows.length <= 456, 'Each allocation export must hold 1 to 456 rows');
    allocations.push(...rows);
  }
  for (const key of ['subject', 'uprn', 'propertyOwnershipId']) {
    const values = allocations.map(row => row[key]).filter(value => value !== undefined);
    assert.equal(new Set(values).size, values.length, `Allocation exports overlap on ${key}`);
  }
  return allocations;
}

function rrn(manual, uprn) {
  return `${manual ? '1' : '0'}${String(uprn).padStart(18, '0')}1`.match(/.{4}/g).join('-');
}

export function registrationDependencyServer({ fixtures, appUrl, port = 19091, pendingChecks = 0 }) {
  const app = requireLoopback(appUrl);
  assert.equal(app.pathname, '/');
  assert.ok(Number.isInteger(pendingChecks) && pendingChecks >= 0 && pendingChecks <= 20);
  assert.ok(Array.isArray(fixtures) && fixtures.length > 0 && fixtures.length <= 658, 'At most 456 registration and two 101-row allocation exports');
  const allocations = new Map();
  const subjects = new Set();
  for (const fixture of fixtures) {
    assert.ok(typeof fixture.subject === 'string' && fixture.subject.length > 0 && !subjects.has(fixture.subject));
    assert.ok(Number.isSafeInteger(fixture.uprn) && fixture.uprn > 0 && !allocations.has(String(fixture.uprn)));
    assert.ok(typeof fixture.address === 'string' && fixture.address.length > 0 && typeof fixture.postcode === 'string');
    subjects.add(fixture.subject);
    allocations.set(String(fixture.uprn), fixture);
  }
  const payments = new Map();
  const requests = [];
  const server = createServer(async (request, response) => {
    const url = new URL(request.url, `http://${request.headers.host}`);
    requests.push({ method: request.method, path: url.pathname, query: url.search });
    function json(status, body) {
      response.writeHead(status, { 'Content-Type': 'application/json' });
      response.end(JSON.stringify(body));
    }
    async function body() {
      let value = '';
      for await (const chunk of request) {
        value += chunk;
        assert.ok(value.length <= 65536, 'Dependency request exceeds fixture limit');
      }
      return value;
    }
    function paymentJson(payment) {
      const base = `http://127.0.0.1:${server.address().port}/local/gov-uk-pay`;
      return {
        payment_id: payment.id, amount: payment.amount, reference: payment.reference, description: payment.description,
        return_url: payment.returnUrl, created_date: '2026-11-05T00:00:00Z', delayed_capture: true,
        state: { status: payment.status, finished: ['success', 'cancelled'].includes(payment.status) },
        _links: {
          self: { href: `${base}/v1/payments/${payment.id}`, method: 'GET' },
          next_url: { href: `${base}/mock-card-page/${payment.id}`, method: 'GET' },
          capture: { href: `${base}/v1/payments/${payment.id}/capture`, method: 'POST' },
        },
      };
    }
    try {
      if (request.method === 'GET' && url.pathname === '/health' && !url.search) {
        return json(200, { status: 'UP', allocations: allocations.size, payments: payments.size });
      }
      if (request.method === 'GET' && url.pathname === '/audit' && !url.search) {
        return json(200, [...payments.values()].map(payment => ({ paymentId: payment.id, reference: payment.reference,
          status: payment.status, amount: payment.amount, pendingChecks: payment.pendingServed,
          journeyId: new URL(payment.returnUrl).searchParams.get('journeyId') })));
      }
      if (request.method === 'POST' && url.pathname === '/auth/oauth/token' && !url.search) {
        assert.match(request.headers.authorization ?? '', /^Basic [A-Za-z0-9+/=]+$/);
        assert.equal(new URLSearchParams(await body()).get('grant_type'), 'client_credentials');
        return json(200, { access_token: token, token_type: 'Bearer', expires_in: 3600 });
      }
      if (request.method === 'GET' && url.pathname === '/api/prsdatabase/assessments/search') {
        assert.equal(request.headers.authorization, `Bearer ${token}`);
        assert.equal([...url.searchParams.keys()].length, 1);
        const uprnSearch = url.searchParams.get('uprn');
        const certificate = url.searchParams.get('rrn');
        let fixture, manual = false;
        if (uprnSearch !== null) {
          assert.match(uprnSearch, /^UPRN-\d{12,18}$/);
          fixture = allocations.get(String(Number(uprnSearch.substring(5))));
        } else {
          assert.match(certificate ?? '', /^\d{4}(?:-\d{4}){4}$/);
          const digits = certificate.replaceAll('-', '');
          assert.match(digits, /^[01]\d{18}1$/);
          manual = digits[0] === '1';
          fixture = allocations.get(String(Number(digits.substring(1, 19))));
        }
        if (!fixture) return json(404, { errors: [{ code: 'NOT_FOUND', title: 'No local EPC fixture' }] });
        const parts = fixture.address.split(',').map(value => value.trim());
        const latest = rrn(manual, fixture.uprn, '1');
        return json(200, { data: {
          epcRrn: latest, latestEpcRrnForAddress: latest,
          address: { addressLine1: manual ? `NFT registration ${fixture.uprn}` : parts[0], addressLine2: '',
            town: manual ? 'Performance Town' : parts.at(-2), postcode: fixture.postcode },
          currentEnergyEfficiencyBand: 'C', expiryDate: '2035-01-01',
        } });
      }
      if (request.method === 'POST' && url.pathname === '/local/gov-uk-pay/v1/payments' && !url.search) {
        const data = JSON.parse(await body());
        assert.ok(Number.isSafeInteger(data.amount) && data.amount > 0);
        assert.ok(typeof data.reference === 'string' && data.reference.length > 0);
        assert.equal(data.description, 'Register your rental property');
        assert.equal(data.delayed_capture, true);
        const destination = new URL(data.return_url);
        assert.equal(destination.origin, app.origin);
        assert.equal(destination.pathname, registrationPath);
        assert.equal(destination.searchParams.getAll('journeyId').length, 1);
        assert.equal(destination.searchParams.getAll('paymentReference').length, 1);
        assert.equal(destination.searchParams.get('paymentReference'), data.reference);
        assert.ok([...destination.searchParams.keys()].every(key => ['journeyId', 'paymentReference'].includes(key)));
        assert.equal(destination.hash, '');
        const id = `registration-payment-${payments.size + 1}`;
        const payment = { id, amount: data.amount, reference: data.reference, description: data.description,
          returnUrl: destination.href, status: 'created',
          cardVisited: false, pendingRemaining: pendingChecks, pendingServed: 0 };
        payments.set(id, payment);
        return json(201, paymentJson(payment));
      }
      const card = /^\/local\/gov-uk-pay\/mock-card-page\/(registration-payment-\d+)$/.exec(url.pathname);
      if (request.method === 'GET' && card && !url.search) {
        const payment = payments.get(card[1]);
        assert.ok(payment, 'Unknown payment');
        assert.equal(payment.status, 'created');
        assert.equal(payment.cardVisited, false);
        payment.cardVisited = true;
        payment.status = pendingChecks > 0 ? 'created' : 'capturable';
        response.writeHead(302, { Location: payment.returnUrl });
        return response.end();
      }
      const pay = /^\/local\/gov-uk-pay\/v1\/payments\/(registration-payment-\d+)(?:\/(capture|cancel))?$/.exec(url.pathname);
      if (pay && !url.search) {
        const payment = payments.get(pay[1]);
        assert.ok(payment, 'Unknown payment');
        if (request.method === 'GET' && !pay[2]) {
          if (payment.cardVisited && payment.status === 'created') {
            if (payment.pendingRemaining > 0) {
              payment.pendingRemaining--;
              payment.pendingServed++;
            } else payment.status = 'capturable';
          }
          return json(200, paymentJson(payment));
        }
        if (request.method === 'POST' && pay[2] === 'capture') {
          assert.equal(payment.status, 'capturable');
          payment.status = 'success';
          response.writeHead(204); return response.end();
        }
        if (request.method === 'POST' && pay[2] === 'cancel') {
          assert.ok(!['success', 'cancelled'].includes(payment.status));
          payment.status = 'cancelled';
          response.writeHead(204); return response.end();
        }
      }
      json(404, { error: 'Unmodelled local dependency request' });
    } catch (error) {
      json(400, { error: error.message });
    }
  });
  return { server, requests, payments, listen: () => new Promise(resolve => server.listen(port, '127.0.0.1', resolve)) };
}

async function allocationExportSelfTest() {
  const row = (index, extra = {}) => ({ subject: `synthetic-${index}`, uprn: 900 + index, address: `${index} Fixture Road`, postcode: 'FA1 1AA', ...extra });
  const files = {
    a: [row(1), row(2)], b: [row(3)], c: [row(4)], d: [row(5)], empty: [], large: Array.from({ length: 457 }, (_, index) => row(100 + index)),
    registration: Array.from({ length: 456 }, (_, index) => row(1000 + index)),
    sameSubject: [row(6, { subject: 'synthetic-1' })], sameUprn: [row(7, { uprn: 901 })],
    property: [{ propertyOwnershipId: 41 }], sameProperty: [{ propertyOwnershipId: 41 }], object: { subject: 'synthetic-8' },
  };
  const options = { read: async path => JSON.stringify(files[path]), resolvePath: async path => path.replace(/^alias-/, '') };
  assert.deepEqual((await loadAllocationExports('a,b', options)).map(item => item.subject), ['synthetic-1', 'synthetic-2', 'synthetic-3']);
  assert.equal((await loadAllocationExports('a,b,c', options)).length, 4);
  assert.equal((await loadAllocationExports('registration', options)).length, 456);
  for (const [paths, message] of [['a,b,c,d', /At most three/], ['a,a', /distinct files/], ['a,alias-a', /distinct files/], ['a,', /At most three/],
    ['empty', /1 to 456/], ['large', /1 to 456/], ['object', /1 to 456/], ['a,sameSubject', /overlap on subject/], ['a,sameUprn', /overlap on uprn/],
    ['property,sameProperty', /overlap on propertyOwnershipId/], ['', /allocation export is required/]]) {
    await assert.rejects(loadAllocationExports(paths, options), message, paths);
  }
  const fixtures = await loadAllocationExports('a,b,c', options);
  assert.throws(() => registrationDependencyServer({ fixtures: [...fixtures, ...fixtures], appUrl: 'http://localhost:18080' }));
  console.log('Local registration multi-file allocation export guards passed');
}

async function selfTest() {
  const fixture = { subject: 'synthetic-landlord', uprn: 901, address: '1 Fixture Road, Fixture Town, FA1 1AA', postcode: 'FA1 1AA' };
  const dependency = registrationDependencyServer({ fixtures: [fixture], appUrl: 'http://localhost:18080', port: 0, pendingChecks: 2 });
  await dependency.listen();
  const base = `http://127.0.0.1:${dependency.server.address().port}`;
  const localFetch = (value, options = {}) => {
    const destination = new URL(value);
    assert.ok(destination.origin === base && !destination.username && !destination.password,
      'Requests must target the exact local test server without credentials');
    return fetch(destination, { ...options, redirect: 'manual' });
  };
  try {
    for (const destination of [`http://user@127.0.0.1:${dependency.server.address().port}/health`,
      'https://example.com/health', 'http://127.0.0.1:1/health', 'http://localhost:18080/health']) {
      assert.throws(() => localFetch(destination), /exact local test server/);
    }
    const health = await localFetch(`${base}/health`);
    assert.equal(health.status, 200);
    const oauth = await localFetch(`${base}/auth/oauth/token`, { method: 'POST', headers: { Authorization: 'Basic bG9jYWw6bG9jYWw=' },
      body: new URLSearchParams({ grant_type: 'client_credentials' }) });
    assert.equal((await oauth.json()).access_token, token);
    for (const manual of [true, false]) {
      const response = await localFetch(`${base}/api/prsdatabase/assessments/search?rrn=${rrn(manual, 901)}`, { headers: { Authorization: `Bearer ${token}` } });
      assert.equal(response.status, 200);
      const json = await response.json();
      assert.equal(json.data.address.addressLine1, manual ? 'NFT registration 901' : '1 Fixture Road');
    }
    for (let attempt = 0; attempt < 2; attempt++) {
      const reference = `reference-${attempt}`;
      const response = await localFetch(`${base}/local/gov-uk-pay/v1/payments`, { method: 'POST', body: JSON.stringify({ amount: 6500,
        reference, description: 'Register your rental property', delayed_capture: true,
        return_url: `http://localhost:18080${registrationPath}?journeyId=journey-a&paymentReference=${reference}` }) });
      assert.equal(response.status, 201);
      const payment = await response.json();
      const card = await localFetch(payment._links.next_url.href);
      assert.equal(card.status, 302);
      assert.equal(new URL(card.headers.get('location')).origin, 'http://localhost:18080');
      for (let poll = 0; poll < 2; poll++) {
        assert.equal((await (await localFetch(payment._links.self.href)).json()).state.status, 'created');
      }
      const state = await (await localFetch(payment._links.self.href)).json();
      assert.equal(state.state.status, 'capturable');
      assert.equal((await localFetch(payment._links.capture.href, { method: 'POST' })).status, 204);
    }
    const audit = await (await localFetch(`${base}/audit`)).json();
    assert.deepEqual(audit.map(payment => payment.status), ['success', 'success']);
    assert.ok(audit.every(payment => payment.journeyId === 'journey-a' && payment.amount === 6500));
    assert.equal(new Set(audit.map(payment => payment.paymentId)).size, 2);
    assert.deepEqual(audit.map(payment => payment.pendingChecks), [2, 2]);
    assert.throws(() => registrationDependencyServer({ fixtures: [fixture], appUrl: 'https://example.com' }));
    console.log('Local registration OAuth/EPC/pending/capture dependency contracts passed');
  } finally { await new Promise(resolve => dependency.server.close(resolve)); }
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  if (process.argv.includes('--self-test')) {
    await allocationExportSelfTest();
    await selfTest();
  }
  else {
    const values = Object.fromEntries(process.argv.slice(2).map(argument => {
      const index = argument.indexOf('=');
      assert.ok(index > 2, 'Use --fixture-path=PATH --app-url=http://localhost:18080 --port=19091');
      return [argument.substring(2, index), argument.substring(index + 1)];
    }));
    assert.ok(values['fixture-path'], 'A bounded read-only registration allocation export is required');
    assert.ok(Object.keys(values).every(key => ['fixture-path', 'app-url', 'port', 'pending-checks'].includes(key)));
    const port = Number(values.port ?? 19091);
    assert.ok(Number.isInteger(port) && port > 1024 && port <= 65535);
    const fixtures = await loadAllocationExports(values['fixture-path']);
    const dependency = registrationDependencyServer({ fixtures,
      appUrl: values['app-url'] ?? 'http://localhost:18080', port, pendingChecks: Number(values['pending-checks'] ?? 0) });
    await dependency.listen();
    console.log(`Local registration dependencies listening at http://127.0.0.1:${port}; ${dependency.requests.length} requests`);
    for (const signal of ['SIGINT', 'SIGTERM']) process.on(signal, () => dependency.server.close(() => process.exit(0)));
  }
}
