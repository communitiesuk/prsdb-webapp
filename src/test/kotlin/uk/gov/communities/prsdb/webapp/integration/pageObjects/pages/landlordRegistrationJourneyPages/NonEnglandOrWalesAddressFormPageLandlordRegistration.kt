package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.NonEnglandOrWalesAddressStep

class NonEnglandOrWalesAddressFormPageLandlordRegistration(
    page: Page,
) : BasePage(
        page,
        "${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/${NonEnglandOrWalesAddressStep.ROUTE_SEGMENT}",
    ) {
    val heading = Heading.default(page)
}
