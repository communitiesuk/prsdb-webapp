package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import java.util.regex.Pattern

class ConfirmationPagePropertyRegistration(
    page: Page,
) : BasePage(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/$CONFIRMATION_PATH_SEGMENT") {
    val registrationNumberText: String = page.locator(".govuk-inset-text").textContent().trim()
    val whatYouNeedToDoNextHeading = page.locator("h2:has-text('What you need to do next')")
    val whatHappensNextHeading = page.locator("h2:has-text('What happens next')")
    val lettingAgentSubHeading =
        page.locator("h3:has-text('Your letting agent or property manager needs to provide the remaining required details')")
    val paymentSummaryHeading = page.locator("h2:has-text('Payment summary')")
    val paymentSummaryList = PaymentSummaryList(page)

    val surveyLink = Button.byText(page, "Go to survey")
    val goToDashboardLink = Link.byText(page, "Go to dashboard")

    class PaymentSummaryList(
        page: Page,
    ) : SummaryList(page) {
        val paymentRow = getRow(Pattern.compile("^Payment$"))
        val paymentDateRow = getRow("Payment date")
        val renewalDateRow = getRow("Renewal date")
    }
}
