package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLaUserStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class CheckAnswersPageLandlordRegistration(
    page: Page,
) : BasePage(page, "${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/${RegisterLaUserStepId.CheckAnswers.urlPathSegment}") {
    fun confirmAndSubmit() = form.submit()

    val form = CheckAnswersLandlordRegistration(page)

    class CheckAnswersLandlordRegistration(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val summaryList = CheckAnswersLandlordRegistrationSummaryList(locator)
    }

    class CheckAnswersLandlordRegistrationSummaryList(
        locator: Locator,
    ) : SummaryList(locator) {
        val emailRow = getRow("Email address")
    }
}
