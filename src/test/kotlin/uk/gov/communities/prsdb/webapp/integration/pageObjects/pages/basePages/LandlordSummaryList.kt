package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList

class LandlordSummaryList(
    page: Page,
) : SummaryList(page) {
    val nameRow = getRow("Name")
    val dateOfBirthRow = getRow("Date of Birth")
    val verifiedByOneLoginRow = getRow("Verified by GOV.UK One Login")
    val emailRow = getRow("Email address")
    val contactNumberRow = getRow("Contact number")
    val contactAddressRow = getRow("Contact address")
}
