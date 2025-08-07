import {initAll as initGDS} from 'govuk-frontend';
import {initSelectAutocomplete} from "./autocomplete";
import {addFileUploadListener} from "./fileUploadScript";
import {addCookieConsentHandler} from "./cookieConsentHandler.js";
import $ from 'jquery'
import {initAll as initMoJDS} from '@ministryofjustice/frontend'
import '../resources/css/custom.scss'
import {googleAnalyticsEnabler} from "#main-javascript/googleAnalyticsEnabler.js";

initGDS()

initSelectAutocomplete()

addFileUploadListener()

addCookieConsentHandler()

googleAnalyticsEnabler()

window.$ = $
initMoJDS()