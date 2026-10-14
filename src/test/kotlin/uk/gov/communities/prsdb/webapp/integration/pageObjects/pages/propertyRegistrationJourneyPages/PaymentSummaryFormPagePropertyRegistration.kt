package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PaymentSummaryStep

class PaymentSummaryFormPagePropertyRegistration(
    page: Page,
) : BasePage(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${PaymentSummaryStep.ROUTE_SEGMENT}") {
    val heading = Heading(page.locator("h1"))
    val form = FormWithSectionHeader(page)
    val summaryList = PaymentSummaryList(page)

    class PaymentSummaryList(
        page: Page,
    ) : SummaryList(page) {
        val paymentDateRow = getRow("Payment date")
    }
}
