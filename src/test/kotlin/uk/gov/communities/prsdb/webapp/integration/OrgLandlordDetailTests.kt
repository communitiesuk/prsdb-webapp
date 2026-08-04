package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.REGISTERED_PROPERTIES_FRAGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import kotlin.test.assertEquals

@WithOrgLandlordProfile
class OrgLandlordDetailTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Test
    fun `the org landlord details page loads with the organisation name, details tab selected and a delete organisation link`(page: Page) {
        val detailsPage = navigator.goToOrgLandlordDetails()

        assertThat(page.locator("#main-content h1")).containsText("Local Organisation Landlord")
        assertThat(detailsPage.deleteOrganisationLink).isVisible()
        assertEquals("organisation-details", detailsPage.tabs.activeTabPanelId)
    }

    @Test
    fun `the organisation details tab shows the organisation's details`(page: Page) {
        navigator.goToOrgLandlordDetails()

        assertThat(page.getByText("5 Mythical Place")).isVisible()
        assertThat(page.getByText("local-org-landlord@example.com")).isVisible()
        assertThat(page.getByText("07111111111")).isVisible()
        assertThat(page.getByText("Companies House number")).isVisible()
        assertThat(page.getByText("12345678")).isVisible()
    }

    @Test
    fun `the organisation contacts tab shows the main contact's details`(page: Page) {
        val detailsPage = navigator.goToOrgLandlordDetails()

        detailsPage.tabs.goToOrganisationContacts()

        assertThat(page.getByText("Main contact")).isVisible()
        assertThat(page.getByText("Local Main Contact")).isVisible()
        assertThat(page.getByText("local-main-contact@example.com")).isVisible()
        assertThat(page.getByText("07111111113")).isVisible()
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
