package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckElectricalSafetyAnswersStep

class CheckElectricalSafetyAnswersFormPagePropertyRegistration(
    page: Page,
) : BasePage(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${CheckElectricalSafetyAnswersStep.ROUTE_SEGMENT}") {
    val heading = Heading(page.locator("h1"))
    val form = Form(page)
    val summaryList = ElectricalSafetySummaryList(page)

    class ElectricalSafetySummaryList(
        page: Page,
    ) : SummaryList(page, 0) {
        val electricalCertRow = getRow("Which electrical safety certificate do you have for this property?")
        val expiryDateRow = getRow("Expiry date")
        val yourCertificateRow = getRow("Your certificate")
    }
}
