package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLaUserStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SummaryList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.FormBasePage

class CheckAnswersPageLandlordRegistration(
    page: Page,
) : FormBasePage(page, "/$REGISTER_LANDLORD_JOURNEY_URL/${RegisterLaUserStepId.CheckAnswers.urlPathSegment}") {
    val summaryList = SummaryList(page)
}
