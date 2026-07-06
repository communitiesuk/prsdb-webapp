package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.EmailFormPage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgEmailStep

class OrgEmailFormPageLandlordRegistration(
    page: Page,
) : EmailFormPage(page, "${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/${OrgEmailStep.ROUTE_SEGMENT}")
