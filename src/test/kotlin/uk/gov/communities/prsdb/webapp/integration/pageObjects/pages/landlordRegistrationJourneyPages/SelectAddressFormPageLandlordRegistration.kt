package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.SelectAddressFormPage

class SelectAddressFormPageLandlordRegistration(
    page: Page,
) : SelectAddressFormPage(
        page,
        "/$REGISTER_LANDLORD_JOURNEY_URL/${LandlordRegistrationStepId.SelectAddress.urlPathSegment}",
    )
