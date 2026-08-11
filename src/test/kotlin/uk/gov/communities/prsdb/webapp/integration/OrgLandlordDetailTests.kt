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
    fun `the organisation contacts tab shows the main contact's details`(page: Page) {
        val detailsPage = navigator.goToOrgLandlordDetails()

        detailsPage.tabs.goToOrganisationContacts()

        assertThat(detailsPage.organisationContactsPanel).containsText("Main contact")
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

    @Test
    fun `the organisation details tab shows the organisation's registration and organisation type details`(page: Page) {
        val detailsPage = navigator.goToOrgLandlordDetails()
        val summaryList = detailsPage.organisationDetailsSummaryList

        assertThat(summaryList.lrnRow.value).containsText("L-")
        assertThat(summaryList.landlordTypeRow.value).containsText("Organisation")
        assertThat(summaryList.nameRow.value).containsText("Local Organisation Landlord")
        assertThat(summaryList.addressRow.value).containsText("FA1 1AE")
        assertThat(summaryList.emailRow.value).containsText("local-org-landlord@example.com")
        assertThat(summaryList.phoneRow.value).containsText("07111111111")
        assertThat(summaryList.organisationTypeRow.value).containsText("Company")
        assertThat(summaryList.registeredCharityRow.value).containsText("No")
        assertThat(summaryList.registeredWithCompaniesHouseRow.value).containsText("Yes")
        assertThat(summaryList.companyNumberRow.value).containsText("12345678")
    }
}
