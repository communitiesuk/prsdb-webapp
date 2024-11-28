package uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.pages.laUserRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LA_USER_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLaUserStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.pages.basePages.BasePage

class LandingPageLaUserRegistration(
    page: Page,
) : BasePage(page, "/$REGISTER_LA_USER_JOURNEY_URL/${RegisterLaUserStepId.LandingPage.urlPathSegment}") {
    fun clickBeginButton() = Form(page).submit()

    val headingCaption = page.locator(".govuk-caption-l")
    val heading = page.locator(".govuk-heading-l")
}
