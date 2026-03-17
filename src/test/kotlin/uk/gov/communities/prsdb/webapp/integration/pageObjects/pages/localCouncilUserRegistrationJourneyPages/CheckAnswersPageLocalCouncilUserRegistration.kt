package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.localCouncilUserRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLocalCouncilUserController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.localCouncilUserRegistration.stepConfig.LocalCouncilUserCyaStep

class CheckAnswersPageLocalCouncilUserRegistration(
    page: Page,
) : BasePage(
        page,
        RegisterLocalCouncilUserController.LOCAL_COUNCIL_USER_REGISTRATION_ROUTE +
            "/${LocalCouncilUserCyaStep.ROUTE_SEGMENT}",
    ) {
    val form = PostForm(page)

    val heading = Heading(page.locator("h1"))

    val summaryList = CheckAnswersLocalCouncilUserRegistrationSummaryList(page)

    class CheckAnswersLocalCouncilUserRegistrationSummaryList(
        page: Page,
    ) : SummaryList(page) {
        val nameRow = getRow("Name")
        val emailRow = getRow("Email address")
    }
}
