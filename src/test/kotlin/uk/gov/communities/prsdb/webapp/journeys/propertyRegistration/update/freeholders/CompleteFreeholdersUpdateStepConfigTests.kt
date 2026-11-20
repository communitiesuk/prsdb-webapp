package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.freeholders

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.UnrecoverableJourneyStateException
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class CompleteFreeholdersUpdateStepConfigTests {
    @Mock
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var journeyState: UpdateFreeholdersJourneyState

    private val propertyId = 123L
    private val lastModifiedDate = Instant.parse("2025-01-01T00:00:00Z")
    private val freeholder = FreeholderDataModel(detailsKnown = true, name = "New Freeholder", email = "freeholder@example.com")

    @Test
    fun `afterStepIsReached updates the freeholders with the journey's last modified date`() {
        // Arrange
        whenever(journeyState.propertyId).thenReturn(propertyId)
        whenever(journeyState.lastModifiedDate).thenReturn(lastModifiedDate.toString())
        whenever(journeyState.freeholdersMap).thenReturn(mapOf(1 to freeholder))
        val stepConfig = CompleteFreeholdersUpdateStepConfig(propertyOwnershipService)

        // Act
        stepConfig.afterStepIsReached(journeyState)

        // Assert
        verify(propertyOwnershipService).updateFreeholders(propertyId, listOf(freeholder), lastModifiedDate)
    }

    @Test
    fun `afterStepIsReached throws when the freeholders list is missing`() {
        // Arrange
        whenever(journeyState.freeholdersMap).thenReturn(null)
        whenever(journeyState.journeyId).thenReturn("journey-id")
        val stepConfig = CompleteFreeholdersUpdateStepConfig(propertyOwnershipService)

        // Act & Assert
        assertThrows(UnrecoverableJourneyStateException::class.java) {
            stepConfig.afterStepIsReached(journeyState)
        }
        verifyNoInteractions(propertyOwnershipService)
    }

    @Test
    fun `afterStepIsReached deletes the journey and rethrows when there is an update conflict`() {
        // Arrange
        whenever(journeyState.propertyId).thenReturn(propertyId)
        whenever(journeyState.lastModifiedDate).thenReturn(lastModifiedDate.toString())
        whenever(journeyState.freeholdersMap).thenReturn(mapOf(1 to freeholder))
        doThrow(UpdateConflictException("Conflict"))
            .whenever(propertyOwnershipService)
            .updateFreeholders(propertyId, listOf(freeholder), lastModifiedDate)
        val stepConfig = CompleteFreeholdersUpdateStepConfig(propertyOwnershipService)

        // Act & Assert
        assertThrows(UpdateConflictException::class.java) {
            stepConfig.afterStepIsReached(journeyState)
        }
        verify(journeyState).deleteJourney()
    }

    @Test
    fun `resolveNextDestination deletes the journey and returns the default destination`() {
        // Arrange
        val stepConfig = CompleteFreeholdersUpdateStepConfig(propertyOwnershipService)
        val defaultDestination = Destination.ExternalUrl("/landlord/property-details/1#people-responsible")

        // Act
        val result = stepConfig.resolveNextDestination(journeyState, defaultDestination)

        // Assert
        verify(journeyState).deleteJourney()
        assertEquals(defaultDestination, result)
    }
}
