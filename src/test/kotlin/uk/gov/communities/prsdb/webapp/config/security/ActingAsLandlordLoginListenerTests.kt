package uk.gov.communities.prsdb.webapp.config.security

import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.authentication.event.AuthenticationSuccessEvent
import uk.gov.communities.prsdb.webapp.services.ActingAsLandlordSessionService

class ActingAsLandlordLoginListenerTests {
    private val actingAsLandlordSessionService: ActingAsLandlordSessionService = mock()
    private val listener = ActingAsLandlordLoginListener(actingAsLandlordSessionService)

    // TODO PDJB-1907: Integration test that signing in again ends acting as a landlord
    @Test
    fun `signing in clears any acting as user ID from the session`() {
        // Arrange
        val event = AuthenticationSuccessEvent(TestingAuthenticationToken("user-id", null))

        // Act
        listener.onApplicationEvent(event)

        // Assert
        verify(actingAsLandlordSessionService).clearActingAsUserId()
    }
}
