package uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel.stepConfig

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.controllers.TeamMembersController
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlordUser
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel.ChangeOrgTeamMemberAccessLevelJourneyState
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.RemoveOrgTeamMemberConfirmationEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordUserService
import uk.gov.communities.prsdb.webapp.services.UserToOrgLandlordUserService
import java.net.URI
import kotlin.test.assertSame
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class CompleteRemoveOrgUserStepConfigTests {
    @Mock
    private lateinit var mockOrganisationalLandlordUserService: OrganisationalLandlordUserService

    @Mock
    private lateinit var mockState: ChangeOrgTeamMemberAccessLevelJourneyState

    @Mock
    private lateinit var mockUserToOrgLandlordUserService: UserToOrgLandlordUserService

    @Mock
    private lateinit var mockEmailSender: EmailNotificationService<RemoveOrgTeamMemberConfirmationEmail>

    @Mock
    private lateinit var mockAbsoluteUrlProvider: AbsoluteUrlProvider

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    private fun createStepConfig() =
        CompleteRemoveOrgUserStepConfig(
            mockOrganisationalLandlordUserService,
            mockUserToOrgLandlordUserService,
            mockEmailSender,
            mockAbsoluteUrlProvider,
        )

    @Test
    fun `afterStepIsReached removes the target membership and emails only the removing admin`() {
        // Arrange
        val organisation = mock<OrganisationalLandlord>()
        whenever(organisation.name).thenReturn("Keystone Living Group")
        val removedMember = mock<OrganisationalLandlordUser>()
        whenever(removedMember.name).thenReturn("Anita Locke")
        whenever(removedMember.organisationalLandlord).thenReturn(organisation)
        val admin = mock<OrganisationalLandlordUser>()
        whenever(admin.name).thenReturn("Alex Admin")
        whenever(admin.email).thenReturn("alex@example.com")
        whenever(mockState.otherOrganisationalLandlordUserId).thenReturn(42L)
        whenever(mockOrganisationalLandlordUserService.getOrganisationalLandlordUserOrNull(42L)).thenReturn(removedMember)
        whenever(mockUserToOrgLandlordUserService.getOrganisationalLandlordUserForCurrentUser()).thenReturn(admin)
        whenever(mockAbsoluteUrlProvider.buildTeamMembersUri()).thenReturn(URI("https://example.com/team-members"))

        // Act
        createStepConfig().afterStepIsReached(mockState)

        // Assert
        verify(mockOrganisationalLandlordUserService).removeOrganisationalLandlordUser(42L)
        verify(mockEmailSender).sendEmail(
            eq("alex@example.com"),
            eq(
                RemoveOrgTeamMemberConfirmationEmail(
                    adminName = "Alex Admin",
                    memberName = "Anita Locke",
                    organisationName = "Keystone Living Group",
                    teamMembersUrl = URI("https://example.com/team-members"),
                ),
            ),
        )
        verifyNoMoreInteractions(mockEmailSender)
    }

    @Test
    fun `afterStepIsReached throws when the member to remove cannot be found`() {
        // Arrange
        whenever(mockState.otherOrganisationalLandlordUserId).thenReturn(42L)
        whenever(mockUserToOrgLandlordUserService.getOrganisationalLandlordUserForCurrentUser())
            .thenReturn(mock<OrganisationalLandlordUser>())
        whenever(mockOrganisationalLandlordUserService.getOrganisationalLandlordUserOrNull(42L)).thenReturn(null)

        // Act, Assert
        assertThrows<PrsdbWebException> { createStepConfig().afterStepIsReached(mockState) }
        verify(mockOrganisationalLandlordUserService, never()).removeOrganisationalLandlordUser(any())
        verifyNoMoreInteractions(mockEmailSender)
    }

    @Test
    fun `afterStepIsReached throws when the current user is not an organisational landlord user`() {
        // Arrange
        val authentication = mock<Authentication>()
        whenever(authentication.name).thenReturn("test-user")
        val securityContext = mock<SecurityContext>()
        whenever(securityContext.authentication).thenReturn(authentication)
        SecurityContextHolder.setContext(securityContext)
        whenever(mockUserToOrgLandlordUserService.getOrganisationalLandlordUserForCurrentUser()).thenReturn(null)

        // Act
        val exception = assertThrows<PrsdbWebException> { createStepConfig().afterStepIsReached(mockState) }

        // Assert
        assertTrue(exception.message!!.contains("test-user"))
        verify(mockOrganisationalLandlordUserService, never()).removeOrganisationalLandlordUser(any())
        verifyNoMoreInteractions(mockEmailSender)
    }

    @Test
    fun `resolveNextDestination deletes the journey before returning the default destination`() {
        // Arrange
        val destination = Destination.ExternalUrl(TeamMembersController.TEAM_MEMBERS_ROUTE)

        // Act
        val result = createStepConfig().resolveNextDestination(mockState, destination)

        // Assert
        verify(mockState).deleteJourney()
        assertSame(destination, result)
    }
}
