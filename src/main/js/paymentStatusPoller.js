export const POLL_INTERVAL_MS = 3000;
export const MAX_ATTEMPTS = 20;

const JS_HIDDEN_CLASS = 'moj-js-hidden';
const JS_CONTENT_ID = 'payment-status-js-content';
const NO_JS_CONTENT_ID = 'payment-status-no-js-content';

export function initPaymentStatusPoller() {
    const jsContent = document.getElementById(JS_CONTENT_ID);
    const noJsContent = document.getElementById(NO_JS_CONTENT_ID);
    if (!jsContent || !noJsContent) {
        return;
    }
    createPaymentStatusPoller({ jsContent, noJsContent }).start();
}

export function createPaymentStatusPoller(
    { jsContent, noJsContent },
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

    // Loading the page checks the payment status, and redirects to the outcome page once the payment has finished
    const fetchPage = () => fetchFn(window.location.href, { cache: 'no-store' });

    const wasRedirectedToAnotherPage = (response) => new URL(response.url).pathname !== window.location.pathname;

    const scheduleCheck = (attempt) => schedule(() => checkPaymentStatus(attempt), POLL_INTERVAL_MS);

    const checkPaymentStatus = async (attempt) => {
        const response = await fetchPage().catch(() => null);
        if (!response?.ok) {
            showNoJavaScriptContent();
        } else if (wasRedirectedToAnotherPage(response)) {
            navigate(response.url);
        } else if (attempt < MAX_ATTEMPTS) {
            scheduleCheck(attempt + 1);
        } else {
            showNoJavaScriptContent();
        }
    };

    // The payment status was checked when this page loaded, so the first check waits for the poll interval
    const start = () => {
        showJavaScriptContent();
        scheduleCheck(1);
    };

    return { start };
}
