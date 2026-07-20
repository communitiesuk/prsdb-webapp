package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.CONTINUE_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_THIS_LATER_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader.SectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput

abstract class NumberOfHouseholdsFormBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val backLink = BackLink.default(page)

    val form = NumOfHouseholdsForm(page)
    val header = Heading(page.locator("h1"))
    val sectionHeader = SectionHeader(page.locator("main"))

    fun submitNumberOfHouseholds(num: Int) = submitNumberOfHouseholds(num.toString())

    fun submitNumberOfHouseholds(num: String) {
        form.householdsInput.fill(num)
        form.submitForm()
    }

    fun submitProvideThisLater() {
        form.submitSecondaryButton()
    }

    class NumOfHouseholdsForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val householdsInput = TextInput.textByFieldName(locator, "numberOfHouseholds")
        val fieldsetLegend = FieldsetLegend(locator)

        // TODO PDJB-1340: Tidy up when the PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING Feature Flag is removed
        fun submitForm() {
            val submitButtonForAction = locator.locator("button[type='submit'][value='continue']")
            if (submitButtonForAction.count() > 0) {
                submitPrimaryButton()
            } else {
                SubmitButton(locator).clickAndWait()
            }
        }

        fun submitPrimaryButton(buttonAction: String = CONTINUE_BUTTON_ACTION_NAME) = submitSelectedButton(buttonAction)

        fun submitSecondaryButton(buttonAction: String = PROVIDE_THIS_LATER_BUTTON_ACTION_NAME) = submitSelectedButton(buttonAction)
    }
}
