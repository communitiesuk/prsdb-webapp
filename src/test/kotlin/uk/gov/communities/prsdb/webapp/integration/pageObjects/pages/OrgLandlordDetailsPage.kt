package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController.Companion.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationLandlordNameController.Companion.UPDATE_ORG_NAME_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class OrgLandlordDetailsPage(
    page: Page,
) : BasePage(page, LANDLORD_DETAILS_FOR_LANDLORD_ROUTE) {
    val mainContent: Locator = page.locator("main")
    private val organisationNameChangeLink = Link(page.locator("a[href='$UPDATE_ORG_NAME_ROUTE/organisation-name']"))

    fun clickOrganisationNameChangeLinkAndWait() = organisationNameChangeLink.clickAndWait()
}
