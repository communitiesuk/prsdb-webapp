import assert from 'node:assert/strict';
import { describe, test, before, after } from 'node:test';
import jsdom from 'global-jsdom';
import {submitSyntheticForm, createFileUploadIntercepter} from '../../main/js/fileUploadScript.js';


    jsdom(`
<form id="test-form" method="post" enctype="multipart/form-data">
    <input type="hidden" name="_csrf" value="test-csrf-token">
    <input id="test-file-input" class="govuk-file-upload" type="file">
    <button type="submit">Submit</button>
</form>
`);

function mockFormSubmissions() {
    const submittedForms = [];

    // Mock form.submit()
    HTMLFormElement.prototype.submit = function() {
        submittedForms.push(this)
    };

    // Mock submit event reaching browser
    document.addEventListener('submit', (event) => {
        submittedForms.push(event.target)
    });

    return submittedForms;
}

describe('File Upload Handler', () => {

    test('Intercepts large files', () => {
        const form = document.querySelector('form#test-form');
        const mockFile = {
            name: 'test.pdf',
            type: 'application/pdf',
            size: 16 * 1024 * 1024
        };

        const submittedForms = mockFormSubmissions()

        const handler = createFileUploadIntercepter(form, () => mockFile);
        form.addEventListener('submit', handler);

        const submitEvent = new window.Event('submit');
        form.dispatchEvent(submitEvent);

        assert.strictEqual(submittedForms.length, 1);
        const submittedForm = submittedForms[0];

        // Verify synthetic form was submitted
        assert.strictEqual(submittedForm.method.toLowerCase(), 'post');
        assert.strictEqual(submittedForm.style.display.toLowerCase(), 'none');
        assert.equal(submittedForm.id, '');

        // Verify synthetic form inputs
        const inputs = submittedForm.querySelectorAll('input');
        assert.strictEqual(inputs.length, 4);

        for (const pair of [{key: '_csrf', value: 'test-csrf-token'},
            {key: 'name', value: 'test.pdf'},
            {key: 'contentType', value: 'application/pdf'},
            {key: 'contentLength', value: 16777216}]) {
            const input = submittedForm.querySelector(`input[name="${pair.key}"]`);
            assert.equal(input.value, pair.value.toString());
        }

        form.removeEventListener('submit', handler);
    });

    test('Intercepts small files', () => {
        const form = document.querySelector('form#test-form');
        const mockFile = {
            name: 'test.pdf',
            type: 'application/pdf',
            size: 14 * 1024 * 1024
        };

        const submittedForms = mockFormSubmissions()

        const handler = createFileUploadIntercepter(form, () => mockFile);
        form.addEventListener('submit', handler);

        const submitEvent = new window.Event('submit', {bubbles: true});
        form.dispatchEvent(submitEvent);

        assert.strictEqual(submittedForms.length, 1);
        const submittedForm = submittedForms[0];

        // Verify default form was submitted
        assert.strictEqual(submittedForm.method.toLowerCase(), 'post');
        assert.strictEqual(submittedForm.style.display.toLowerCase(), '');
        assert.equal(submittedForm.id, 'test-form');

        form.removeEventListener('submit', handler);
    });
});