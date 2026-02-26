package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Select
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LocalCouncilStep

class SelectLocalCouncilFormPagePropertyRegistration(
    page: Page,
) : BasePage(
        page,
        "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${LocalCouncilStep.ROUTE_SEGMENT}",
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
