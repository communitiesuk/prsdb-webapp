package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Assertions.assertTrue
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.SearchLandlordRegisterPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.SearchPropertyRegisterPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import kotlin.test.Test

class LocalAuthorityDashboardTests : SinglePageTestWithSeedData("data-local.sql") {
    @Test
    fun `the dashboard loads displaying the user's name and local authority`(page: Page) {
        val dashboard = navigator.goToLocalAuthorityDashboard()
        assertThat(dashboard.bannerHeading).containsText("Mock User")
        assertThat(dashboard.bannerSubHeading).containsText("ISLE OF MAN")
    }

    @Test
    fun `the search for a property button links to the property search page`(page: Page) {
        val dashboard = navigator.goToLocalAuthorityDashboard()
        dashboard.searchPropertyButton.clickAndWait()
        assertPageIs(page, SearchPropertyRegisterPage::class)
    }

    @Test
    fun `the search for a landlord button links to the property search page`(page: Page) {
        val dashboard = navigator.goToLocalAuthorityDashboard()
        dashboard.searchLandlordButton.clickAndWait()
        assertPageIs(page, SearchLandlordRegisterPage::class)
    }

    @Test
    fun `the renters rights bill link goes to an external page`(page: Page) {
        val dashboard = navigator.goToLocalAuthorityDashboard()
        dashboard.rentersRightsBillLink.clickAndWait()
        assertTrue(page.url().contains("https://www.gov.uk/government/publications/guide-to-the-renters-rights-bill"))
    }
}
