package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.REGISTERED_PROPERTIES_FRAGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import kotlin.test.assertEquals

class OrgLandlordDetailTests : IntegrationTestWithImmutableData("data-mockuser-organisation-landlord.sql") {
    @Test
    fun `the org landlord details page loads with the organisation details tab selected and a delete organisation button`(page: Page) {
        val detailsPage = navigator.goToOrgLandlordDetails()

        assertThat(detailsPage.deleteOrganisationButton).isVisible()
        assertEquals("organisation-details", detailsPage.tabs.activeTabPanelId)
    }

    @Test
    fun `the org landlord details page has organisation details, contacts and registered properties tabs`(page: Page) {
        val detailsPage = navigator.goToOrgLandlordDetails()

        assertEquals(3, detailsPage.tabs.tabsList.count())

        detailsPage.tabs.goToRegisteredProperties()
        assertEquals(REGISTERED_PROPERTIES_FRAGMENT, detailsPage.tabs.activeTabPanelId)
    }
}
