package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader.SectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList

open class CheckGasSafetyAnswersFormBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val heading = Heading(page.locator("h1"))
    val sectionHeader = SectionHeader(page.locator("main"))
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
