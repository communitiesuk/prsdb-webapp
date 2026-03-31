package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.controllers.LocalCouncilDashboardController.Companion.LOCAL_COUNCIL_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent

class LocalCouncilStartPageTests : IntegrationTestWithImmutableData("data-mockuser-not-landlord.sql") {
    @Test
    fun `the local council start page renders`(page: Page) {
        val startPage = navigator.goToLocalCouncilStartPage()
        BaseComponent
            .assertThat(startPage.heading)
            .containsText("Check a rental property or landlord: private beta")
    }

    @Test
    fun `the start now button links to the local council dashboard`(page: Page) {
        val startPage = navigator.goToLocalCouncilStartPage()
        BaseComponent
            .assertThat(startPage.startButton)
            .hasAttribute("href", LOCAL_COUNCIL_DASHBOARD_URL)
    }
}
