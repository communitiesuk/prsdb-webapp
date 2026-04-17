package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcExemptionStep

class EpcExemptionFormPagePropertyRegistration(
    page: Page,
) : BasePage(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${EpcExemptionStep.ROUTE_SEGMENT}") {
    val form = EpcExemptionForm(page)

    fun submitExemptionReason(exemptionReason: EpcExemptionReason) {
        form.exemptionReasonRadios.selectValue(exemptionReason)
        form.submit()
    }

    class EpcExemptionForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val exemptionReasonRadios = Radios(locator)
    }
}
