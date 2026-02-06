package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.NameFormPage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.NameStep

class NameFormPageLandlordRegistration(
    page: Page,
) : NameFormPage(page, "${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/${NameStep.ROUTE_SEGMENT}")
