package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.FormBasePage

class ManualContactAddressFormPageLandlordRegistration(
    page: Page,
) : FormBasePage(
        page,
        "/$REGISTER_LANDLORD_JOURNEY_URL/${LandlordRegistrationStepId.ManualContactAddress.urlPathSegment}",
    ) {
    val addressLineOneInput = form.getTextInput("addressLineOne")
    val addressLineTwoInput = form.getTextInput("addressLineTwo")
    val townOrCityInput = form.getTextInput("townOrCity")
    val countyInput = form.getTextInput("county")
    val postcodeInput = form.getTextInput("postcode")
}
