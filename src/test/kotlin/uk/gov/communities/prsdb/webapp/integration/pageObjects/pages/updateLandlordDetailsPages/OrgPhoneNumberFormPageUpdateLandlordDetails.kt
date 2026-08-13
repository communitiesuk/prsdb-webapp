package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationLandlordPhoneNumberController.Companion.UPDATE_ORG_PHONE_NUMBER_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.PhoneNumberFormPage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgPhoneNumberStep

class OrgPhoneNumberFormPageUpdateLandlordDetails(
    page: Page,
) : PhoneNumberFormPage(
        page,
        "$UPDATE_ORG_PHONE_NUMBER_ROUTE/${OrgPhoneNumberStep.ROUTE_SEGMENT}",
    )
