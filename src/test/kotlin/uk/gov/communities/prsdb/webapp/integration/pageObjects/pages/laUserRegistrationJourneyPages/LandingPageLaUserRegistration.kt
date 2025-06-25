package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLAUserController
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLaUserStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class LandingPageLaUserRegistration(
    page: Page,
) : BasePage(page, "${RegisterLAUserController.LA_USER_REGISTRATION_ROUTE}/${RegisterLaUserStepId.LandingPage.urlPathSegment}") {
    fun clickBeginButton() = PostForm(page).submit()

    val headingCaption = page.locator(".govuk-caption-l")
    val heading = page.locator(".govuk-heading-l")
}
