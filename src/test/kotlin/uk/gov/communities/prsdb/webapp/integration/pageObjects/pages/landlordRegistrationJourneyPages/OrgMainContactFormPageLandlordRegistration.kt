package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgMainContactStep

// TODO: PDJB-1167 - Update this placeholder page object once the organisation main contact step is implemented
class OrgMainContactFormPageLandlordRegistration(
    page: Page,
) : BasePage(page, "$LANDLORD_REGISTRATION_ROUTE/${OrgMainContactStep.ROUTE_SEGMENT}") {
    val form = PostForm(page)
}
