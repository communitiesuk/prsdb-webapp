package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLaUserStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class CheckAnswersPageLandlordRegistration(
    page: Page,
) : BasePage(page, "/$REGISTER_LANDLORD_JOURNEY_URL/${RegisterLaUserStepId.CheckAnswers.urlPathSegment}") {
    fun confirm() = form.submit()

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
