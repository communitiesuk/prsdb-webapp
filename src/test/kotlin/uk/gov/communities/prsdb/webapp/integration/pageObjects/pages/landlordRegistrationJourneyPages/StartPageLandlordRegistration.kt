package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class StartPageLandlordRegistration(
    page: Page,
) : BasePage(page, "/$REGISTER_LANDLORD_JOURNEY_URL") {
    val heading: Heading = Heading(page.locator("main h1"))
    val startButton = Button.byText(page, "Start now")
}
