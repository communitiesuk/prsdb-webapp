package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Table
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Tabs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class OrgLandlordDetailsPage(
    page: Page,
) : BasePage(page, LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE) {
    val backLink = BackLink.default(page)
    val tabs = OrgLandlordDetailsTabs(page)
    val deleteOrganisationButton = Button.byText(page, "Delete organisation")
    val registeredPropertiesTable = Table(page)

    class OrgLandlordDetailsTabs(
        page: Page,
    ) : Tabs(page) {
        fun goToOrganisationContacts() = goToTab("Organisation contacts")

        fun goToRegisteredProperties() = goToTab("Registered properties")
    }
}
