package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.FormBasePage

class OutsideEnglandOrWalesAddressFormPageLandlordRegistration(
    page: Page,
) : FormBasePage(
        page,
        "/$REGISTER_LANDLORD_JOURNEY_URL/${LandlordRegistrationStepId.OutsideEnglandOrWalesAddress.urlPathSegment}",
    ) {
    val textAreaInput = form.getTextArea()
}
