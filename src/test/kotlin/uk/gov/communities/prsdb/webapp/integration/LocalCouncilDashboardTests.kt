package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Nested
import uk.gov.communities.prsdb.webapp.constants.LOCAL_COUNCIL_DASHBOARD_SURVEY_URL
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLocalCouncilUsersPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.SearchLandlordRegisterPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.SearchPropertyRegisterPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import kotlin.test.Test

class LocalCouncilDashboardTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Test
    fun `the dashboard loads displaying the user's name and local council`(page: Page) {
        val dashboard = navigator.goToLocalCouncilDashboard()
        assertThat(dashboard.bannerHeading).containsText("Mock User")
        assertThat(dashboard.bannerSubHeading).containsText("BATH AND NORTH EAST SOMERSET COUNCIL")
    }

    @Test
    fun `the search for a property button links to the property search page`(page: Page) {
        val dashboard = navigator.goToLocalCouncilDashboard()
        dashboard.searchPropertyButton.clickAndWait()
        assertPageIs(page, SearchPropertyRegisterPage::class)
    }

    @Test
    fun `the search for a landlord button links to the property search page`(page: Page) {
        val dashboard = navigator.goToLocalCouncilDashboard()
        dashboard.searchLandlordButton.clickAndWait()
        assertPageIs(page, SearchLandlordRegisterPage::class)
    }

    @Test
    fun `the feedback survey link points to the dashboard survey`(page: Page) {
        val dashboard = navigator.goToLocalCouncilDashboard()
        assertThat(dashboard.surveyLink).hasAttribute("href", LOCAL_COUNCIL_DASHBOARD_SURVEY_URL)
    }

    @Test
    fun `the feedback survey body references the manage users section for an admin user`(page: Page) {
        val dashboard = navigator.goToLocalCouncilDashboard()
        assertThat(dashboard.surveyPanelBody).hasText(
            "Explore the Search for a property, Search for a landlord and Manage users sections, then share your opinion. " +
                "Please only do this survey once.",
        )
    }

    @Nested
    inner class LcUserNotAdmin : NestedIntegrationTestWithImmutableData("data-mockuser-local-council-user-not-admin.sql") {
        @Test
        fun `the manage users button is not visible`(page: Page) {
            val dashboard = navigator.goToLocalCouncilDashboard()
            assertThat(dashboard.manageUsersLink).isHidden()
        }

        @Test
        fun `the feedback survey body does not reference the manage users section`(page: Page) {
            val dashboard = navigator.goToLocalCouncilDashboard()
            assertThat(dashboard.surveyPanelBody).hasText(
                "Explore the Search for a property and Search for a landlord sections, then share your opinion. " +
                    "Please only do this survey once.",
            )
        }
    }

    @Nested
    inner class LcAdminUser : NestedIntegrationTestWithImmutableData("data-mockuser-local-council-admin-user.sql") {
        @Test
        fun `the manage users button is visible and when clicked redirects to the manage users page`(page: Page) {
            val dashboard = navigator.goToLocalCouncilDashboard()
            dashboard.manageUsersLink.clickAndWait()
            assertPageIs(page, ManageLocalCouncilUsersPage::class)
        }
    }
}
