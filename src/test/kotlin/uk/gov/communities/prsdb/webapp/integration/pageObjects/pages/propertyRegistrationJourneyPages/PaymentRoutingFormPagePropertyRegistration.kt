package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PaymentRoutingStep

// TODO PDJB-993: Delete this page object when PaymentRoutingStep becomes an internal step - the outcome will then be
//  derived from the real payment status rather than a user-submitted radio, so there will be no page to model.
class PaymentRoutingFormPagePropertyRegistration(
    page: Page,
) : BasePage(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${PaymentRoutingStep.ROUTE_SEGMENT}") {
    val form = PaymentRoutingForm(page)

    class PaymentRoutingForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val radios = Radios(locator)
    }
}
