package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Checkboxes
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipKind
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipKindStep

class OwnershipKindFormPagePropertyRegistration(
    page: Page,
) : BasePage(page, "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/${OwnershipKindStep.ROUTE_SEGMENT}") {
    val form = OwnershipKindForm(page)
    val shareOfFreeholdDetails: Locator = page.locator("details.govuk-details")

    fun submitOwnershipKind(ownershipKind: OwnershipKind) {
        ownershipKind.options.forEach { form.checkboxes.checkCheckbox(it.name) }
        form.submit()
    }

    class OwnershipKindForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val checkboxes = Checkboxes(locator)
    }
}
