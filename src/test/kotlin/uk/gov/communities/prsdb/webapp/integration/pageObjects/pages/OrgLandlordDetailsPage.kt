package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.LandlordDetailsBasePage
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationLandlordNameController.Companion.UPDATE_ORG_NAME_ROUTE

class OrgLandlordDetailsPage(
    page: Page,
) : LandlordDetailsBasePage(page, LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE) {
    override val tabs = OrgLandlordDetailsTabs(page)
    val deleteOrganisationLink = Link.byText(page, "Delete organisation")
    val organisationDetailsPanel: Locator = page.locator("#organisation-details")
    val organisationContactsPanel: Locator = page.locator("#organisation-contacts")
    val mainContent: Locator = page.locator("main")
    private val organisationNameChangeLink = Link(page.locator("a[href='$UPDATE_ORG_NAME_ROUTE/organisation-name']"))

    fun clickOrganisationNameChangeLinkAndWait() = organisationNameChangeLink.clickAndWait()

    class OrgLandlordDetailsTabs(
        page: Page,
    ) : LandlordDetailsTabs(page) {
        fun goToOrganisationDetails() = goToTab("Organisation details")

        fun goToOrganisationContacts() = goToTab("Organisation contacts")
    }
}
