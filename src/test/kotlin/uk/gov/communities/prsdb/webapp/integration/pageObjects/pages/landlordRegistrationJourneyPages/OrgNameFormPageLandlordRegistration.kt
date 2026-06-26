package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.TextFormPage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgNameStep

class OrgNameFormPageLandlordRegistration(
    page: Page,
) : TextFormPage(page, "${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/${OrgNameStep.ROUTE_SEGMENT}")
