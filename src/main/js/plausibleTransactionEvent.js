export function initPlausibleEventButtons() {
    document.querySelectorAll('[data-plausible-event]').forEach((element) => {
        const eventName = element.dataset.plausibleEvent;
        const form = element.closest('form');

        if (form) {
            form.addEventListener('submit', () => fireEvent(eventName));
        } else {
            element.addEventListener('click', () => fireEvent(eventName));
        }
    });
}

function fireEvent(eventName) {
    if (typeof window.plausible === 'function') {
        window.plausible(eventName);
    }
}
