package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.CONTINUE_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_THIS_LATER_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasCertStep

class HasGasCertFormPagePropertyRegistration(
    page: Page,
) : BasePage(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${HasGasCertStep.ROUTE_SEGMENT}") {
    val heading = Heading(page.locator("h1"))

    val form = HasGasCertForm(page)

    fun submitHasCertificate() {
        form.hasCertRadios.selectValue("true")
        form.submitPrimaryButton()
    }

    fun submitHasNoCertificate() {
        form.hasCertRadios.selectValue("false")
        form.submitPrimaryButton()
    }

    fun submitProvideThisLater() = form.submitSecondaryButton()

    class HasGasCertForm(
        page: Page,
    ) : PostForm(page) {
        val hasCertRadios = Radios(locator)

        fun submitPrimaryButton(buttonAction: String = CONTINUE_BUTTON_ACTION_NAME) = submitSelectedButton(buttonAction)

        fun submitSecondaryButton(buttonAction: String = PROVIDE_THIS_LATER_BUTTON_ACTION_NAME) = submitSelectedButton(buttonAction)
    }
}
