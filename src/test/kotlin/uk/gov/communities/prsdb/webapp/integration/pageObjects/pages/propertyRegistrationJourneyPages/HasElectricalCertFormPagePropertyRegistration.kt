package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.CONTINUE_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_THIS_LATER_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.constants.enums.HasElectricalSafetyCertificate
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertStep

class HasElectricalCertFormPagePropertyRegistration(
    page: Page,
) : BasePage(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${HasElectricalCertStep.ROUTE_SEGMENT}") {
    val heading = Heading(page.locator("h1"))
    val form = HasElectricalCertForm(page)

    fun submitHasEic() {
        form.hasCertRadios.selectValue(HasElectricalSafetyCertificate.HAS_EIC)
        form.submitPrimaryButton()
    }

    fun submitHasEicr() {
        form.hasCertRadios.selectValue(HasElectricalSafetyCertificate.HAS_EICR)
        form.submitPrimaryButton()
    }

    fun submitHasNoCert() {
        form.hasCertRadios.selectValue(HasElectricalSafetyCertificate.NO_CERTIFICATE)
        form.submitPrimaryButton()
    }

    fun submitProvideThisLater() = form.submitSecondaryButton()

    class HasElectricalCertForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val hasCertRadios = Radios(locator)

        fun submitPrimaryButton(buttonAction: String = CONTINUE_BUTTON_ACTION_NAME) = submitSelectedButton(buttonAction)

        fun submitSecondaryButton(buttonAction: String = PROVIDE_THIS_LATER_BUTTON_ACTION_NAME) = submitSelectedButton(buttonAction)
    }
}
