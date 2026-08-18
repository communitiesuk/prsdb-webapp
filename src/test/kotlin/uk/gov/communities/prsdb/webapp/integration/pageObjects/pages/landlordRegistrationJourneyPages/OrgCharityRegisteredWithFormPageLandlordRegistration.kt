package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.OrgCharityRegisteredWithFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityRegisteredWithStep

class OrgCharityRegisteredWithFormPageLandlordRegistration(
    page: Page,
) : OrgCharityRegisteredWithFormBasePage(
        page,
        "${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/${OrgCharityRegisteredWithStep.ROUTE_SEGMENT}",
    )
