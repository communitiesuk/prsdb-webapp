package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LA_USER_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLaUserStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.JourneyForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class CheckAnswersPageLaUserRegistration(
    page: Page,
) : BasePage(page, "/$REGISTER_LA_USER_JOURNEY_URL/${RegisterLaUserStepId.CheckAnswers.urlPathSegment}") {
    val form = CheckAnswersLaUserRegistrationForm(page)

    class CheckAnswersLaUserRegistrationForm(
        page: Page,
    ) : JourneyForm(page) {
        val summaryList = CheckAnswersLaUserRegistrationSummaryList(locator)
    }

    class CheckAnswersLaUserRegistrationSummaryList(
        locator: Locator,
    ) : SummaryList(locator) {
        val nameRow = getRow("Name")
        val emailRow = getRow("Email address")
    }
}
