package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.SelectAddressFormPage

class SelectContactAddressFormPageLandlordRegistration(
    page: Page,
) : SelectAddressFormPage(
        page,
        "${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/${LandlordRegistrationStepId.SelectContactAddress.urlPathSegment}",
    )
