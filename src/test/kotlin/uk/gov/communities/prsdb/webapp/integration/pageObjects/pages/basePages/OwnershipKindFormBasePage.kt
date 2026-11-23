package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Checkboxes
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipKind

abstract class OwnershipKindFormBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = OwnershipKindForm(page)
    val backLink = BackLink.default(page)

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
