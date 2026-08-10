package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.OrgTypeFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTypeStep

class OrgTypeFormPageLandlordRegistration(
    page: Page,
) : OrgTypeFormBasePage(page, "${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/${OrgTypeStep.ROUTE_SEGMENT}")
