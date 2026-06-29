package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.organisationLandlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgNameStep

// TODO: PDJB-1132 - Update this placeholder page object once the organisation name step is implemented
class OrganisationNamePageLandlordRegistration(
    page: Page,
) : BasePage(page, "$LANDLORD_REGISTRATION_ROUTE/${OrgNameStep.ROUTE_SEGMENT}") {
    val form = PostForm(page)
}
