package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.getButton
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.PropertyDetailsBasePage

class PropertyDetailsPageLandlordView(
    page: Page,
) : PropertyDetailsBasePage(page, "/property-details") {
    val deleteButton = getButton(page, "Delete property record")
}
