package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.LookupAddressFormPage

class LookupContactAddressFormPageLandlordRegistration(
    page: Page,
) : LookupAddressFormPage(
        page,
        "/$REGISTER_LANDLORD_JOURNEY_URL/${LandlordRegistrationStepId.LookupContactAddress.urlPathSegment}",
    )
