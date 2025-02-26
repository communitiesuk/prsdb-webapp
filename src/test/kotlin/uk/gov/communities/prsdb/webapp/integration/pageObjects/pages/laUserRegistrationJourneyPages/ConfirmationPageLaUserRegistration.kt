package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LA_USER_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.controllers.RegisterLAUserController.Companion.CONFIRMATION_PAGE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class ConfirmationPageLaUserRegistration(
    page: Page,
) : BasePage(page, "/$REGISTER_LA_USER_JOURNEY_URL/$CONFIRMATION_PAGE_PATH_SEGMENT") {
    val bannerHeading = page.locator(".govuk-panel__title")
    val bodyHeading = page.locator(".govuk-heading-m")
    val returnToDashboardButton = Button.byText(page, "Go to dashboard")
}
