package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.InsetText
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.LandlordDetailsBasePage

class LocalAuthorityViewLandlordDetailsPage(
    page: Page,
    urlArguments: Map<String, String>,
) : LandlordDetailsBasePage(page, LandlordDetailsController.getLandlordDetailsPath(urlArguments["id"]!!.toLong())) {
    val insetText = InsetText(page)
}
