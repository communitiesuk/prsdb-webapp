import assert from 'node:assert/strict';
import { afterEach, beforeEach, describe, mock, test } from 'node:test';
import jsdom from 'global-jsdom';
import {
    createPaymentStatusPoller,
    initPaymentStatusPoller,
    MAX_ATTEMPTS,
    POLL_INTERVAL_MS,
} from '#main-javascript/paymentStatusPoller.js';

const PAGE_URL = 'http://localhost/landlord/register-property/payment-return?journeyId=abc&paymentReference=ref';
const STILL_PROCESSING_URL = 'http://localhost/landlord/register-property/payment-return?journeyId=abc';
const OUTCOME_URL = 'http://localhost/landlord/register-property/confirmation';

const POLLER_HTML = `
    <div id="payment-status-js-content" hidden></div>
    <div id="payment-status-no-js-content" class="moj-js-hidden">
        <form action="" method="post"><input type="hidden" name="_csrf" value="csrf-token"></form>
    </div>`;

describe('Payment status poller', () => {
    let cleanup;
    let originalFetch;
    let jsContent;
    let noJsContent;
    let fetchCalls;
    let navigatedTo;
    let scheduled;

    const stillProcessing = () => ({ ok: true, url: STILL_PROCESSING_URL });

    const isShowingJavaScriptContent = () => !jsContent.hidden && noJsContent.classList.contains('moj-js-hidden');
    const isShowingNoJavaScriptContent = () => jsContent.hidden && !noJsContent.classList.contains('moj-js-hidden');

    const createPoller = (respond = stillProcessing) =>
        createPaymentStatusPoller({ jsContent, noJsContent }, {
            fetchFn: async (url, options) => {
                fetchCalls.push({ url, options });
                return respond();
            },
            navigate: (url) => navigatedTo.push(url),
            schedule: (callback, delayMs) => scheduled.push({ callback, delayMs }),
        });

    const startAndRunFirstCheck = async (respond) => {
        createPoller(respond).start();
        await scheduled[0].callback();
    };

    beforeEach(() => {
        cleanup = jsdom(`<!DOCTYPE html><html><body>${POLLER_HTML}</body></html>`, { url: PAGE_URL });
        originalFetch = globalThis.fetch;

        jsContent = document.getElementById('payment-status-js-content');
        noJsContent = document.getElementById('payment-status-no-js-content');
        fetchCalls = [];
        navigatedTo = [];
        scheduled = [];
    });

    afterEach(() => {
        mock.timers.reset();
        globalThis.fetch = originalFetch;
        cleanup();
    });

    test('initialising does nothing when the page has no payment status poller', async () => {
        document.body.innerHTML = '<form action="" method="post"></form>';
        globalThis.fetch = async (url) => {
            fetchCalls.push({ url });
        };

        initPaymentStatusPoller();
        await new Promise((resolve) => setImmediate(resolve));

        assert.strictEqual(fetchCalls.length, 0);
    });

    test('initialising shows the JavaScript content and checks the payment status after the poll interval', async () => {
        mock.timers.enable({ apis: ['setTimeout'] });
        globalThis.fetch = (url) => {
            fetchCalls.push({ url });
            return new Promise(() => {});
        };

        initPaymentStatusPoller();
        mock.timers.tick(POLL_INTERVAL_MS - 1);
        await new Promise((resolve) => setImmediate(resolve));

        assert.ok(isShowingJavaScriptContent());
        assert.strictEqual(fetchCalls.length, 0);

        mock.timers.tick(1);
        await new Promise((resolve) => setImmediate(resolve));

        assert.strictEqual(fetchCalls.length, 1);
    });

    test('starting waits for the poll interval before the first check, as loading the page checked the payment status', () => {
        createPoller().start();

        assert.strictEqual(fetchCalls.length, 0);
        assert.strictEqual(scheduled.length, 1);
        assert.strictEqual(scheduled[0].delayMs, POLL_INTERVAL_MS);
    });

    test('checking loads the current page without using a cached response', async () => {
        await startAndRunFirstCheck();

        assert.strictEqual(fetchCalls[0].url, PAGE_URL);
        assert.strictEqual(fetchCalls[0].options.method, undefined);
        assert.strictEqual(fetchCalls[0].options.cache, 'no-store');
    });

    test('redirects to the outcome page when the check ends on a different page', async () => {
        await startAndRunFirstCheck(() => ({ ok: true, url: OUTCOME_URL }));

        assert.deepStrictEqual(navigatedTo, [OUTCOME_URL]);
    });

    test('schedules another check when the payment is still processing', async () => {
        await startAndRunFirstCheck();

        assert.strictEqual(scheduled.length, 2);
        assert.strictEqual(scheduled[1].delayMs, POLL_INTERVAL_MS);
    });

    test('shows the no-JavaScript content and stops checking after the maximum number of attempts', async () => {
        createPoller().start();
        for (let attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            await scheduled.at(-1).callback();
        }

        assert.strictEqual(fetchCalls.length, MAX_ATTEMPTS);
        assert.strictEqual(scheduled.length, MAX_ATTEMPTS);
        assert.ok(isShowingNoJavaScriptContent());
    });

    test('shows the no-JavaScript content and stops checking when the check returns an error', async () => {
        await startAndRunFirstCheck(() => ({ ok: false, url: STILL_PROCESSING_URL }));

        assert.ok(isShowingNoJavaScriptContent());
        assert.strictEqual(scheduled.length, 1);
    });

    test('shows the no-JavaScript content and stops checking when the check fails', async () => {
        await startAndRunFirstCheck(() => {
            throw new TypeError('Failed to fetch');
        });

        assert.ok(isShowingNoJavaScriptContent());
        assert.strictEqual(scheduled.length, 1);
    });
});