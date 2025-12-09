export function plausibleSetUp() {
    window.plausible = window.plausible || function () {
        (window.plausible.q = window.plausible.q || []).push(arguments)
    }
}
