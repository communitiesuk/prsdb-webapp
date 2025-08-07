import * as cookieHelper from 'cookie';
import {CONSENT_COOKIE_NAME} from "#main-javascript/cookieConsentHandler.js";

export function googleAnalyticsEnabler() {
    const consentCookieValue = cookieHelper.parse(document.cookie)[CONSENT_COOKIE_NAME];
    window.googleAnalyticsEnabled = consentCookieValue === 'true';
}

