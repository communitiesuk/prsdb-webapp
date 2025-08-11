import * as cookieHelper from 'cookie';

const CONSENT_COOKIE_NAME = 'cookie_consent';
const COOKIES_ROUTE = '/cookies';

export function addCookieConsentHandler() {
    const consentCookieValue = cookieHelper.parse(document.cookie)[CONSENT_COOKIE_NAME];
    const onCookiePage = location.pathname.includes(COOKIES_ROUTE);

    if (consentCookieValue == null && !onCookiePage) {
       new CookieBanner().display();
    }

    updateGtmConsent(consentCookieValue === 'true');
    if (consentCookieValue === 'false') {
        deleteCookie("_ga")
        deleteCookie("_ga_PDPW9SQ94W")
    }
}

function updateGtmConsent(isGranted = false) {
    const gtag = window.gtag || function (...args) {
        window.dataLayer = window.dataLayer || []
        window.dataLayer.push(args)
    }

    gtag('consent', 'update', {
        ad_user_data: isGranted ? 'granted' : 'denied',
        ad_personalization: isGranted ? 'granted' : 'denied',
        ad_storage: isGranted ? 'granted' : 'denied',
        analytics_storage: isGranted ? 'granted' : 'denied'
    })
}

function deleteCookie(name) {
    document.cookie = name + '=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
}

class CookieBanner {
    #cookieBanner;
    #cookieMessage;
    #acceptCookiesButton;
    #rejectCookiesButton;
    #cookieConfirmationMessage;
    #cookiesAcceptedText;
    #cookiesRejectedText;
    #hideCookiesConfirmationButton;

    constructor() {
        this.#cookieBanner = document.querySelector('.govuk-cookie-banner');
        this.#cookieMessage = document.getElementById('cookie-banner-message');
        this.#acceptCookiesButton = document.getElementById('accept-cookies-button');
        this.#rejectCookiesButton = document.getElementById('reject-cookies-button');
        this.#cookieConfirmationMessage = document.getElementById('cookie-banner-confirmation-message');
        this.#cookiesAcceptedText = document.getElementById('cookies-accepted-text');
        this.#cookiesRejectedText = document.getElementById('cookies-rejected-text');
        this.#hideCookiesConfirmationButton = document.getElementById('hide-cookies-confirmation-button');
    }

    display() {
        this.#cookieBanner.hidden = false;

        this.#handleCookiesConsentButton(this.#acceptCookiesButton, true);
        this.#handleCookiesConsentButton(this.#rejectCookiesButton, false);

        this.#hideCookiesConfirmationButton.addEventListener('click', () => {
            this.#cookieBanner.hidden = true;
        });
    }

    #handleCookiesConsentButton(button, consentValue) {
        button.addEventListener('click', () => {
            this.#createConsentCookie(consentValue);

            this.#cookieMessage.hidden = true;

            const confirmationMessageText = consentValue ? this.#cookiesAcceptedText : this.#cookiesRejectedText;
            this.#cookieConfirmationMessage.hidden = false;
            confirmationMessageText.hidden = false;

            updateGtmConsent(consentValue);
            if (consentValue === false) {
                deleteCookie("_ga")
                deleteCookie("_ga_PDPW9SQ94W")
            }
        });
    }

    #createConsentCookie(consentValue) {
        document.cookie = cookieHelper.serialize(CONSENT_COOKIE_NAME, consentValue, {
            expires: new Date(Date.now() + 365 * 24 * 60 * 60 * 1000), // 1 year from now
                path: '/',
                sameSite: true
            }
        );
    }
}