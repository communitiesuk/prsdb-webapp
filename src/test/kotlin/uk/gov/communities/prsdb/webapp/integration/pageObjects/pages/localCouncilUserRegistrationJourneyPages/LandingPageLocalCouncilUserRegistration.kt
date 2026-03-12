package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.localCouncilUserRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLocalCouncilUserController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.localCouncilUserRegistration.stepConfig.LandingPageStep

class LandingPageLocalCouncilUserRegistration(
    page: Page,
) : BasePage(
        page,
        RegisterLocalCouncilUserController.LOCAL_COUNCIL_USER_REGISTRATION_ROUTE +
            "/${LandingPageStep.ROUTE_SEGMENT}",
    ) {
    fun clickBeginButton() = PostForm(page).submit()

    val headingCaption = page.locator(".govuk-caption-l")
    val heading = page.locator(".govuk-heading-l")
}
