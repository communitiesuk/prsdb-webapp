package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.InsetText
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.LandlordDetailsBasePage

class LocalAuthorityViewLandlordDetailsPage(
    page: Page,
) : LandlordDetailsBasePage(page, "/landlord-details") {
    val insetText = InsetText(page)
}
