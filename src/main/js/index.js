import {initAll} from 'govuk-frontend'
import {enhanceSelectElement} from 'accessible-autocomplete'

initAll()

window.onload = addAccessibleAutocompleteToSelectElements

function addAccessibleAutocompleteToSelectElements() {
    document.querySelectorAll('.govuk-select')
        .forEach((selectElement) => enhanceSelectElement({
            selectElement: selectElement,
            showAllValues: true,
            inputClasses: 'govuk-label',
            menuClasses: 'govuk-label',
        }))
}