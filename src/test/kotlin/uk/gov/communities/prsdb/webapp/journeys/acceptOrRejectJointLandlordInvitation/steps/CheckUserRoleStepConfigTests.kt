package uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.AcceptOrRejectJointLandlordInvitationJourneyState
import uk.gov.communities.prsdb.webapp.services.UserRolesService

@ExtendWith(MockitoExtension::class)
class CheckUserRoleStepConfigTests {
    @Mock
    lateinit var mockUserRolesService: UserRolesService

    @Mock
    lateinit var mockState: AcceptOrRejectJointLandlordInvitationJourneyState

    private val username = "test-user"

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `mode returns USER_IS_ALREADY_REGISTERED_AS_LANDLORD when state indicates user is a landlord`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.userIsLandlord).thenReturn(true)

        // Act & Assert
        assertEquals(UserRoleStatus.USER_IS_ALREADY_REGISTERED_AS_LANDLORD, stepConfig.mode(mockState))
    }

    @Test
    fun `mode returns USER_NOT_REGISTERED_AS_LANDLORD when state indicates user is not a landlord`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.userIsLandlord).thenReturn(false)

        // Act & Assert
        assertEquals(UserRoleStatus.USER_NOT_REGISTERED_AS_LANDLORD, stepConfig.mode(mockState))
    }

    @Test
    fun `mode throws PrsdbWebException when userIsLandlord is null`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.userIsLandlord).thenReturn(null)

        // Act & Assert
        assertThrows<PrsdbWebException> { stepConfig.mode(mockState) }
    }

    @Test
    fun `afterStepIsReached sets userIsLandlord to false when user does not have landlord role`() {
        // Arrange
        val stepConfig = setupStepConfig()
        setMockPrincipal(username)
        whenever(mockUserRolesService.getHasLandlordUserRole(username)).thenReturn(false)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockState).userIsLandlord = false
    }

    @Test
    fun `afterStepIsReached sets userIsLandlord to true when user has landlord role`() {
        // Arrange
        val stepConfig = setupStepConfig()
        setMockPrincipal(username)
        whenever(mockUserRolesService.getHasLandlordUserRole(username)).thenReturn(true)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockState).userIsLandlord = true
    }

    private fun setupStepConfig(): CheckUserRoleStepConfig = CheckUserRoleStepConfig(mockUserRolesService)

    private fun setMockPrincipal(name: String) {
        val oidcUser = mock<OidcUser>()
        whenever(oidcUser.name).thenReturn(name)
        val authentication = mock<Authentication>()
        whenever(authentication.principal).thenReturn(oidcUser)
        val context = mock<SecurityContext>()
        whenever(context.authentication).thenReturn(authentication)
        SecurityContextHolder.setContext(context)
    }
}
