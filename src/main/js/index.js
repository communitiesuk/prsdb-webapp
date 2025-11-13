import {initAll as initGDS} from 'govuk-frontend';
import {initSelectAutocomplete} from "./autocomplete";
import {addFileUploadListener} from "./fileUploadScript";
import {addCookieConsentHandler} from "./cookieConsentHandler.js";
import $ from 'jquery'
import {initAll as initMoJDS} from '@ministryofjustice/frontend'
import '../resources/css/custom.scss'
import {setJsEnabled} from "#main-javascript/setJsEnabled.js";
import {plausibleSetUp} from "#main-javascript/plausibleSetUp.js";
import {setGtagConsentDefault} from "#main-javascript/googleAnalyticsSetUp.js";

plausibleSetUp()

setGtagConsentDefault()

setJsEnabled()

initGDS()

initSelectAutocomplete()

addFileUploadListener()

addCookieConsentHandler()

window.$ = $
initMoJDS()
