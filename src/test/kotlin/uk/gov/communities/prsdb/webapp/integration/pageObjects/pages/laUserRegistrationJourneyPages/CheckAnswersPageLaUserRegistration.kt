package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLAUserController
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLaUserStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class CheckAnswersPageLaUserRegistration(
    page: Page,
) : BasePage(page, "${RegisterLAUserController.LA_USER_REGISTRATION_ROUTE}/${RegisterLaUserStepId.CheckAnswers.urlPathSegment}") {
    val form = PostForm(page)

    val heading = Heading(page.locator("h1"))

    val summaryList = CheckAnswersLaUserRegistrationSummaryList(page)

    class CheckAnswersLaUserRegistrationSummaryList(
        page: Page,
    ) : SummaryList(page) {
        val nameRow = getRow("Name")
        val emailRow = getRow("Email address")
    }
}
