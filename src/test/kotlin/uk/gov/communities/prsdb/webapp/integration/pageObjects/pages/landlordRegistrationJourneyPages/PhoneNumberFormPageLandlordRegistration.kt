package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class PhoneNumberFormPageLandlordRegistration(
    page: Page,
) : BasePage(page, "/$REGISTER_LANDLORD_JOURNEY_URL/${LandlordRegistrationStepId.PhoneNumber.urlPathSegment}") {
    val form = PhoneNumberFormLandlord(page)

    fun submitPhoneNumber(phoneNumber: String) {
        form.phoneNumberInput.fill(phoneNumber)
        form.submit()
    }

    class PhoneNumberFormLandlord(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val phoneNumberInput = TextInput.textByFieldName(locator, "phoneNumber")
    }
}
