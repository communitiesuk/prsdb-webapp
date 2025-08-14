package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.RegisterLAUserController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class ConfirmationPageLaUserRegistration(
    page: Page,
) : BasePage(page, "${RegisterLAUserController.LA_USER_REGISTRATION_ROUTE}/$CONFIRMATION_PATH_SEGMENT") {
    val bannerHeading = Heading(page.locator(".govuk-panel__title"))
    val bodyHeading = Heading(page.locator(".govuk-heading-xl"))
    val returnToDashboardButton = Button.byText(page, "Go to dashboard")
}
