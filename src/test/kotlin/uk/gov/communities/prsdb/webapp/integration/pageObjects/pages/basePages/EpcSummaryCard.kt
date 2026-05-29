package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryCard
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList

class EpcSummaryCard(
    page: Page,
    title: String,
) : SummaryCard(page, title) {
    override val summaryList = EpcSummaryList(locator)
}

class EpcSummaryList(
    locator: Locator,
) : SummaryList(locator, index = 0) {
    val hasEpcRow = getRow("Do you have an EPC")
    val isEpcRequiredRow = getRow("Is an EPC required")
    val epcExemptionRow = getRow("Why does this property not need an EPC")
    val certificateStatusRow = getRow("Certificate status")
    val energyRatingRow = getRow("Energy rating")
    val expiryDateRow = getRow("Expiry date")
    val certificateNumberRow = getRow("Certificate number")
}
