package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class ConfirmIdentityFormPageLandlordRegistration(
    page: Page,
) : BasePage(page, "/$REGISTER_LANDLORD_JOURNEY_URL/${LandlordRegistrationStepId.ConfirmIdentity.urlPathSegment}") {
    val form = FormWithSectionHeader(page)

    fun confirm() = form.submit()
}
