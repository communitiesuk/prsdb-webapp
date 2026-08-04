package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.CONTINUE_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_THIS_LATER_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios

abstract class LicensingTypeFormPage(
    page: Page,
    urlSegment: String,
) : BasePage(
        page,
        urlSegment,
    ) {
    val form = LicensingTypeForm(page)

    fun submitLicensingType(licensingType: LicensingType) {
        form.licensingTypeRadios.selectValue(licensingType)
        form.submitPrimaryButton()
    }

    fun submitProvideThisLater() = form.submitSecondaryButton()

    val provideThisLaterButton = form.provideThisLaterButton

    class LicensingTypeForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val licensingTypeRadios = Radios(locator)
        val provideThisLaterButton = locator.locator("button[type='submit'][value='$PROVIDE_THIS_LATER_BUTTON_ACTION_NAME']")

        fun submitPrimaryButton(buttonAction: String = CONTINUE_BUTTON_ACTION_NAME) = submitSelectedButton(buttonAction)

        fun submitSecondaryButton(buttonAction: String = PROVIDE_THIS_LATER_BUTTON_ACTION_NAME) = submitSelectedButton(buttonAction)
    }
}
