import jsdom from 'global-jsdom';
import { describe, test, beforeEach } from 'node:test';
import assert from 'node:assert/strict';
import { addCookieConsentHandler } from '#main-javascript/cookieConsentHandler.js';

let teardown;

function setup(onCookiesPage = false) {
    if (teardown !== undefined) {
        teardown();
    }

    const html = `
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

    teardown = jsdom(html, {
        url: onCookiesPage ? 'https://example.com/cookies' : 'https://example.com'
    });

    window.GOOGLE_ANALYTICS_MEASUREMENT_ID = 'G-PDPW9SQ94W'
    window.GOOGLE_COOKIE_DOMAIN = 'communities.gov.uk'
    window.dataLayer = []
}

describe('Cookie Consent Handler', () => {
    beforeEach(() => { setup() });

    test('Displays cookie banner if not on cookies page and consent cookie is not set', () => {
        const cookieBanner = document.querySelector('.govuk-cookie-banner');

        addCookieConsentHandler();

        assert.strictEqual(cookieBanner.hidden, false);
    });

    test('Does not display cookie banner if consent cookie is set', () => {
        document.cookie = 'cookie_consent=true';
        const cookieBanner = document.querySelector('.govuk-cookie-banner');

        addCookieConsentHandler();

        assert.strictEqual(cookieBanner.hidden, true);
    });

    test('Does not display cookie banner on cookies page', () => {
        const onCookiesPage = true;
        setup(onCookiesPage);
        const cookieBanner = document.querySelector('.govuk-cookie-banner');

        addCookieConsentHandler();

        assert.strictEqual(cookieBanner.hidden, true);
    });

    test('Makes accept cookies button create consent cookie and display confirmation message when clicked', () => {
        const cookieMessage = document.getElementById('cookie-banner-message');
        const acceptButton = document.getElementById('accept-cookies-button');
        const confirmationMessage = document.getElementById('cookie-banner-confirmation-message');
        const acceptedText = document.getElementById('cookies-accepted-text');

        addCookieConsentHandler();
        acceptButton.click();

        assert.strictEqual(document.cookie.includes('cookie_consent=true'), true);
        assert.strictEqual(cookieMessage.hidden, true);
        assert.strictEqual(confirmationMessage.hidden, false);
        assert.strictEqual(acceptedText.hidden, false);
    });

    test('Makes reject cookies button create no-consent cookie and display rejection message when clicked', () => {
        const cookieMessage = document.getElementById('cookie-banner-message');
        const rejectButton = document.getElementById('reject-cookies-button');
        const rejectionMessage = document.getElementById('cookie-banner-confirmation-message');
        const rejectedText = document.getElementById('cookies-rejected-text');

        addCookieConsentHandler();
        rejectButton.click();

        assert.strictEqual(document.cookie.includes('cookie_consent=false'), true);
        assert.strictEqual(cookieMessage.hidden, true);
        assert.strictEqual(rejectionMessage.hidden, false);
        assert.strictEqual(rejectedText.hidden, false);
    });

    test('Makes hide confirmation button hide cookie banner when clicked', () => {
        const cookieBanner = document.querySelector('.govuk-cookie-banner');
        const acceptButton = document.getElementById('accept-cookies-button');
        const hideButton = document.getElementById('hide-cookies-confirmation-button');

        addCookieConsentHandler();
        acceptButton.click();
        hideButton.click();

        assert.strictEqual(cookieBanner.hidden, true);
    });
    describe('updates Google Analytics (GA) consent', () => {
        test('to granted if the cookie_consent cookie is already set to true', () => {
            document.cookie = 'cookie_consent=true';

            addCookieConsentHandler();

            assert.deepStrictEqual(window.dataLayer, expectedDataLayer(true));
        })

        test('to denied and expires existing GA cookies if the cookie_consent cookie is already set to false', () => {
            // Arrange
            document.cookie = 'cookie_consent=false;';
            document.cookie = '_ga=123;';
            document.cookie = '_ga_PDPW9SQ94W=456;'
            assert.strictEqual(document.cookie.includes('_ga='), true);
            assert.strictEqual(document.cookie.includes('_ga_PDPW9SQ94W='), true);

            // Act
            addCookieConsentHandler();

            // Assert
            assert.deepStrictEqual(window.dataLayer, expectedDataLayer(false));
            assert.strictEqual(document.cookie.includes('_ga='), false);
            assert.strictEqual(document.cookie.includes('_ga_PDPW9SQ94W='), false);
        });

        test('to denied if the cookie_consent cookie is not set', () => {
            addCookieConsentHandler();

            assert.deepStrictEqual(window.dataLayer, expectedDataLayer(false));
        });

        test('to granted when cookies are accepted on the cookie banner', () => {
            const acceptButton = document.getElementById('accept-cookies-button');

            addCookieConsentHandler();
            assert.deepStrictEqual(window.dataLayer, expectedDataLayer(false));
            acceptButton.click();

            assert.deepStrictEqual(window.dataLayer[1],  expectedDataLayer(true)[0]);
        });
        test('to denied and expires existing GA cookies when cookies are rejected on the cookie banner', () => {
            document.cookie = '_ga=123; _ga_PDPW9SQ94W=456;';
            document.cookie = '_ga_PDPW9SQ94W=456;'
            assert.strictEqual(document.cookie.includes('_ga='), true);
            assert.strictEqual(document.cookie.includes('_ga_PDPW9SQ94W='), true);
            const rejectButton = document.getElementById('reject-cookies-button');

            addCookieConsentHandler();
            assert.deepStrictEqual(window.dataLayer, expectedDataLayer(false));
            rejectButton.click();

            assert.deepStrictEqual(window.dataLayer[1],  expectedDataLayer(false)[0]);
            assert.strictEqual(document.cookie.includes('_ga='), false);
            assert.strictEqual(document.cookie.includes('_ga_PDPW9SQ94W='), false);
        });
    });
});

function expectedDataLayer(granted) {
    return [
        ['consent', 'update', {
            analytics_storage: granted ? 'granted' : 'denied'
        }]
    ];
}