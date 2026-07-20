import assert from 'node:assert/strict';
import { afterEach, beforeEach, describe, test } from 'node:test';
import jsdom from 'global-jsdom';
import { initPlausibleEventButtons } from '#main-javascript/plausibleTransactionEvent.js';

describe('Plausible event buttons', () => {
    let cleanup;
    let plausibleCalls;
    let submittedForms;

    beforeEach(() => {
        cleanup = jsdom('<!DOCTYPE html><html><body></body></html>');

        plausibleCalls = [];
        window.plausible = (name, options) => plausibleCalls.push({ name, options });

        submittedForms = [];
        HTMLFormElement.prototype.submit = function () {
            submittedForms.push(this);
        };
    });

    afterEach(() => {
        cleanup();
    });

    test('clicking a tagged element that is not in a form fires the named event', () => {
        document.body.innerHTML = `<a id="link" href="/next" data-plausible-event="Transaction">Go</a>`;

        initPlausibleEventButtons();
        document.getElementById('link').dispatchEvent(new window.Event('click', { bubbles: true }));

        assert.strictEqual(plausibleCalls.length, 1);
        assert.strictEqual(plausibleCalls[0].name, 'Transaction');
    });

    test('submitting a form via a tagged button fires the event without blocking submission', () => {
        document.body.innerHTML = `
            <form id="form">
                <button type="submit" data-plausible-event="Transaction">Confirm and submit</button>
            </form>`;
        const form = document.getElementById('form');

        initPlausibleEventButtons();
        const submitEvent = new window.Event('submit', { bubbles: true, cancelable: true });
        form.dispatchEvent(submitEvent);

        assert.strictEqual(submitEvent.defaultPrevented, false, 'submission must proceed normally');
        assert.strictEqual(plausibleCalls.length, 1);
        assert.strictEqual(plausibleCalls[0].name, 'Transaction');
    });

    test('the form still submits normally if window.plausible is unavailable', () => {
        delete window.plausible;
        document.body.innerHTML = `
            <form id="form">
                <button type="submit" data-plausible-event="Transaction">Confirm</button>
            </form>`;
        const form = document.getElementById('form');

        initPlausibleEventButtons();
        const submitEvent = new window.Event('submit', { bubbles: true, cancelable: true });
        form.dispatchEvent(submitEvent);

        assert.strictEqual(submitEvent.defaultPrevented, false, 'submission must proceed normally');
        assert.strictEqual(plausibleCalls.length, 0);
    });
});
