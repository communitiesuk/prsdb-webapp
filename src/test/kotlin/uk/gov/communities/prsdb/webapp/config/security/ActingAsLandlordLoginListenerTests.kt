package uk.gov.communities.prsdb.webapp.config.security

import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.authentication.event.AuthenticationSuccessEvent
import uk.gov.communities.prsdb.webapp.services.ActingAsLandlordService

class ActingAsLandlordLoginListenerTests {
    private val actingAsLandlordService: ActingAsLandlordService = mock()
    private val listener = ActingAsLandlordLoginListener(actingAsLandlordService)

    @Test
    fun `signing in clears any acting as user ID from the session`() {
        // Arrange
        val event = AuthenticationSuccessEvent(TestingAuthenticationToken("user-id", null))

        // Act
        listener.onApplicationEvent(event)

        // Assert
        verify(actingAsLandlordService).clearActingAsUserId()
    }
}
