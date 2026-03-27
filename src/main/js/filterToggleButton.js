import $ from 'jquery'
import {FilterToggleButton} from '@ministryofjustice/frontend'

export function initFilterToggleButton() {
    if (!document.querySelector('.moj-filter')) return

    new FilterToggleButton({
        bigModeMediaQuery: '(min-width: 48.0625em)',
        startHidden: false,
        toggleButton: {
            container: $('.moj-action-bar__filter'),
            showText: 'Show filters panel',
            hideText: 'Close filters panel',
            classes: 'govuk-button--secondary'
        },
        closeButton: {
            container: $('.moj-filter__header-action'),
            text: 'Close'
        },
        filter: {
            container: $('.moj-filter')
        }
    })
}
