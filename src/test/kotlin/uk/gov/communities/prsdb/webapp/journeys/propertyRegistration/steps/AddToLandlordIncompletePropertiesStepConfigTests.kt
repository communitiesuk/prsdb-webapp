package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.services.IncompletePropertyForLandlordService

@ExtendWith(MockitoExtension::class)
class AddToLandlordIncompletePropertiesStepConfigTests {
    @Mock
    private lateinit var mockIncompletePropertyForLandlordService: IncompletePropertyForLandlordService

    private lateinit var stepConfig: AddToLandlordIncompletePropertiesStepConfig

    @BeforeEach
    fun setUp() {
        stepConfig = AddToLandlordIncompletePropertiesStepConfig(mockIncompletePropertyForLandlordService)
    }

    @Test
    fun `afterSaveState calls addIncompletePropertyToLandlord with the saved journey state`() {
        // Arrange
        val mockState = mock<JourneyState>()
        val mockSavedJourneyState = mock<SavedJourneyState>()

        // Act
        stepConfig.afterSaveState(mockState, mockSavedJourneyState)

        // Assert
        verify(mockIncompletePropertyForLandlordService).addIncompletePropertyToLandlord(mockSavedJourneyState)
    }
}
