package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.InsetText
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.PropertyDetailsBasePage

class PropertyDetailsPageLocalAuthorityView(
    page: Page,
) : PropertyDetailsBasePage(page, "/local-authority/property-details") {
    val insetText = InsetText(page)
}
