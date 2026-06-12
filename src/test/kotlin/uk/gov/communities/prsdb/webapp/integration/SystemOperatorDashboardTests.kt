package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.InviteLocalCouncilAdminPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.MetricsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import kotlin.test.Test

class SystemOperatorDashboardTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Test
    fun `the dashboard loads displaying the system operator heading`(page: Page) {
        val dashboard = navigator.goToSystemOperatorDashboard()
        assertThat(dashboard.bannerHeading).containsText("System operator dashboard")
    }

    @Test
    fun `the invite a local council admin button links to the invite page`(page: Page) {
        val dashboard = navigator.goToSystemOperatorDashboard()
        dashboard.inviteLocalCouncilAdminButton.clickAndWait()
        assertPageIs(page, InviteLocalCouncilAdminPage::class)
    }

    @Test
    fun `the view metrics button links to the metrics page`(page: Page) {
        val dashboard = navigator.goToSystemOperatorDashboard()
        dashboard.metricsButton.clickAndWait()
        assertPageIs(page, MetricsPage::class)
    }

    @Test
    fun `the generate passcode button is not shown when the require-passcode profile is inactive`(page: Page) {
        val dashboard = navigator.goToSystemOperatorDashboard()
        assertThat(dashboard.generatePasscodeButton).isHidden()
    }
}
