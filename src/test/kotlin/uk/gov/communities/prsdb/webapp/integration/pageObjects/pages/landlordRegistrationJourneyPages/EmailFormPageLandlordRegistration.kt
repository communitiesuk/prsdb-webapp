package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.EmailFormPage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.EmailStep

class EmailFormPageLandlordRegistration(
    page: Page,
) : EmailFormPage(page, "${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/${EmailStep.ROUTE_SEGMENT}")
