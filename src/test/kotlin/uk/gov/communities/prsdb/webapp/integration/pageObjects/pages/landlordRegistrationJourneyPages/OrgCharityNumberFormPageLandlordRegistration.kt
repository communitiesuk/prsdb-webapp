package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityNumberStep

// TODO: PDJB-1142 - Update this placeholder page object once the organisation charity number step is implemented
class OrgCharityNumberFormPageLandlordRegistration(
    page: Page,
) : BasePage(page, "$LANDLORD_REGISTRATION_ROUTE/${OrgCharityNumberStep.ROUTE_SEGMENT}") {
    val form = PostForm(page)
}
