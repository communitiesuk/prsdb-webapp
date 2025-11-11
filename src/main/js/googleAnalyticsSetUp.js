export function setGtagConsentDefault() {
    // Set consent for Google tag to denied by default
    window.dataLayer = window.dataLayer || [];
    function gtag() { dataLayer.push(arguments); }
    gtag('consent', 'default', {
        'ad_user_data': 'denied',
        'ad_personalization': 'denied',
        'ad_storage': 'denied',
        'analytics_storage': 'denied',
        'wait_for_update': 500,
    });
}
export function gtagSetUp(measurementIdAsString) {
    //  Google tag (gtag.js)
    window.dataLayer = window.dataLayer || [];
    function gtag() { dataLayer.push(arguments); }
    gtag('js', new Date());
    gtag('config', [[measurementIdAsString]])
}

export function setGaEnvVars(googleAnalyticsMeasurementId, googleAnalyticsCookieDomain) {
    // Add environment variables to the window so they can be accessed in js scripts
    window.GOOGLE_ANALYTICS_MEASUREMENT_ID = [[googleAnalyticsMeasurementId]];
    window.GOOGLE_COOKIE_DOMAIN = [[googleAnalyticsCookieDomain]];
}
