package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.test.context.jdbc.Sql
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDetailsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages.RegisterPropertyStartPage
import kotlin.test.Test
import kotlin.test.assertEquals

@Sql("/data-local.sql")
class LandlordDashboardTests : IntegrationTest() {
    @Test
    fun `the dashboard loads displaying the user's name and lrn`() {
        val dashboard = navigator.goToLandlordDashboard()
        assertThat(dashboard.bannerHeading).containsText("Alexander Smith")
        assertThat(dashboard.bannerSubHeading).containsText("Landlord registration number")
        assertThat(dashboard.bannerSubHeading).containsText("L-CKSQ-3SX9")
    }

    @Test
    fun `the register property buttons links to the property registration journey`(page: Page) {
        val dashboard = navigator.goToLandlordDashboard()
        dashboard.registerPropertyButton.clickAndWait()
        assertPageIs(page, RegisterPropertyStartPage::class)
    }

    @Test
    fun `the view incomplete properties button links to the incomplete properties page`(page: Page) {
        val dashboard = navigator.goToLandlordDashboard()
        dashboard.viewIncompletePropertiesButton.clickAndWait()
        // TODO PRSD-1078 change the assert to check the page is the incomplete properties page
        assertTrue(page.url().contains("/landlord/incomplete-properties"))
    }

    @Test
    fun `the view property records button links to property records tab on the landlord details page`(page: Page) {
        val dashboard = navigator.goToLandlordDashboard()
        dashboard.viewPropertyRecordsButton.clickAndWait()
        val detailsPage = assertPageIs(page, LandlordDetailsPage::class)
        assertEquals("registered-properties", detailsPage.tabs.activeTabPanelId)
    }

    @Test
    fun `the view landlord record button links to the landlord details page`(page: Page) {
        val dashboard = navigator.goToLandlordDashboard()
        dashboard.viewLandlordRecordButton.clickAndWait()
        assertPageIs(page, LandlordDetailsPage::class)
    }

    @Test
    fun `the renters rights bill link goes to an external page`(page: Page) {
        val dashboard = navigator.goToLandlordDashboard()
        dashboard.rentersRightsBillLink.clickAndWait()
        assertTrue(page.url().contains("https://www.gov.uk/government/publications/guide-to-the-renters-rights-bill"))
    }
}
