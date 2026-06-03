package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryCard
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList

class ElectricalSafetySummaryCard(
    page: Page,
    title: String,
) : SummaryCard(page, title) {
    override val summaryList = ElectricalSafetySummaryList(locator)
}

class ElectricalSafetySummaryList(
    locator: Locator,
) : SummaryList(locator) {
    val whichCertificateRow = getRow("Which electrical safety certificate")
    val certificateStatusRow = getRow("Certificate status")
    val expiryDateRow = getRow("Expiry date")
    val yourCertificateRow = getRow("Your certificate")
}
