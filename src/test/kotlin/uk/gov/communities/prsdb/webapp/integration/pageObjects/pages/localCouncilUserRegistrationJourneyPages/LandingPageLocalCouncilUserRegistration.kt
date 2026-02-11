package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.localCouncilUserRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLocalCouncilUserController
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLocalCouncilUserStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class LandingPageLocalCouncilUserRegistration(
    page: Page,
) : BasePage(
        page,
        "${RegisterLocalCouncilUserController.LOCAL_COUNCIL_USER_REGISTRATION_ROUTE}" +
            "/${RegisterLocalCouncilUserStepId.LandingPage.urlPathSegment}",
    ) {
    fun clickBeginButton() = PostForm(page).submit()

    val headingCaption = page.locator(".govuk-caption-l")
    val heading = page.locator(".govuk-heading-l")
}
