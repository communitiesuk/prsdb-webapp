package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.DateFormPage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.DateOfBirthStep

class DateOfBirthFormPageLandlordRegistration(
    page: Page,
) : DateFormPage(page, "${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/${DateOfBirthStep.ROUTE_SEGMENT}")
