package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.LandlordDetailsBasePage

class SystemOperatorViewLandlordDetailsPage(
    page: Page,
    urlArguments: Map<String, String>,
) : LandlordDetailsBasePage(page, LandlordDetailsController.getLandlordDetailsForSystemOperatorPath(urlArguments["id"]!!.toLong())) {
    override val expectedTitleHeading
        get() = "Landlord record for ${page.locator("h1").first().textContent().trim()}"
}
