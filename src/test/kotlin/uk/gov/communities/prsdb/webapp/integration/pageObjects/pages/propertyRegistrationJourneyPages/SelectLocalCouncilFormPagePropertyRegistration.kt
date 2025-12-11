package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.NewRegisterPropertyController
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Select
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class SelectLocalCouncilFormPagePropertyRegistration(
    page: Page,
) : BasePage(
        page,
        "${NewRegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${RegisterPropertyStepId.LocalCouncil.urlPathSegment}",
    ) {
    val form = SelectLocalCouncilForm(page)

    fun submitLocalCouncil(
        partialName: String,
        fullName: String,
    ) {
        form.laSelect.fillPartialAndSelectValue(partialName, fullName)
        form.submit()
    }

    class SelectLocalCouncilForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val laSelect = Select(locator)
    }
}
