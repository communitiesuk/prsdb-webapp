package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateGasSafetyJourneyPages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.CONTINUE_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_THIS_LATER_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.controllers.UpdateGasSafetyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasCertStep

class HasGasCertFormPageUpdateGasSafety(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        UpdateGasSafetyController.UPDATE_GAS_SAFETY_ROUTE
            .replace("{propertyOwnershipId}", urlArguments["propertyOwnershipId"]!!) +
            "/${HasGasCertStep.ROUTE_SEGMENT}",
    ) {
    val heading = Heading(page.locator("h1"))
    val form = HasGasCertForm(page)

    val provideThisLaterButton: Locator =
        page.locator("button[name='action'][value='$PROVIDE_THIS_LATER_BUTTON_ACTION_NAME']")

    fun submitHasCertificate() {
        form.hasCertRadios.selectValue("true")
        form.submitPrimaryButton()
    }

    fun submitHasNoCertificate() {
        form.hasCertRadios.selectValue("false")
        form.submitPrimaryButton()
    }

    class HasGasCertForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val hasCertRadios = Radios(locator)

        fun submitPrimaryButton(buttonAction: String = CONTINUE_BUTTON_ACTION_NAME) = submitSelectedButton(buttonAction)
    }
}
