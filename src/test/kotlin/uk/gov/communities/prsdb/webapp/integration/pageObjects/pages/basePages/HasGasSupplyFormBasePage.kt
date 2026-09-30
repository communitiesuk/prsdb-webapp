package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.CONTINUE_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_THIS_LATER_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader.SectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios

open class HasGasSupplyFormBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val heading = Heading(page.locator("h1"))
    val form = HasGasSupplyForm(page)
    val sectionHeader = SectionHeader(page.locator("main"))

    fun submitHasGasSupply() {
        form.hasGasSupplyRadios.selectValue("true")
        form.submitPrimaryButton()
    }

    fun submitHasNoGasSupply() {
        form.hasGasSupplyRadios.selectValue("false")
        form.submitPrimaryButton()
    }

    fun submitProvideThisLater() = form.submitSecondaryButton()

    class HasGasSupplyForm(
        page: Page,
    ) : PostForm(page) {
        val hasGasSupplyRadios = Radios(locator)

        fun submitPrimaryButton(buttonAction: String = CONTINUE_BUTTON_ACTION_NAME) = submitSelectedButton(buttonAction)

        fun submitSecondaryButton(buttonAction: String = PROVIDE_THIS_LATER_BUTTON_ACTION_NAME) = submitSelectedButton(buttonAction)
    }
}
