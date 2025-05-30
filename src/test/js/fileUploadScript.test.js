import assert from 'node:assert/strict';
import { describe, test } from 'node:test';
import jsdom from 'global-jsdom';
import {createFileUploadIntercepter} from '#main-javascript/fileUploadScript.js';
import fileUploadConstants from '#main-javascript/fileUploadConstants.json' with {type: "json"};


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
        if (!event.defaultPrevented) {
            submittedForms.push(event.target)
        }
    });

    return submittedForms;
}

function dispatchSubmitEventTo(form) {
    const submitEvent = new window.Event('submit', {bubbles: true, cancelable: true});
    form.dispatchEvent(submitEvent);
}

describe('File Upload Handler', () => {

    test('Intercepts large files and submits a metadata only synthetic form instead', () => {
        const form = document.querySelector('form#test-form');
        const mockFile = {
            name: 'test.pdf',
            type: 'application/pdf',
            size: fileUploadConstants.maxFileSizeBytes + 1
        };

        const submittedForms = mockFormSubmissions()

        const handler = createFileUploadIntercepter(form, () => mockFile);
        form.addEventListener('submit', handler);

        dispatchSubmitEventTo(form);

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
            {key: 'contentLength', value: fileUploadConstants.maxFileSizeBytes + 1}]) {
            const input = submittedForm.querySelector(`input[name="${pair.key}"]`);
            assert.equal(input.value, pair.value.toString());
        }

        form.removeEventListener('submit', handler);
    });

    test('Does not intercept small files form submissions so the default form behaviour occurs', () => {
        const form = document.querySelector('form#test-form');
        const mockFile = {
            name: 'test.pdf',
            type: 'application/pdf',
            size: fileUploadConstants.maxFileSizeBytes
        };

        const submittedForms = mockFormSubmissions()

        const handler = createFileUploadIntercepter(form, () => mockFile);
        form.addEventListener('submit', handler);

        dispatchSubmitEventTo(form);

        assert.strictEqual(submittedForms.length, 1);
        const submittedForm = submittedForms[0];

        // Verify default form was submitted
        assert.strictEqual(submittedForm.method.toLowerCase(), 'post');
        assert.strictEqual(submittedForm.style.display.toLowerCase(), '');
        assert.equal(submittedForm.id, 'test-form');

        form.removeEventListener('submit', handler);
    });
});