package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextArea
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class NonEnglandOrWalesAddressFormPageLandlordRegistration(
    page: Page,
) : BasePage(
        page,
        "/$REGISTER_LANDLORD_JOURNEY_URL/${LandlordRegistrationStepId.NonEnglandOrWalesAddress.urlPathSegment}",
    ) {
    val form = InternationalAddressFormLandlord(page)

    fun submitAddress(address: String) {
        form.textAreaInput.fill(address)
        form.submit()
    }

    class InternationalAddressFormLandlord(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val textAreaInput = TextArea(locator)
    }
}
