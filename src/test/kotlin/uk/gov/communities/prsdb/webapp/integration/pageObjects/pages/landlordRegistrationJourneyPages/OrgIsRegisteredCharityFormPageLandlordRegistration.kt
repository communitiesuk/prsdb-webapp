package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.OrgIsRegisteredCharityFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCharityStep

class OrgIsRegisteredCharityFormPageLandlordRegistration(
    page: Page,
) : OrgIsRegisteredCharityFormBasePage(
        page,
        "${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/${OrgIsRegisteredCharityStep.ROUTE_SEGMENT}",
    )
