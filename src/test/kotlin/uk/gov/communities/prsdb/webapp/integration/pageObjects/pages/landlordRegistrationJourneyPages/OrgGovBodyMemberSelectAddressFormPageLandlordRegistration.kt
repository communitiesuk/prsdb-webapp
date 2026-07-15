package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.SelectAddressFormPage
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.GovBodyMemberAddressTask

class OrgGovBodyMemberSelectAddressFormPageLandlordRegistration(
    page: Page,
) : SelectAddressFormPage(
        page,
        "$LANDLORD_REGISTRATION_ROUTE/${GovBodyMemberAddressTask.GOV_BODY_MEMBER_ADDRESS_ROUTE_SEGMENT}/${SelectAddressStep.ROUTE_SEGMENT}",
    ) {
    val heading = page.locator("h1")
}
