package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.NullSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.INCOMPLETE_PROPERTY_FORM_CONTEXTS_DELETED_THIS_SESSION
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_REACTIVATED_BY_LAST_PROPERTY_REGISTRATION

@ExtendWith(MockitoExtension::class)
class PropertyRegistrationConfirmationServiceTests {
    @Mock
    private lateinit var mockSession: HttpSession

    @InjectMocks
    private lateinit var propertyRegistrationConfirmationService: PropertyRegistrationConfirmationService

    @Test
    fun `addIncompletePropertyFormContextsDeletedThisSession adds a contextId to the list of cancelled formContexts this session`() {
        // Arrange
        whenever(mockSession.getAttribute(INCOMPLETE_PROPERTY_FORM_CONTEXTS_DELETED_THIS_SESSION))
            .thenReturn(mutableListOf("1"))

        // Act
        propertyRegistrationConfirmationService.addIncompletePropertyFormContextsDeletedThisSession("2")

        // Assert
        verify(mockSession).setAttribute(
            INCOMPLETE_PROPERTY_FORM_CONTEXTS_DELETED_THIS_SESSION,
            mutableListOf("1", "2"),
        )
    }

    @Test
    fun `wasIncompletePropertyDeletedThisSession returns true when the form context was deleted this session`() {
        // Arrange
        whenever(mockSession.getAttribute(INCOMPLETE_PROPERTY_FORM_CONTEXTS_DELETED_THIS_SESSION))
            .thenReturn(mutableListOf("1", "2", "3"))

        // Act
        val wasDeleted =
            propertyRegistrationConfirmationService.wasIncompletePropertyDeletedThisSession("2")

        // Assert
        assertTrue(wasDeleted)
    }

    @Test
    fun `wasIncompletePropertyDeletedThisSession returns false when the form context was not deleted this session`() {
        // Arrange
        whenever(mockSession.getAttribute(INCOMPLETE_PROPERTY_FORM_CONTEXTS_DELETED_THIS_SESSION))
            .thenReturn(mutableListOf("1", "2", "3"))

        // Act
        val wasDeleted =
            propertyRegistrationConfirmationService.wasIncompletePropertyDeletedThisSession("4")

        // Assert
        assertFalse(wasDeleted)
    }

    @Test
    fun `setLandlordReactivatedByLastRegistration stores whether the landlord was reactivated`() {
        // Act
        propertyRegistrationConfirmationService.setLandlordReactivatedByLastRegistration(true)

        // Assert
        verify(mockSession).setAttribute(LANDLORD_REACTIVATED_BY_LAST_PROPERTY_REGISTRATION, true)
    }

    @Test
    fun `wasLandlordReactivatedByLastRegistration returns true when the session records a reactivation`() {
        // Arrange
        whenever(mockSession.getAttribute(LANDLORD_REACTIVATED_BY_LAST_PROPERTY_REGISTRATION)).thenReturn(true)

        // Act, Assert
        assertTrue(propertyRegistrationConfirmationService.wasLandlordReactivatedByLastRegistration())
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(booleans = [false])
    fun `wasLandlordReactivatedByLastRegistration returns false when the session does not record a reactivation`(sessionValue: Boolean?) {
        // Arrange
        whenever(mockSession.getAttribute(LANDLORD_REACTIVATED_BY_LAST_PROPERTY_REGISTRATION)).thenReturn(sessionValue)

        // Act, Assert
        assertFalse(propertyRegistrationConfirmationService.wasLandlordReactivatedByLastRegistration())
    }
}
