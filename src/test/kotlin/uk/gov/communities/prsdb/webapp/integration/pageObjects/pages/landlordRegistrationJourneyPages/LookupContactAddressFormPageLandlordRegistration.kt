package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.FormBasePage

class LookupContactAddressFormPageLandlordRegistration(
    page: Page,
) : FormBasePage(
        page,
        "/$REGISTER_LANDLORD_JOURNEY_URL/${LandlordRegistrationStepId.LookupContactAddress.urlPathSegment}",
    ) {
    val postcodeInput = form.getTextInput("postcode")

    fun getPostcodeError() = form.getErrorMessage(0)

    val houseNameOrNumberInput = form.getTextInput("houseNameOrNumber")

    fun getHouseNameOrNumberError() = form.getErrorMessage(1)
}
