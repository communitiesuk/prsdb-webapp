package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.organisationLandlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgEmailStep

// TODO: PDJB-1135 - Update this placeholder page object once the organisation email step is implemented
class OrganisationEmailPageLandlordRegistration(
    page: Page,
) : BasePage(page, "$LANDLORD_REGISTRATION_ROUTE/${OrgEmailStep.ROUTE_SEGMENT}") {
    val form = PostForm(page)
}
