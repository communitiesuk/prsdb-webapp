import fileUploadConstants from './fileUploadConstants.json' with {type: "json"};

export function addFileUploadListener() {
    const form = document.querySelector('form#single-file-upload-form[enctype="multipart/form-data"]');
    if (form)
        form.addEventListener('submit', createFileUploadIntercepter(form));
}

export function createFileUploadIntercepter(form, getFile = getInputFileFromForm) {
    return (event) => {
        const file = getFile(form);
        if (isFileTooLarge(file)) {
            substituteWithMetadataUpload(event, file, form);
        } else {
            disableSubmitButton(form);
        }
    }
}

function substituteWithMetadataUpload(event, file, form) {
    // Do not submit the file
    event.preventDefault();

    // Submit a synthetic form with the file metadata instead
    submitSyntheticForm({
        _csrf: form.querySelector('input[name="_csrf"]').value,
        name: file.name,
        contentType: file.type,
        contentLength: file.size
    });
}

function disableSubmitButton(form) {
    // If the file is valid, allow the form to be submitted normally but prevent users resubmitting
    const submitButton = form.querySelector('button[type="submit"]');
    submitButton.disabled = true;
}

function submitSyntheticForm(data) {
    const form = createHiddenForm(data);
    document.body.appendChild(form);
    form.submit();
    document.body.removeChild(form);
}

function createHiddenForm(data) {
    const form = document.createElement('form');
    form.action = '';
    form.method = 'POST';
    form.style.display = 'none';

    for (const key in data) {
        const input = createHiddenInput(key, data[key]);
        form.appendChild(input);
    }

    return form;
}

function createHiddenInput(key, value){
    const input = document.createElement('input');
    input.type = 'hidden';
    input.name = key;
    input.value = value;
    return input;
}

function getInputFileFromForm(form){
    const fileInput = form.querySelector('.govuk-file-upload');
    return fileInput.files[0];
}

function isFileTooLarge(file) {
    return file.size > fileUploadConstants.maxFileSizeBytes;
}
