package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.freeholders

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel
import uk.gov.communities.prsdb.webapp.services.FreeholderService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createPropertyOwnership

@ExtendWith(MockitoExtension::class)
class CompleteFreeholdersUpdateStepConfigTests {
    @Mock
    private lateinit var freeholderService: FreeholderService

    @Mock
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var journeyState: UpdateFreeholdersJourneyState

    @Test
    fun `completion reconciles the submitted freeholders against the saved snapshot`() {
        val propertyOwnership = createPropertyOwnership()
        val submittedFreeholders =
            listOf(FreeholderDataModel(detailsKnown = true, name = "New Freeholder", email = "freeholder@example.com"))
        val snapshot = mapOf(1L to "2025-01-01T00:00:00Z")
        whenever(journeyState.propertyId).thenReturn(propertyOwnership.id)
        whenever(journeyState.freeholdersMap).thenReturn(mapOf(1 to submittedFreeholders.single()))
        whenever(journeyState.freeholderSnapshot).thenReturn(snapshot)
        whenever(propertyOwnershipService.getPropertyOwnership(propertyOwnership.id)).thenReturn(propertyOwnership)

        val stepConfig =
            CompleteFreeholdersUpdateStepConfig(
                freeholderService,
                propertyOwnershipService,
            )

        stepConfig.afterStepIsReached(journeyState)

        verify(freeholderService).updateFreeholders(propertyOwnership, submittedFreeholders, snapshot)
    }

    @Test
    fun `completion discards the journey when the freeholder snapshot is stale`() {
        val propertyOwnership = createPropertyOwnership()
        val submittedFreeholders = emptyList<FreeholderDataModel>()
        val snapshot = emptyMap<Long, String>()
        whenever(journeyState.propertyId).thenReturn(propertyOwnership.id)
        whenever(journeyState.freeholdersMap).thenReturn(emptyMap())
        whenever(journeyState.freeholderSnapshot).thenReturn(snapshot)
        whenever(propertyOwnershipService.getPropertyOwnership(propertyOwnership.id)).thenReturn(propertyOwnership)
        doThrow(UpdateConflictException("Freeholders changed"))
            .whenever(freeholderService)
            .updateFreeholders(propertyOwnership, submittedFreeholders, snapshot)

        val stepConfig =
            CompleteFreeholdersUpdateStepConfig(
                freeholderService,
                propertyOwnershipService,
            )

        assertThrows(UpdateConflictException::class.java) {
            stepConfig.afterStepIsReached(journeyState)
        }

        verify(journeyState).deleteJourney()
    }
}
