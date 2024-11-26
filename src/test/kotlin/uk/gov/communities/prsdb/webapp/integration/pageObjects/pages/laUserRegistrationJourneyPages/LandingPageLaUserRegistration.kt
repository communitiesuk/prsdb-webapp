package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LA_USER_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLaUserStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.getSubmitButton
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class LandingPageLaUserRegistration(
    page: Page,
) : BasePage(page, "/$REGISTER_LA_USER_JOURNEY_URL/${RegisterLaUserStepId.LandingPage.urlPathSegment}") {
    private val beginButton = getSubmitButton(page)

    fun clickBeginAndAssertNextPage(): NameFormPageLaUserRegistration = clickElementAndAssertNextPage(beginButton)
}
