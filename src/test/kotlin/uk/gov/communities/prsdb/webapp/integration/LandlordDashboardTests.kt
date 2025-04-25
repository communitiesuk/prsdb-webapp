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
        assertThat(dashboard.dashboardBannerHeading).containsText("Alexander Smith")
        assertThat(dashboard.dashboardBannerSubHeading).containsText("Landlord registration number")
        assertThat(dashboard.dashboardBannerSubHeading).containsText("L-CKSQ-3SX9")
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

    @Test
    @Sql("/data-mockuser-landlord-with-one-incomplete-property.sql")
    fun `the dashboard loads with a notification banner and correct message when the landlord has one incomplete property`(page: Page) {
        val dashboard = navigator.goToLandlordDashboard()
        assertThat(dashboard.notificationBanner.title).containsText("Important")
        assertThat(dashboard.notificationBanner.subheading).containsText("You have 1 incomplete property: View incomplete properties")
    }

    @Test
    @Sql("/data-mockuser-landlord-with-incomplete-properties.sql")
    fun `the dashboard loads with a notification banner when the landlord has incomplete properties`(page: Page) {
        val dashboard = navigator.goToLandlordDashboard()
        assertThat(dashboard.notificationBanner.title).containsText("Important")
        assertThat(dashboard.notificationBanner.subheading).containsText("You have 2 incomplete properties: View incomplete properties")
    }

    @Test
    @Sql("/data-mockuser-landlord-with-incomplete-properties.sql")
    fun `the link in the notification banner redirects to the incomplete properties page`(page: Page) {
        val dashboard = navigator.goToLandlordDashboard()
        dashboard.notificationBanner.link.clickAndWait()
        // TODO PRSD-1078 change the assert to check the page is the incomplete properties page
        assertTrue(page.url().contains("/landlord/incomplete-properties"))
    }

    @Test
    @Sql("/data-mockuser-landlord-with-properties.sql")
    fun `the dashboard loads without a notification banner when the landlord has no incomplete properties`() {
        val dashboard = navigator.goToLandlordDashboard()
        assertThat(dashboard.notificationBanner).isHidden()
    }
}
