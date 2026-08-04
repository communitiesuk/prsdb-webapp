package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.REGISTERED_PROPERTIES_FRAGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import kotlin.test.assertEquals

class OrgLandlordDetailTests : IntegrationTestWithImmutableData("data-mockuser-organisation-landlord.sql") {
    @Test
    fun `the org landlord details page loads with the organisation name, details tab selected and a delete organisation link`(page: Page) {
        val detailsPage = navigator.goToOrgLandlordDetails()

        assertThat(page.locator("h1")).containsText("Keystone Living Group")
        assertThat(detailsPage.deleteOrganisationLink).isVisible()
        assertEquals("organisation-details", detailsPage.tabs.activeTabPanelId)
    }

    @Test
    fun `the organisation details tab shows the organisation's details`(page: Page) {
        navigator.goToOrgLandlordDetails()

        assertThat(page.getByText("3rd Floor, 88 Kingsway Square, London, ZX1 4QP")).isVisible()
        assertThat(page.getByText("hello@keystoneliving.co.uk")).isVisible()
        assertThat(page.getByText("020 7123 4567")).isVisible()
        assertThat(page.getByText("Companies House number")).isVisible()
        assertThat(page.getByText("01234567")).isVisible()
    }

    @Test
    fun `the organisation contacts tab shows the main contact's details`(page: Page) {
        val detailsPage = navigator.goToOrgLandlordDetails()

        detailsPage.tabs.goToOrganisationContacts()

        assertThat(page.getByText("Main contact")).isVisible()
        assertThat(page.getByText("Jane Doe")).isVisible()
        assertThat(page.getByText("jane.doe@keystoneliving.co.uk")).isVisible()
        assertThat(page.getByText("020 7123 4568")).isVisible()
    }

    @Test
    fun `the org landlord details page has organisation details, contacts and registered properties tabs`(page: Page) {
        val detailsPage = navigator.goToOrgLandlordDetails()

        assertEquals(3, detailsPage.tabs.tabsList.count())

        detailsPage.tabs.goToRegisteredProperties()
        assertEquals(REGISTERED_PROPERTIES_FRAGMENT, detailsPage.tabs.activeTabPanelId)
    }

    @Test
    fun `clicking each tab activates the corresponding panel`(page: Page) {
        val detailsPage = navigator.goToOrgLandlordDetails()

        assertEquals("organisation-details", detailsPage.tabs.activeTabPanelId)

        detailsPage.tabs.goToOrganisationContacts()
        assertEquals("organisation-contacts", detailsPage.tabs.activeTabPanelId)

        detailsPage.tabs.goToRegisteredProperties()
        assertEquals(REGISTERED_PROPERTIES_FRAGMENT, detailsPage.tabs.activeTabPanelId)

        detailsPage.tabs.goToOrganisationDetails()
        assertEquals("organisation-details", detailsPage.tabs.activeTabPanelId)
    }
}
