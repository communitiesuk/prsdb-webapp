package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.ManualAddressFormPage

class ManualContactAddressFormPageLandlordRegistration(
    page: Page,
) : ManualAddressFormPage(
        page,
        "/$REGISTER_LANDLORD_JOURNEY_URL/${LandlordRegistrationStepId.ManualContactAddress.urlPathSegment}",
    )
