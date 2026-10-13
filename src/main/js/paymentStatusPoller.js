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

// Submits the payment status form in the background. The server redirects to the outcome page once the payment has
// finished, or back to this page while it is still processing.
export function createPaymentStatusPoller(
    { jsContent, noJsContent, form },
    {
        fetchFn = (url, options) => fetch(url, options),
        navigate = (url) => window.location.replace(url),
        schedule = (callback, delayMs) => setTimeout(callback, delayMs),
    } = {},
) {
    let attempts = 0;

    const showJavaScriptContent = () => {
        jsContent.hidden = false;
    };

    // Falls back to the manual check, e.g. when automatic checks give up. MoJ Frontend's moj-js-hidden class is what
    // hides the manual check when JavaScript is enabled.
    const showNoJavaScriptContent = () => {
        jsContent.hidden = true;
        noJsContent.classList.remove(JS_HIDDEN_CLASS);
    };

    async function poll() {
        attempts += 1;
        try {
            // Sent URL-encoded, as the browser submits the form, because multipart requests are handled as file uploads
            const response = await fetchFn(form.action, { method: 'POST', body: new URLSearchParams(new FormData(form)) });
            if (!response.ok) {
                showNoJavaScriptContent();
            } else if (new URL(response.url).pathname !== window.location.pathname) {
                navigate(response.url);
            } else if (attempts >= MAX_ATTEMPTS) {
                showNoJavaScriptContent();
            } else {
                schedule(poll, POLL_INTERVAL_MS);
            }
        } catch {
            showNoJavaScriptContent();
        }
    }

    function start() {
        showJavaScriptContent();
        return poll();
    }

    return { start, poll };
}
