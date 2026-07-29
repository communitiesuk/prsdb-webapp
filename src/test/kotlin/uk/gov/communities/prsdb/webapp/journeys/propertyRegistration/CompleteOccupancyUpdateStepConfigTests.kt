package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OccupiedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.occupancy.CompleteOccupancyUpdateStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.occupancy.UpdateOccupancyJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OccupancyFormModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyUpdateEmailService

@ExtendWith(MockitoExtension::class)
class CompleteOccupancyUpdateStepConfigTests {
    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockPropertyUpdateEmailService: PropertyUpdateEmailService

    @Mock
    private lateinit var mockState: UpdateOccupancyJourneyState

    @Mock
    private lateinit var mockOccupiedStep: OccupiedStep

    @Mock
    private lateinit var mockOccupancyFormModel: OccupancyFormModel

    private lateinit var stepConfig: CompleteOccupancyUpdateStepConfig

    private val propertyId = 123L
    private val initialLastModifiedDate = Clock.System.now().toJavaInstant()

    @BeforeEach
    fun setUp() {
        stepConfig =
            CompleteOccupancyUpdateStepConfig(
                propertyOwnershipService = mockPropertyOwnershipService,
                propertyUpdateEmailService = mockPropertyUpdateEmailService,
            )
    }

    @Test
    fun `afterStepIsReached calls updateIsOccupied on propertyOwnershipService`() {
        // Arrange
        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockState.lastModifiedDate).thenReturn(initialLastModifiedDate.toString())
        whenever(mockState.occupied).thenReturn(mockOccupiedStep)
        whenever(mockOccupiedStep.formModel).thenReturn(mockOccupancyFormModel)
        whenever(mockOccupancyFormModel.occupied).thenReturn(true)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockPropertyOwnershipService).updateIsOccupied(
            id = propertyId,
            isOccupied = true,
            initialLastModifiedDate = initialLastModifiedDate,
        )
    }

    @Test
    fun `afterStepIsReached sends an update email with the occupancy bullet`() {
        // Arrange
        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockState.lastModifiedDate).thenReturn(initialLastModifiedDate.toString())
        whenever(mockState.occupied).thenReturn(mockOccupiedStep)
        whenever(mockOccupiedStep.formModel).thenReturn(mockOccupancyFormModel)
        whenever(mockOccupancyFormModel.occupied).thenReturn(true)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockPropertyUpdateEmailService).sendUpdateEmails(
            eq(propertyId),
            eq(listOf("Whether the property is occupied by tenants")),
        )
    }

    @Test
    fun `afterStepIsReached deletes the journey then rethrows when it gets an UpdateConflictException`() {
        // Arrange
        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockState.lastModifiedDate).thenReturn(initialLastModifiedDate.toString())
        whenever(mockState.occupied).thenReturn(mockOccupiedStep)
        whenever(mockOccupiedStep.formModel).thenReturn(mockOccupancyFormModel)
        whenever(mockOccupancyFormModel.occupied).thenReturn(true)
        whenever(
            mockPropertyOwnershipService.updateIsOccupied(
                id = propertyId,
                isOccupied = true,
                initialLastModifiedDate = initialLastModifiedDate,
            ),
        ).thenThrow(UpdateConflictException::class.java)

        // Act, assert
        assertThrows<UpdateConflictException> { stepConfig.afterStepIsReached(mockState) }

        verify(mockState).deleteJourney()
    }

    @Test
    fun `resolveNextDestination calls deleteJourney on state`() {
        // Act
        stepConfig.resolveNextDestination(mockState, Destination.ExternalUrl("redirect"))

        // Assert
        verify(mockState).deleteJourney()
    }
}
