import {initAll as initGDS} from 'govuk-frontend';
import {initSelectAutocomplete} from "./autocomplete";
import $ from 'jquery'
import {initAll as initMoJDS} from '@ministryofjustice/frontend'
import '../resources/css/prsdClasses.scss'

initGDS()

initSelectAutocomplete()

window.$ = $
initMoJDS()