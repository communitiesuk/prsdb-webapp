package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.CONTINUE_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_THIS_LATER_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios

open class HasEpcFormBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = HasEpcForm(page)

    fun submitHasEpc() {
        form.hasCertRadios.selectValue("true")
        form.submitPrimaryButton()
    }

    fun submitHasNoEpc() {
        form.hasCertRadios.selectValue("false")
        form.submitPrimaryButton()
    }

    fun submitProvideThisLater() {
        form.submitSecondaryButton()
    }

    class HasEpcForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val hasCertRadios = Radios(locator)

        fun submitPrimaryButton(buttonAction: String = CONTINUE_BUTTON_ACTION_NAME) = submitSelectedButton(buttonAction)

        fun submitSecondaryButton(buttonAction: String = PROVIDE_THIS_LATER_BUTTON_ACTION_NAME) = submitSelectedButton(buttonAction)
    }
}
