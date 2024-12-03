package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.getLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.FormBasePage

class SelectAddressFormPageLandlordRegistration(
    page: Page,
) : FormBasePage(page, "/$REGISTER_LANDLORD_JOURNEY_URL/${LandlordRegistrationStepId.SelectAddress.urlPathSegment}") {
    val searchAgain = getLink(page, "Search Again")
    val radios = form.getRadios()
}
