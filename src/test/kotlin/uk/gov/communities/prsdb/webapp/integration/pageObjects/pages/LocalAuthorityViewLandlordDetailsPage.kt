package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.InsetText
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.LandlordDetailsBasePage

class LocalAuthorityViewLandlordDetailsPage(
    page: Page,
) : LandlordDetailsBasePage(page, LandlordDetailsController.LANDLORD_DETAILS_ROUTE) {
    val insetText = InsetText(page)
    val backLink = BackLink.default(page)
}
