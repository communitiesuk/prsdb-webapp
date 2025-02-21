package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Select
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class SelectLocalAuthorityFormPagePropertyRegistration(
    page: Page,
) : BasePage(
        page,
        "/$REGISTER_PROPERTY_JOURNEY_URL/${RegisterPropertyStepId.LocalAuthority.urlPathSegment}",
    ) {
    val form = SelectLocalAuthorityForm(page)

    fun submitLocalAuthority(
        partialName: String,
        fullName: String,
    ) {
        form.laSelect.fillPartialAndSelectValue(partialName, fullName)
        form.submit()
    }

    class SelectLocalAuthorityForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val laSelect = Select(locator)
    }
}
