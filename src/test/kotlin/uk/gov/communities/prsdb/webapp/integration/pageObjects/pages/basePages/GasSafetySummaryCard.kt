package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryCard
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList

class GasSafetySummaryCard(
    page: Page,
    title: String,
) : SummaryCard(page, title) {
    override val summaryList = GasSafetySummaryList(locator)
}

class GasSafetySummaryList(
    locator: Locator,
) : SummaryList(locator) {
    val hasGasSupplyRow = getRow("Does the property have a gas supply")
    val hasValidCertRow = getRow("Do you have a valid gas safety certificate")
    val certificateStatusRow = getRow("Certificate status")
    val issueDateRow = getRow("Issue date")
    val yourCertificateRow = getRow("Your certificate")
}
