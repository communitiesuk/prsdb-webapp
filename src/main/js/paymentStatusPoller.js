export const POLL_INTERVAL_MS = 3000;
export const MAX_ATTEMPTS = 20;

const JS_HIDDEN_CLASS = 'moj-js-hidden';
const JS_CONTENT_ID = 'payment-status-js-content';
const NO_JS_CONTENT_ID = 'payment-status-no-js-content';

export function initPaymentStatusPoller() {
    const jsContent = document.getElementById(JS_CONTENT_ID);
    const noJsContent = document.getElementById(NO_JS_CONTENT_ID);
    const form = noJsContent?.querySelector('form');
    if (!jsContent || !form) {
        return;
    }
    createPaymentStatusPoller({ jsContent, noJsContent, form }).start();
}

export function createPaymentStatusPoller(
    { jsContent, noJsContent, form },
    {
        fetchFn = (url, options) => fetch(url, options),
        navigate = (url) => window.location.replace(url),
        schedule = (callback, delayMs) => setTimeout(callback, delayMs),
    } = {},
) {
    const showJavaScriptContent = () => {
        jsContent.hidden = false;
    };

    const showNoJavaScriptContent = () => {
        jsContent.hidden = true;
        noJsContent.classList.remove(JS_HIDDEN_CLASS);
    };

    const postForm = () => fetchFn(form.action, { method: 'POST', body: new URLSearchParams(new FormData(form)) });

    const wasRedirectedToAnotherPage = (response) => new URL(response.url).pathname !== window.location.pathname;

    const checkPaymentStatus = async (attempt = 1) => {
        const response = await postForm().catch(() => null);
        if (!response?.ok) {
            showNoJavaScriptContent();
        } else if (wasRedirectedToAnotherPage(response)) {
            navigate(response.url);
        } else if (attempt < MAX_ATTEMPTS) {
            schedule(() => checkPaymentStatus(attempt + 1), POLL_INTERVAL_MS);
        } else {
            showNoJavaScriptContent();
        }
    };

    const start = () => {
        showJavaScriptContent();
        return checkPaymentStatus();
    };

    return { start };
}
