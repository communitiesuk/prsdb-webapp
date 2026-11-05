import {writeFile} from 'node:fs/promises';
import {resolve} from 'node:path';
import {pathToFileURL} from 'node:url';

function loopbackOrigin(value) {
  const url = new URL(value);
  if (url.protocol !== 'http:' || !['localhost', '127.0.0.1', '[::1]'].includes(url.hostname) ||
      url.username || url.password || url.pathname !== '/' || url.search || url.hash) {
    throw new Error('Council dependencies must be HTTP loopback origins without credentials or paths');
  }
  return url.origin;
}

export function buildCouncilConfiguration(existing, {simulatorUrl, appUrl}) {
  simulatorUrl = loopbackOrigin(simulatorUrl);
  appUrl = loopbackOrigin(appUrl);
  if (existing.simulatorUrl &&
      new URL(simulatorUrl).port === new URL(loopbackOrigin(existing.simulatorUrl)).port) {
    throw new Error('Council RS256 requires a separate simulator instance; do not overwrite the One Login ES256 simulator');
  }
  const client = existing.clientConfiguration;
  if (!client?.publicKey || !client?.clientId || client.publicKeySource !== 'STATIC' ||
      client.token_auth_method !== 'private_key_jwt') {
    throw new Error('The existing simulator must have a STATIC public key and private_key_jwt client configured');
  }
  const callback = `${appUrl}/local-council/login/oauth2/code/internal-access`;
  const clientId = `${client.clientId}-council`;
  return {
    simulatorConfiguration: {
      simulatorUrl,
      clientConfiguration: {
        ...client,
        clientId,
        scopes: ['openid'],
        redirectUrls: [callback],
        postLogoutRedirectUrls: [`${appUrl}/signout`],
        claims: [],
        identityVerificationSupported: false,
        idTokenSigningAlgorithm: 'RS256',
        clientLoCs: ['P0'],
      },
    },
    springConfiguration: {
      'spring.security.oauth2.client.registration.internal-access.client-id': clientId,
      'spring.security.oauth2.client.registration.internal-access.client-authentication-method': 'private_key_jwt',
      'spring.security.oauth2.client.registration.internal-access.authorization-grant-type': 'authorization_code',
      'spring.security.oauth2.client.registration.internal-access.scope': 'openid',
      'spring.security.oauth2.client.registration.internal-access.redirect-uri': callback,
      'spring.security.oauth2.client.provider.internal-access.issuer-uri': `${simulatorUrl}/`,
      'spring.security.oauth2.client.provider.internal-access.authorization-uri': `${simulatorUrl}/authorize`,
      'spring.security.oauth2.client.provider.internal-access.token-uri': `${simulatorUrl}/token`,
      'spring.security.oauth2.client.provider.internal-access.jwk-set-uri': `${simulatorUrl}/.well-known/jwks.json`,
      'spring.security.oauth2.client.provider.internal-access.user-info-uri': `${simulatorUrl}/userinfo`,
      'spring.security.oauth2.client.provider.internal-access.user-name-attribute': 'sub',
    },
  };
}

export function requireInteractiveAuthorization(status, body) {
  if (status !== 200 || !/<form\b/i.test(body) ||
      ![/name=["']sub["']/, /name=["']authCode["']/, /name=["']state["']/].every(pattern => pattern.test(body))) {
    throw new Error('Council simulator must expose interactive subject selection; start the container with -e INTERACTIVE_MODE=true');
  }
}

async function configure() {
  const source = loopbackOrigin(process.env.ONE_LOGIN_SIMULATOR_URL ?? 'http://127.0.0.1:19090');
  const simulatorUrl = process.env.COUNCIL_SIMULATOR_URL ?? 'http://127.0.0.1:19091';
  const output = process.env.COUNCIL_SPRING_CONFIG_PATH;
  if (!output) throw new Error('COUNCIL_SPRING_CONFIG_PATH must name a new session-artifact JSON file');
  const response = await fetch(`${source}/config`, {signal: AbortSignal.timeout(10000), redirect: 'error'});
  if (!response.ok) throw new Error(`Existing simulator configuration: HTTP ${response.status}`);
  const configuration = buildCouncilConfiguration(await response.json(), {
    simulatorUrl, appUrl: process.env.COUNCIL_APP_URL ?? 'http://localhost:18080',
  });
  const update = await fetch(`${loopbackOrigin(simulatorUrl)}/config`, {
    method: 'POST', headers: {'Content-Type': 'application/json'}, redirect: 'error',
    signal: AbortSignal.timeout(10000), body: JSON.stringify(configuration.simulatorConfiguration),
  });
  if (!update.ok) throw new Error(`Council simulator configuration: HTTP ${update.status}: ${await update.text()}`);
  const discovery = await fetch(`${loopbackOrigin(simulatorUrl)}/.well-known/openid-configuration`, {
    redirect: 'error', signal: AbortSignal.timeout(10000),
  });
  if (!discovery.ok) throw new Error(`Council simulator discovery: HTTP ${discovery.status}`);
  const metadata = await discovery.json();
  if (metadata.issuer !== `${loopbackOrigin(simulatorUrl)}/`) throw new Error('Unexpected council simulator issuer');
  const authorization = new URL(`${loopbackOrigin(simulatorUrl)}/authorize`);
  authorization.search = new URLSearchParams({
    response_type: 'code',
    client_id: configuration.simulatorConfiguration.clientConfiguration.clientId,
    scope: 'openid',
    redirect_uri: configuration.springConfiguration['spring.security.oauth2.client.registration.internal-access.redirect-uri'],
    state: 'council-bootstrap-readiness',
    nonce: 'council-bootstrap-readiness',
    code_challenge: 'Gw9SLr0OzFFZKnWjvKbE4RPr66HqEAxH8TqqrsN4oUw',
    code_challenge_method: 'S256',
  }).toString();
  const interactive = await fetch(authorization, {redirect: 'manual', signal: AbortSignal.timeout(10000)});
  requireInteractiveAuthorization(interactive.status, await interactive.text());
  await writeFile(output, `${JSON.stringify(configuration.springConfiguration, null, 2)}\n`, {flag: 'wx', mode: 0o600});
  console.log(`Council OIDC configured at ${loopbackOrigin(simulatorUrl)}; merge ${output} into the existing local Spring JSON before restarting.`);
  console.log('Use gatling.council.simulatorUrl for this simulator; existing gatling.simulatorUrl remains One Login.');
  console.log('Passcodes remain disabled: explicitly add require-passcode to the local Spring profiles and gatling.operator.passcodes=true for a separate passcode run.');
}

if (process.argv[1] && import.meta.url === pathToFileURL(resolve(process.argv[1])).href) {
  configure().catch(error => {
    console.error(error.message);
    process.exitCode = 1;
  });
}
