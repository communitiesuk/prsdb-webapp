package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.communities.prsdb.webapp.controllers.TeamMembersController.Companion.INVITATIONS_FRAGMENT
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.TeamMembersPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.cancelOrganisationalLandlordInvitationPages.AreYouSurePageCancelOrganisationalLandlordInvitation
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@WithOrgLandlordProfile
class CancelOrganisationalLandlordInvitationJourneyTests :
    IntegrationTestWithMutableData("data-local.sql") {
    @Autowired
    private lateinit var invitationRepository: OrganisationalLandlordInvitationRepository

    private val oldestPendingInvitationToken = UUID.fromString("1234abcd-5678-abcd-1234-567abcd2222c")

    @Test
    fun `an admin can cancel a pending invitation`(page: Page) {
        // Arrange
        val teamMembersPage = navigator.goToTeamMembers()
        teamMembersPage.tabs.goToInvitations(pendingInvitationCount = 3)
        teamMembersPage.pendingInvitationsSummaryList.getRowByIndex(0).clickNamedActionLinkAndWait("Cancel")

        // Act
        val areYouSurePage = assertPageIs(page, AreYouSurePageCancelOrganisationalLandlordInvitation::class)
        BaseComponent
            .assertThat(areYouSurePage.form.fieldsetHeading)
            .containsText("Do you want to cancel the invitation for pending-org-invite-c@example.com?")
        BaseComponent.assertThat(areYouSurePage.form.submitButton).hasText("Confirm")
        areYouSurePage.submitWantsToProceed()

        // Assert
        val teamMembersPageAfter = assertPageIs(page, TeamMembersPage::class)
        BaseComponent.assertThat(teamMembersPageAfter.successBanner.content.heading).hasText("Invitation cancelled")
        assertEquals(INVITATIONS_FRAGMENT, teamMembersPageAfter.tabs.activeTabPanelId)
        assertThat(teamMembersPageAfter.pendingInvitationsSummaryList.rows).hasCount(2)
        assertNull(invitationRepository.findByToken(oldestPendingInvitationToken))
    }

    @Test
    fun `choosing not to cancel returns to the invitations tab without cancelling the invitation`(page: Page) {
        // Arrange
        val teamMembersPage = navigator.goToTeamMembers()
        teamMembersPage.tabs.goToInvitations(pendingInvitationCount = 3)
        teamMembersPage.pendingInvitationsSummaryList.getRowByIndex(0).clickNamedActionLinkAndWait("Cancel")

        // Act
        val areYouSurePage = assertPageIs(page, AreYouSurePageCancelOrganisationalLandlordInvitation::class)
        areYouSurePage.submitDoesNotWantToProceed()

        // Assert
        val teamMembersPageAfter = assertPageIs(page, TeamMembersPage::class)
        BaseComponent.assertThat(teamMembersPageAfter.successBanner).isHidden()
        assertEquals(INVITATIONS_FRAGMENT, teamMembersPageAfter.tabs.activeTabPanelId)
        assertThat(teamMembersPageAfter.pendingInvitationsSummaryList.rows).hasCount(3)
        assertNotNull(invitationRepository.findByToken(oldestPendingInvitationToken))
    }
}
