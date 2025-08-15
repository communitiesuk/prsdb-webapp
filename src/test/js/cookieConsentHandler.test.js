import jsdom from 'global-jsdom';
import { describe, test, beforeEach } from 'node:test';
import assert from 'node:assert/strict';
import { addCookieConsentHandler } from '#main-javascript/cookieConsentHandler.js';

let teardown;

let cookieBanner;
let cookieMessage;
let acceptButton;
let confirmationMessage;
let acceptedText;
let rejectButton;
let rejectionMessage;
let rejectedText;
let hideButton;

let consentForm;
let yesRadio;
let noRadio;
let submitButton;
let successBanner;

const CONSENT_COOKIE_NAME = 'cookie_consent';
const GA_COOKIE_NAME = '_ga';
const GA_ID_COOKIE_NAME = '_ga_XXXXXXXXXX';
const consentFormValues = [null, false, true];

function setup(onCookiesPage = false) {
    if (teardown !== undefined) {
        teardown();
    }

    const cookieBannerHtml = `
        <div class="govuk-cookie-banner" hidden>
            <div id="cookie-banner-message">
                <button id="accept-cookies-button"></button>
                <button id="reject-cookies-button"></button>
            </div>
            <div id="cookie-banner-confirmation-message" hidden>
                <span id="cookies-accepted-text" hidden></span>
                <span id="cookies-rejected-text" hidden></span>
                <button id="hide-cookies-confirmation-button"></button>
            </div>
        </div>
    `;

    const cookiesPageHtml = `
        <div class="govuk-notification-banner--success" hidden></div>
        <form hidden>
            <input type="radio" value="true" id="consent-yes" name="consent">
            <input type="radio" value="false" id="consent-no" name="consent">
            <button type="submit" id="submit-consent-button"></button>
        </form>
    `;

    const html = cookieBannerHtml + (onCookiesPage ? cookiesPageHtml : '');

    teardown = jsdom(html, {
        url: onCookiesPage ? 'https://example.com/cookies' : 'https://example.com'
    });

    window.GOOGLE_ANALYTICS_MEASUREMENT_ID = 'G-XXXXXXXXXX';
    window.GOOGLE_COOKIE_DOMAIN = 'communities.gov.uk';
    window.dataLayer = [];

    cookieBanner = document.querySelector('.govuk-cookie-banner');
    cookieMessage = document.getElementById('cookie-banner-message');
    acceptButton = document.getElementById('accept-cookies-button');
    confirmationMessage = document.getElementById('cookie-banner-confirmation-message');
    acceptedText = document.getElementById('cookies-accepted-text');
    rejectButton = document.getElementById('reject-cookies-button');
    rejectionMessage = document.getElementById('cookie-banner-confirmation-message');
    rejectedText = document.getElementById('cookies-rejected-text');
    hideButton = document.getElementById('hide-cookies-confirmation-button');

    consentForm = document.querySelector('form');
    yesRadio = document.getElementById('consent-yes');
    noRadio = document.getElementById('consent-no');
    submitButton = document.getElementById('submit-consent-button');
    successBanner = document.querySelector('.govuk-notification-banner--success');
}

function expectedLastDataLayerElement(isConsentGranted) {
    return ['consent', 'update', { analytics_storage: isConsentGranted ? 'granted' : 'denied' }];
}

describe('Cookie Consent Handler', () => {
    beforeEach(() => { setup() });

    describe('updates Google Analytics (GA) consent', () => {
        test('to granted if consent cookie is set to true', () => {
            document.cookie = `${CONSENT_COOKIE_NAME}=true`;
            document.cookie = `${GA_COOKIE_NAME}=123;`;
            document.cookie = `${GA_ID_COOKIE_NAME}=456;`

            addCookieConsentHandler();

            assert.deepStrictEqual(window.dataLayer.at(-1), expectedLastDataLayerElement(true));
            assert.strictEqual(document.cookie.includes(`${GA_COOKIE_NAME}=`), true);
            assert.strictEqual(document.cookie.includes(`${GA_ID_COOKIE_NAME}=`), true);
        })

        test('to denied and expires existing GA cookies if consent cookie is set to false', () => {
            document.cookie = `${CONSENT_COOKIE_NAME}=false;`;
            document.cookie = `${GA_COOKIE_NAME}=123;`;
            document.cookie = `${GA_ID_COOKIE_NAME}=456;`

            addCookieConsentHandler();

            assert.deepStrictEqual(window.dataLayer.at(-1), expectedLastDataLayerElement(false));
            assert.strictEqual(document.cookie.includes(`${GA_COOKIE_NAME}=`), false);
            assert.strictEqual(document.cookie.includes(`${GA_ID_COOKIE_NAME}=`), false);
        });

        test('to denied if consent cookie is not set', () => {
            addCookieConsentHandler();

            assert.deepStrictEqual(window.dataLayer.at(-1), expectedLastDataLayerElement(false));
        });
    });

    describe('makes cookie banner', () => {
        test('show if not on cookies page and consent cookie is not set', () => {
            addCookieConsentHandler();

            assert.strictEqual(cookieBanner.hidden, false);
        });

        test('hide if consent cookie is set', () => {
            document.cookie = `${CONSENT_COOKIE_NAME}=true`;

            addCookieConsentHandler();

            assert.strictEqual(cookieBanner.hidden, true);
        });

        test('hide if on cookies page', () => {
            const onCookiesPage = true;
            setup(onCookiesPage);

            addCookieConsentHandler();

            assert.strictEqual(cookieBanner.hidden, true);
        });

        test('accept cookies button create consent cookie, grant GA consent, and display confirmation message when clicked', () => {
            addCookieConsentHandler();
            acceptButton.click();

            assert.strictEqual(document.cookie.includes(`${CONSENT_COOKIE_NAME}=true`), true);
            assert.deepStrictEqual(window.dataLayer.at(-1),  expectedLastDataLayerElement(true));
            assert.strictEqual(cookieMessage.hidden, true);
            assert.strictEqual(confirmationMessage.hidden, false);
            assert.strictEqual(acceptedText.hidden, false);
        });

        test('reject cookies button create no-consent cookie, deny GA consent, and display rejection message when clicked', () => {
            addCookieConsentHandler();
            rejectButton.click();

            assert.strictEqual(document.cookie.includes(`${CONSENT_COOKIE_NAME}=false`), true);
            assert.deepStrictEqual(window.dataLayer.at(-1),  expectedLastDataLayerElement(false));
            assert.strictEqual(cookieMessage.hidden, true);
            assert.strictEqual(rejectionMessage.hidden, false);
            assert.strictEqual(rejectedText.hidden, false);
        });

        test('hide confirmation button hide cookie banner when clicked', () => {
            addCookieConsentHandler();
            acceptButton.click();
            hideButton.click();

            assert.strictEqual(cookieBanner.hidden, true);
        });
    });

    describe('makes cookies page', () => {
        consentFormValues.forEach((consentValue) => {
            test(`show consent form with ${consentValue ? 'yes' : 'no'} checked when consent cookie's value is ${consentValue}`, () => {
                const onCookiesPage = true;
                setup(onCookiesPage);
                if (consentValue !== null) { document.cookie = `${CONSENT_COOKIE_NAME}=${consentValue}`; }

                addCookieConsentHandler();

                const expectedCheckedRadio = consentValue ? yesRadio : noRadio;
                assert.strictEqual(expectedCheckedRadio.checked, true);
                assert.strictEqual(consentForm.hidden, false);
            });
        });

        test('consent form create consent cookie, grant GA consent, and display success banner when yes is submitted', () => {
            const onCookiesPage = true;
            setup(onCookiesPage);

            addCookieConsentHandler();
            yesRadio.checked = true;
            submitButton.click();

            assert.strictEqual(document.cookie.includes(`${CONSENT_COOKIE_NAME}=true`), true);
            assert.deepStrictEqual(window.dataLayer.at(-1), expectedLastDataLayerElement(true));
            assert.strictEqual(successBanner.hidden, false);
            assert.strictEqual(window.scrollY, 0);
        });

        test('consent form create no-consent cookie, deny GA consent, and display success banner when no is submitted', () => {
            const onCookiesPage = true;
            setup(onCookiesPage);

            addCookieConsentHandler();
            noRadio.checked = true;
            submitButton.click();

            assert.strictEqual(document.cookie.includes(`${CONSENT_COOKIE_NAME}=false`), true);
            assert.deepStrictEqual(window.dataLayer.at(-1), expectedLastDataLayerElement(false));
            assert.strictEqual(successBanner.hidden, false);
            assert.strictEqual(window.scrollY, 0);
        });
    });
});