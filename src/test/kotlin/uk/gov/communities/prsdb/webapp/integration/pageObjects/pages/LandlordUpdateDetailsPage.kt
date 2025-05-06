package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.DETAILS_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.UPDATE_LANDLORD_DETAILS_URL
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.LandlordDetailsBasePage

class LandlordUpdateDetailsPage(
    page: Page,
) : LandlordDetailsBasePage(
        page,
        "$UPDATE_LANDLORD_DETAILS_URL/$DETAILS_PATH_SEGMENT",
    ) {
    val heading = Heading(page.locator("html").locator("main div.moj-page-header-actions h1.govuk-heading-l"))
    val submitButton = Button.byText(page, "TODO: PRSD-355 Confirmation page for update landlord details")
}
