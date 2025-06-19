package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextArea
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class NonEnglandOrWalesAddressFormPageLandlordRegistration(
    page: Page,
) : BasePage(
        page,
        "${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/${LandlordRegistrationStepId.NonEnglandOrWalesAddress.urlPathSegment}",
    ) {
    val form = InternationalAddressFormLandlord(page)

    fun submitAddress(address: String) {
        form.textAreaInput.fill(address)
        form.submit()
    }

    class InternationalAddressFormLandlord(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val textAreaInput = TextArea.default(locator)
    }
}
