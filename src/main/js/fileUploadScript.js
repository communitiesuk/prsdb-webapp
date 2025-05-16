export function addFileUploadListener() {
    const form = document.querySelector('form#single-file-upload-form[enctype="multipart/form-data"]');
    if (form)
        form.addEventListener('submit', function (event) {
            const file = getInputFile(form);
            if (file) {
                if (isFileTooLarge(file)) {
                    // Do not submit the file
                    event.preventDefault();

                    // Submit a synthetic form with the file metadata instead
                    submitSyntheticForm({
                        _csrf: form.querySelector('input[name="_csrf"]').value,
                        name: file.name,
                        contentType: file.type,
                        contentLength: file.size
                    });
                } else {
                    // If the file is valid, allow the form to be submitted normally but prevent users resubmitting
                    const submitButton = form.querySelector('button[type="submit"]');
                    submitButton.disabled = true;
                }
            }
        });
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

function getInputFile(form){
    const fileInput = form.querySelector('.govuk-file-upload');
    return fileInput.files[0];
}

function isFileTooLarge(file) {
    const megaByteInBytes = 1024 * 1024;
    return file.size > 15 * megaByteInBytes;
}