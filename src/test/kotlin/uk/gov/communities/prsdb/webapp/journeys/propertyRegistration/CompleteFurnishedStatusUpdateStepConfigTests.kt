package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FurnishedStatusStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.furnishedStatus.CompleteFurnishedStatusUpdateStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.furnishedStatus.UpdateFurnishedStatusJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FurnishedStatusFormModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@ExtendWith(MockitoExtension::class)
class CompleteFurnishedStatusUpdateStepConfigTests {
    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockState: UpdateFurnishedStatusJourneyState

    @Mock
    private lateinit var mockFurnishedStatusStep: FurnishedStatusStep

    @Mock
    private lateinit var stepConfig: CompleteFurnishedStatusUpdateStepConfig

    @Mock
    private lateinit var mockFurnishedStatusFormModel: FurnishedStatusFormModel

    private val propertyId = 123L
    private val furnishedStatus = FurnishedStatus.FURNISHED
    private val initialLastModifiedDate = Clock.System.now().toJavaInstant()

    @BeforeEach
    fun setUp() {
        stepConfig =
            CompleteFurnishedStatusUpdateStepConfig(
                propertyOwnershipService = mockPropertyOwnershipService,
            )
    }

    @Test
    fun `afterStepDataIsAdded calls updateFurnishedStatus on propertyOwnershipService`() {
        // Arrange
        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockState.furnishedStatus).thenReturn(mockFurnishedStatusStep)
        whenever(mockState.lastModifiedDate).thenReturn(initialLastModifiedDate.toString())
        whenever(mockFurnishedStatusStep.formModel).thenReturn(mockFurnishedStatusFormModel)
        whenever(mockFurnishedStatusFormModel.furnishedStatus).thenReturn(furnishedStatus)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockPropertyOwnershipService).updateFurnishedStatus(
            id = propertyId,
            furnishedStatus = furnishedStatus,
            initialLastModifiedDate = initialLastModifiedDate,
        )
    }

    @Test
    fun `resolveNextDestination calls deleteJourney on state`() {
        // Act
        stepConfig.resolveNextDestination(mockState, Destination.ExternalUrl("redirect"))

        // Assert
        verify(mockState).deleteJourney()
    }
}
