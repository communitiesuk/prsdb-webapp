package uk.gov.communities.prsdb.webapp.integration

import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import kotlin.test.Test

class AuthenticatedHeaderTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Test
    fun `landlord dashboard shows One Login header elements`() {
        val dashboard = navigator.goToLandlordDashboard()

        assertThat(dashboard.authenticatedHeader.oneLoginToggleButton).isAttached()
        assertThat(dashboard.authenticatedHeader.oneLoginAccountLink).isAttached()
        assertThat(dashboard.authenticatedHeader.signOutLink).isVisible()
    }

    @Test
    fun `local council dashboard hides One Login header elements but shows sign out`() {
        val dashboard = navigator.goToLocalCouncilDashboard()

        assertThat(dashboard.authenticatedHeader.oneLoginToggleButton).hasCount(0)
        assertThat(dashboard.authenticatedHeader.oneLoginAccountLink).hasCount(0)
        assertThat(dashboard.authenticatedHeader.signOutLink).isVisible()
    }
}
