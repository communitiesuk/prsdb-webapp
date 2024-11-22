import {enhanceSelectElement} from 'accessible-autocomplete'

export function initSelectAutocomplete() {
    window.onload = () => document.querySelectorAll('.govuk-select')
        .forEach((selectElement) => enhanceSelectElement({
            selectElement: selectElement,
            showAllValues: true,
            inputClasses: 'govuk-label',
            menuClasses: 'govuk-label',
        }))
}