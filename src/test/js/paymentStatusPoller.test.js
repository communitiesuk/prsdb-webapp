import assert from 'node:assert/strict';
import { afterEach, beforeEach, describe, test } from 'node:test';
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
    let originalFormData;
    let originalFetch;
    let jsContent;
    let noJsContent;
    let form;
    let fetchCalls;
    let navigatedTo;
    let scheduled;

    const stillProcessing = () => ({ ok: true, url: STILL_PROCESSING_URL });

    const isShowingJavaScriptContent = () => !jsContent.hidden && noJsContent.classList.contains('moj-js-hidden');
    const isShowingNoJavaScriptContent = () => jsContent.hidden && !noJsContent.classList.contains('moj-js-hidden');

    const createPoller = (respond = stillProcessing) =>
        createPaymentStatusPoller({ jsContent, noJsContent, form }, {
            fetchFn: async (url, options) => {
                fetchCalls.push({ url, options });
                return respond();
            },
            navigate: (url) => navigatedTo.push(url),
            schedule: (callback, delayMs) => scheduled.push({ callback, delayMs }),
        });

    beforeEach(() => {
        cleanup = jsdom(`<!DOCTYPE html><html><body>${POLLER_HTML}</body></html>`, { url: PAGE_URL });
        // global-jsdom does not replace Node's own FormData, which cannot read jsdom forms
        originalFormData = globalThis.FormData;
        globalThis.FormData = window.FormData;
        originalFetch = globalThis.fetch;

        jsContent = document.getElementById('payment-status-js-content');
        noJsContent = document.getElementById('payment-status-no-js-content');
        form = noJsContent.querySelector('form');
        fetchCalls = [];
        navigatedTo = [];
        scheduled = [];
    });

    afterEach(() => {
        globalThis.FormData = originalFormData;
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

    test('initialising shows the JavaScript content and checks the payment status straight away', async () => {
        globalThis.fetch = (url) => {
            fetchCalls.push({ url });
            return new Promise(() => {});
        };

        initPaymentStatusPoller();
        await new Promise((resolve) => setImmediate(resolve));

        assert.ok(isShowingJavaScriptContent());
        assert.strictEqual(fetchCalls.length, 1);
    });

    test('checking posts the form, including its CSRF token, URL-encoded as multipart posts are handled as file uploads', async () => {
        await createPoller().start();

        assert.strictEqual(fetchCalls[0].url, PAGE_URL);
        assert.strictEqual(fetchCalls[0].options.method, 'POST');
        assert.ok(fetchCalls[0].options.body instanceof URLSearchParams);
        assert.strictEqual(fetchCalls[0].options.body.get('_csrf'), 'csrf-token');
    });

    test('redirects to the outcome page when the check ends on a different page', async () => {
        await createPoller(() => ({ ok: true, url: OUTCOME_URL })).start();

        assert.deepStrictEqual(navigatedTo, [OUTCOME_URL]);
    });

    test('schedules another check when the payment is still processing', async () => {
        await createPoller().start();

        assert.strictEqual(scheduled.length, 1);
        assert.strictEqual(scheduled[0].delayMs, POLL_INTERVAL_MS);
    });

    test('shows the no-JavaScript content and stops checking after the maximum number of attempts', async () => {
        await createPoller().start();
        for (let attempt = 1; attempt < MAX_ATTEMPTS; attempt++) {
            await scheduled.at(-1).callback();
        }

        assert.strictEqual(scheduled.length, MAX_ATTEMPTS - 1);
        assert.ok(isShowingNoJavaScriptContent());
    });

    test('shows the no-JavaScript content and stops checking when the check returns an error', async () => {
        await createPoller(() => ({ ok: false, url: STILL_PROCESSING_URL })).start();

        assert.ok(isShowingNoJavaScriptContent());
        assert.strictEqual(scheduled.length, 0);
    });

    test('shows the no-JavaScript content and stops checking when the check fails', async () => {
        await createPoller(() => {
            throw new TypeError('Failed to fetch');
        }).start();

        assert.ok(isShowingNoJavaScriptContent());
        assert.strictEqual(scheduled.length, 0);
    });
});
