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
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BedroomsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.bedrooms.CompleteBedroomsUpdateStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.bedrooms.UpdateBedroomsJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfBedroomsFormModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@ExtendWith(MockitoExtension::class)
class CompleteBedroomsUpdateStepConfigTests {
    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockState: UpdateBedroomsJourneyState

    @Mock
    private lateinit var mockBedroomsStep: BedroomsStep

    @Mock
    private lateinit var stepConfig: CompleteBedroomsUpdateStepConfig

    @Mock
    private lateinit var mockNumberOfBedroomsFormModel: NumberOfBedroomsFormModel

    private val propertyId = 123L
    private val numberOfBedrooms = 3
    private val initialLastModifiedDate = Clock.System.now().toJavaInstant()

    @BeforeEach
    fun setUp() {
        stepConfig =
            CompleteBedroomsUpdateStepConfig(
                propertyOwnershipService = mockPropertyOwnershipService,
            )
        stepConfig.afterStepIsReached(mockState) // This initializes the childJourneyId
        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockState.bedrooms).thenReturn(mockBedroomsStep)
        whenever(mockState.lastModifiedDate).thenReturn(initialLastModifiedDate.toString())
        whenever(mockBedroomsStep.formModel).thenReturn(mockNumberOfBedroomsFormModel)
        whenever(mockNumberOfBedroomsFormModel.numberOfBedrooms).thenReturn(numberOfBedrooms.toString())
    }

    @Test
    fun `afterStepDataIsAdded calls updateHouseholdsAndTenants on propertyOwnershipService`() {
        // Act
        stepConfig.afterStepDataIsAdded(mockState)

        // Assert
        verify(mockPropertyOwnershipService).updateBedrooms(
            id = propertyId,
            numberOfBedrooms = numberOfBedrooms,
            initialLastModifiedDate = initialLastModifiedDate,
        )
    }
}
