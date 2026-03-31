package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateLandlordPhoneNumberController.Companion.UPDATE_PHONE_NUMBER_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.PhoneNumberFormPage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.PhoneNumberStep

class PhoneNumberFormPageUpdateLandlordDetails(
    page: Page,
) : PhoneNumberFormPage(
        page,
        "$UPDATE_PHONE_NUMBER_ROUTE/${PhoneNumberStep.ROUTE_SEGMENT}",
    )
