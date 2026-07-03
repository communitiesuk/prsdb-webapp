package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeNameStep

class LeadTrusteeNameFormPageLandlordRegistration(
    page: Page,
) : BasePage(page, "$LANDLORD_REGISTRATION_ROUTE/${LeadTrusteeNameStep.ROUTE_SEGMENT}") {
    val form = LeadTrusteeNameForm(page)

    fun submitName(name: String) {
        form.trusteeNameInput.fill(name)
        form.submit()
    }

    class LeadTrusteeNameForm(
        page: Page,
    ) : PostForm(page) {
        val trusteeNameInput = TextInput.textByFieldName(locator, "name")
    }
}
