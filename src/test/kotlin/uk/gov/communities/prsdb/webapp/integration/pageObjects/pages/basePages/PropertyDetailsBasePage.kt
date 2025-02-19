package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Tabs

abstract class PropertyDetailsBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val tabs = PropertyDetailsTabs(page)

    fun getLandlordNameLinkFromKeyDetails(landlordName: String) = Link.byText(page, landlordName, 0)

    fun getLandlordLinkFromLandlordDetails(landlordName: String) = Link.byText(page, landlordName, 1)

    val backLink = Link.byText(page, "Back")

    class PropertyDetailsTabs(
        page: Page,
    ) : Tabs(page) {
        fun goToLandlordDetails() {
            goToTab("Landlord details")
        }

        fun goToPropertyDetails() {
            goToTab("Property details")
        }
    }
}
