package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader.SectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList

open class CheckElectricalSafetyAnswersFormBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val heading = Heading(page.locator("h1"))
    val sectionHeader = SectionHeader(page.locator("main"))
    val form = Form(page)
    val summaryList = ElectricalSafetySummaryList(page)
}

class ElectricalSafetySummaryList(
    page: Page,
) : SummaryList(page, 0) {
    val electricalCertRow = getRow("Which electrical safety certificate do you have for this property?")
    val expiryDateRow = getRow("Expiry date")
    val yourCertificateRow = getRow("Your certificate")
}
