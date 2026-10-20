package uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel.stepConfig

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlordUser
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel.ChangeOrgTeamMemberAccessLevelJourneyState
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.ChangeOrgTeamMemberAccessLevelEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordUserService
import java.net.URI

@ExtendWith(MockitoExtension::class)
class CompleteChangeAccessLevelStepConfigTests {
    @Mock
    lateinit var mockOrganisationalLandlordUserService: OrganisationalLandlordUserService

    @Mock
    lateinit var mockEmailSender: EmailNotificationService<ChangeOrgTeamMemberAccessLevelEmail>

    @Mock
    lateinit var mockAbsoluteUrlProvider: AbsoluteUrlProvider

    @Mock
    lateinit var mockState: ChangeOrgTeamMemberAccessLevelJourneyState

    private fun setupStepConfig() =
        CompleteChangeAccessLevelStepConfig(
            mockOrganisationalLandlordUserService,
            mockEmailSender,
            mockAbsoluteUrlProvider,
        )

    private fun mockMember(): OrganisationalLandlordUser {
        val org = mock<OrganisationalLandlord>()
        whenever(org.name).thenReturn("Example Org")
        val member = mock<OrganisationalLandlordUser>()
        whenever(member.name).thenReturn("Anita Locke")
        whenever(member.email).thenReturn("anita@example.com")
        whenever(member.organisationalLandlord).thenReturn(org)
        return member
    }

    private fun stubState(newRole: OrganisationalLandlordUserRole) {
        whenever(mockState.selectedRole).thenReturn(newRole)
        whenever(mockState.otherOrganisationalLandlordUserId).thenReturn(1L)
        whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI("https://example.com/dashboard"))
        val member = mockMember()
        whenever(mockOrganisationalLandlordUserService.getOrganisationalLandlordUserOrNull(1L)).thenReturn(member)
    }

    @Test
    fun `afterStepIsReached updates the role and sends an admin email to the member`() {
        val stepConfig = setupStepConfig()
        stubState(OrganisationalLandlordUserRole.ADMIN)

        stepConfig.afterStepIsReached(mockState)

        verify(mockOrganisationalLandlordUserService).updateRole(1L, OrganisationalLandlordUserRole.ADMIN)
        verify(mockEmailSender).sendEmail(
            eq("anita@example.com"),
            eq(
                ChangeOrgTeamMemberAccessLevelEmail(
                    memberName = "Anita Locke",
                    roleName = "administrator",
                    organisationName = "Example Org",
                    isAdmin = true,
                    dashboardUrl = URI("https://example.com/dashboard"),
                ),
            ),
        )
    }

    @Test
    fun `afterStepIsReached updates the role and sends an editor email to the member`() {
        val stepConfig = setupStepConfig()
        stubState(OrganisationalLandlordUserRole.EDITOR)

        stepConfig.afterStepIsReached(mockState)

        verify(mockOrganisationalLandlordUserService).updateRole(1L, OrganisationalLandlordUserRole.EDITOR)
        verify(mockEmailSender).sendEmail(
            eq("anita@example.com"),
            eq(
                ChangeOrgTeamMemberAccessLevelEmail(
                    memberName = "Anita Locke",
                    roleName = "editor",
                    organisationName = "Example Org",
                    isAdmin = false,
                    dashboardUrl = URI("https://example.com/dashboard"),
                ),
            ),
        )
    }

    @Test
    fun `resolveNextDestination deletes the journey`() {
        val stepConfig = setupStepConfig()

        // Act
        stepConfig.resolveNextDestination(mockState, Destination.ExternalUrl("redirect"))

        // Assert
        verify(mockState).deleteJourney()
    }
}
