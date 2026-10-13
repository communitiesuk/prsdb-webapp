package uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
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
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.AcceptOrganisationalLandlordUserInvitationJourneyState
import uk.gov.communities.prsdb.webapp.services.UserRolesService

@ExtendWith(MockitoExtension::class)
class CheckUserIsLandlordStepConfigTests {
    @Mock
    lateinit var mockUserRolesService: UserRolesService

    @Mock
    lateinit var mockState: AcceptOrganisationalLandlordUserInvitationJourneyState

    private val username = "test-user"

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `mode returns ALREADY_A_LANDLORD when state indicates the user is a landlord`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.userIsLandlord).thenReturn(true)

        // Act & Assert
        assertEquals(LandlordAccountStatus.ALREADY_A_LANDLORD, stepConfig.mode(mockState))
    }

    @Test
    fun `mode returns NOT_A_LANDLORD when state indicates the user is not a landlord`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.userIsLandlord).thenReturn(false)

        // Act & Assert
        assertEquals(LandlordAccountStatus.NOT_A_LANDLORD, stepConfig.mode(mockState))
    }

    @Test
    fun `mode returns null when userIsLandlord has not been set`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.userIsLandlord).thenReturn(null)

        // Act & Assert
        assertNull(stepConfig.mode(mockState))
    }

    @Test
    fun `afterStepIsReached sets userIsLandlord to true when the user has a landlord role`() {
        // Arrange
        val stepConfig = setupStepConfig()
        setMockPrincipal(username)
        whenever(mockUserRolesService.getUserHasLandlordRole(username)).thenReturn(true)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockState).userIsLandlord = true
    }

    @Test
    fun `afterStepIsReached sets userIsLandlord to false when the user has no landlord role`() {
        // Arrange
        val stepConfig = setupStepConfig()
        setMockPrincipal(username)
        whenever(mockUserRolesService.getUserHasLandlordRole(username)).thenReturn(false)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockState).userIsLandlord = false
    }

    private fun setupStepConfig(): CheckUserIsLandlordStepConfig = CheckUserIsLandlordStepConfig(mockUserRolesService)

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
