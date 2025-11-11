package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.laUserRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLocalCouncilUserController
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLocalCouncilUserStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class CheckAnswersPageLaUserRegistration(
    page: Page,
) : BasePage(
        page,
        "${RegisterLocalCouncilUserController.LA_USER_REGISTRATION_ROUTE}/${RegisterLocalCouncilUserStepId.CheckAnswers.urlPathSegment}",
    ) {
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
