package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.freeholders

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.util.ReflectionTestUtils
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

@ExtendWith(MockitoExtension::class)
class InitialiseFreeholdersUpdateStepConfigTests {
    @Mock
    lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    lateinit var mockState: UpdateFreeholdersJourneyState

    private val propertyId = 123L

    @Test
    fun `afterStepIsReached seeds freeholders keyed from 1 in id order and sets the next id`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyId)
        val freeholder1 = MockLandlordData.createFreeholder(propertyOwnership, name = "Alice", email = "alice@example.com").withId(1)
        val freeholder2 = MockLandlordData.createFreeholder(propertyOwnership, name = "Bob").withId(2)
        propertyOwnership.freeholders.add(freeholder2)
        propertyOwnership.freeholders.add(freeholder1)
        whenever(mockState.freeholdersInitialised).thenReturn(null)
        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyId)).thenReturn(propertyOwnership)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockState).freeholdersMap =
            mapOf(
                1 to FreeholderDataModel.fromFreeholder(freeholder1),
                2 to FreeholderDataModel.fromFreeholder(freeholder2),
            )
        verify(mockState).nextFreeholderId = 3
        verify(mockState).freeholdersInitialised = true
    }

    @Test
    fun `afterStepIsReached does nothing when already initialised`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.freeholdersInitialised).thenReturn(true)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verifyNoInteractions(mockPropertyOwnershipService)
        verify(mockState, never()).freeholdersMap = any()
    }

    private fun setupStepConfig(): InitialiseFreeholdersUpdateStepConfig {
        val stepConfig = InitialiseFreeholdersUpdateStepConfig(mockPropertyOwnershipService)
        stepConfig.urlPath = InitialiseFreeholdersUpdateStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }

    private fun <T : Any> T.withId(id: Long): T = apply { ReflectionTestUtils.setField(this, "id", id) }
}
