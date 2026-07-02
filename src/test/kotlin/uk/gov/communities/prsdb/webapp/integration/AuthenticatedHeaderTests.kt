package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncilInvitation
import uk.gov.communities.prsdb.webapp.integration.IntegrationTestWithImmutableData.NestedIntegrationTestWithImmutableData
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.AuthenticatedHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LandlordDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LocalCouncilDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.SystemOperatorDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.services.LocalCouncilInvitationService
import uk.gov.communities.prsdb.webapp.services.LocalCouncilService
import kotlin.test.Test

class AuthenticatedHeaderTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Autowired
    lateinit var localCouncilService: LocalCouncilService

    @Autowired
    lateinit var invitationService: LocalCouncilInvitationService

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

    @Test
    fun `landlord dashboard shows a dashboard nav link that returns to the landlord dashboard`(page: Page) {
        val dashboard = navigator.goToLandlordDashboard()

        assertThat(dashboard.authenticatedHeader.dashboardNavLink).isVisible()

        dashboard.authenticatedHeader.dashboardNavLink.clickAndWait()
        assertPageIs(page, LandlordDashboardPage::class)
    }

    @Test
    fun `local council dashboard shows a dashboard nav link that returns to the local council dashboard`(page: Page) {
        val dashboard = navigator.goToLocalCouncilDashboard()

        assertThat(dashboard.authenticatedHeader.dashboardNavLink).isVisible()

        dashboard.authenticatedHeader.dashboardNavLink.clickAndWait()
        assertPageIs(page, LocalCouncilDashboardPage::class)
    }

    @Test
    fun `system operator dashboard shows a dashboard nav link that returns to the system operator dashboard`(page: Page) {
        navigator.goToSystemOperatorDashboard()
        val header = AuthenticatedHeader(page)

        assertThat(header.dashboardNavLink).isVisible()

        header.dashboardNavLink.clickAndWait()
        assertPageIs(page, SystemOperatorDashboardPage::class)
    }

    @Nested
    inner class LocalCouncilRegistrationHeader : NestedIntegrationTestWithImmutableData("data-mockuser-not-local-council-user.sql") {
        lateinit var invitation: LocalCouncilInvitation

        @BeforeEach
        fun setup() {
            val token =
                invitationService.createInvitationToken(
                    email = "anyEmail@test.com",
                    council = localCouncilService.retrieveLocalCouncilById(2),
                )
            invitation = invitationService.getInvitationFromToken(token)
        }

        @Test
        fun `local council registration page hides One Login header elements but shows sign out`(page: Page) {
            navigator.skipToLocalCouncilUserRegistrationNameFormPage(invitation.token)
            val header = AuthenticatedHeader(page)

            assertThat(header.oneLoginToggleButton).hasCount(0)
            assertThat(header.oneLoginAccountLink).hasCount(0)
            assertThat(header.signOutLink).isVisible()
        }

        @Test
        fun `local council registration page does not show a dashboard nav link`(page: Page) {
            navigator.skipToLocalCouncilUserRegistrationNameFormPage(invitation.token)
            val header = AuthenticatedHeader(page)

            assertThat(header.dashboardNavLink).hasCount(0)
        }
    }
}
