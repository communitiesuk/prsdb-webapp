package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckGasSafetyAnswersStep

class CheckGasSafetyAnswersFormPagePropertyRegistration(
    page: Page,
) : BasePage(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${CheckGasSafetyAnswersStep.ROUTE_SEGMENT}") {
    val heading = Heading(page.locator("h1"))
    val form = Form(page)
    val gasSupplySummaryList = GasSupplySummaryList(page)
    val certSummaryList = CertSummaryList(page)

    class GasSupplySummaryList(
        page: Page,
    ) : SummaryList(page, 0) {
        val gasSupplyRow = getRow("Does the property have a gas supply or any gas appliances?")
        val gasCertRow = getRow("Do you have a gas safety certificate for this property?")
    }

    class CertSummaryList(
        page: Page,
    ) : SummaryList(page, 1) {
        val validGasCertRow = getRow("Do you have a valid gas safety certificate for this property?")
        val issueDateRow = getRow("Issue date")
        val yourCertificateRow = getRow("Your certificate")
    }
}
