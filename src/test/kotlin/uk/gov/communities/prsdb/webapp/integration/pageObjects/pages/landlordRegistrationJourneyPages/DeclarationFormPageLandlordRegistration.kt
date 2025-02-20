package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LANDLORD_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.steps.LandlordRegistrationStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Checkboxes
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class DeclarationFormPageLandlordRegistration(
    page: Page,
) : BasePage(page, "$REGISTER_LANDLORD_JOURNEY_URL/${LandlordRegistrationStepId.Declaration.urlPathSegment}") {
    val form = DeclarationFormLandlord(page)

    fun agreeAndSubmit() {
        form.iAgreeCheckbox.check()
        form.submit()
    }

    class DeclarationFormLandlord(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val iAgreeCheckbox = Checkboxes(locator).getCheckbox("true")
    }
}
