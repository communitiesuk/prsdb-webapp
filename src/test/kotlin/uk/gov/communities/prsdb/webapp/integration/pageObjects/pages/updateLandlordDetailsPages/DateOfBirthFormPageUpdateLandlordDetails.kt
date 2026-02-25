package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateLandlordDateOfBirthController.Companion.UPDATE_DATE_OF_BIRTH_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.DateFormPage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.dateOfBirth.UpdateDateOfBirthStep

class DateOfBirthFormPageUpdateLandlordDetails(
    page: Page,
) : DateFormPage(page, "$UPDATE_DATE_OF_BIRTH_ROUTE/${UpdateDateOfBirthStep.ROUTE_SEGMENT}")
