package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class LegacyIncompletePropertyServiceTests {
    @Mock
    private lateinit var mockLegacyIncompletePropertyFormContextService: LegacyIncompletePropertyFormContextService

    @InjectMocks
    private lateinit var legacyIncompletePropertyService: LegacyIncompletePropertyService

    @Test
    fun `deleteIncompleteProperty deletes the form context for a valid incomplete property`() {
        val formContext = MockLandlordData.createPropertyRegistrationFormContext()
        val principalName = "user"

        whenever(
            mockLegacyIncompletePropertyFormContextService.getIncompletePropertyFormContextForLandlordOrThrowNotFound(
                formContext.id,
                principalName,
            ),
        ).thenReturn(formContext)

        legacyIncompletePropertyService.deleteIncompleteProperty(formContext.id.toString(), principalName)

        verify(mockLegacyIncompletePropertyFormContextService).deleteFormContext(formContext)
    }

    @Test
    fun `deleteIncompleteProperty throws NOT_FOUND error for an invalid incomplete property`() {
        val formContextId = "123"
        val principalName = "user"

        val expectedErrorMessage =
            "404 NOT_FOUND \"Form context with ID: $formContextId and journey type: " +
                "${JourneyType.PROPERTY_REGISTRATION.name} not found for base user: $principalName\""

        whenever(
            mockLegacyIncompletePropertyFormContextService.getIncompletePropertyFormContextForLandlordOrThrowNotFound(
                formContextId.toLong(),
                principalName,
            ),
        ).thenThrow(
            ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Form context with ID: $formContextId and journey type: " +
                    "${JourneyType.PROPERTY_REGISTRATION.name} not found for base user: $principalName",
            ),
        )

        // Act and Assert
        val exception =
            assertThrows<ResponseStatusException> {
                legacyIncompletePropertyService.deleteIncompleteProperty(formContextId, principalName)
            }
        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
        assertEquals(expectedErrorMessage, exception.message)
    }

    @Test
    fun `isIncompletePropertyAvailable returns true when the form context exists`() {
        // Arrange
        val formContextId = 1L
        val expectedFormContext = MockLandlordData.createPropertyRegistrationFormContext(id = formContextId)
        whenever(mockLegacyIncompletePropertyFormContextService.getFormContext(formContextId))
            .thenReturn(expectedFormContext)

        // Act, Assert
        assertTrue(legacyIncompletePropertyService.isIncompletePropertyAvailable(formContextId.toString(), "user"))
    }

    @Test
    fun `isIncompletePropertyAvailable returns false when the form context does not exist`() {
        // Arrange
        val formContextId = 1L
        whenever(mockLegacyIncompletePropertyFormContextService.getFormContext(formContextId)).thenReturn(null)

        // Act, Assert
        assertFalse(legacyIncompletePropertyService.isIncompletePropertyAvailable(formContextId.toString(), "user"))
    }
}
