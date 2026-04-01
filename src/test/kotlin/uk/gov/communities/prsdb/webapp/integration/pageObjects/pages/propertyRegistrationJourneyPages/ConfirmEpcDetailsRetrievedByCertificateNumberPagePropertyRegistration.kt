package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryCard
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.PageWithYesNoRadios
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ConfirmEpcDetailsRetrievedByCertificateNumberStep

class ConfirmEpcDetailsRetrievedByCertificateNumberPagePropertyRegistration(
    page: Page,
) : PageWithYesNoRadios(
        page,
        RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE +
            "/${ConfirmEpcDetailsRetrievedByCertificateNumberStep.ROUTE_SEGMENT}",
    ) {
    val heading = Heading(page.locator("h1"))
    val summaryCard = ConfirmEpcDetailsSummaryCard(page)

    class ConfirmEpcDetailsSummaryCard(
        page: Page,
    ) : SummaryCard(page) {
        override val summaryList = ConfirmEpcDetailsSummaryList(locator)

        class ConfirmEpcDetailsSummaryList(
            parentLocator: Locator,
        ) : SummaryList(parentLocator) {
            val addressRow = getRow("Address")
            val energyEfficiencyRatingRow = getRow("Energy efficiency rating")
            val expiryDateRow = getRow("Expiry date")
            val certificateNumberRow = getRow("Certificate number")
        }
    }
}
