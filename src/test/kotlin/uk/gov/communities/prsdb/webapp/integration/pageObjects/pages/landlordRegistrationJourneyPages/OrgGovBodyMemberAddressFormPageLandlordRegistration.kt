package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyMemberAddressStep

class OrgGovBodyMemberAddressFormPageLandlordRegistration(
    page: Page,
) : BasePage(page, "$LANDLORD_REGISTRATION_ROUTE/${OrgGovBodyMemberAddressStep.ROUTE_SEGMENT}") {
    val form = PostForm(page)
}
