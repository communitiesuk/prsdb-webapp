import {initAll as initGDS} from 'govuk-frontend';
import {initSelectAutocomplete} from "./autocomplete";
import {addFileUploadListener} from "./fileUploadScript";
import $ from 'jquery'
import {initAll as initMoJDS} from '@ministryofjustice/frontend'
import {initFilterToggleButton} from "./filterToggleButton"
import '../resources/css/custom.scss'
import {setJsEnabled} from "#main-javascript/setJsEnabled.js";

setJsEnabled()

initGDS()

initSelectAutocomplete()

addFileUploadListener()

window.$ = $
initMoJDS()
initFilterToggleButton()
