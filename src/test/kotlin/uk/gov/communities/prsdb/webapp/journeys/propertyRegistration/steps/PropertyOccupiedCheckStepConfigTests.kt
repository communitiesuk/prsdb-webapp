package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

@ExtendWith(MockitoExtension::class)
class PropertyOccupiedCheckStepConfigTests {
    @Mock
    lateinit var mockState: EpcState

    @Test
    fun `mode returns YES when the property is occupied`() {
        // Arrange
        val stepConfig = PropertyOccupiedCheckStepConfig()
        whenever(mockState.isOccupied).thenReturn(true)

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertEquals(YesOrNo.YES, result)
    }

    @Test
    fun `mode returns NO when the property is unoccupied`() {
        // Arrange
        val stepConfig = PropertyOccupiedCheckStepConfig()
        whenever(mockState.isOccupied).thenReturn(false)

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertEquals(YesOrNo.NO, result)
    }

    @Test
    fun `mode returns null when the occupancy status is not yet set`() {
        // Arrange
        val stepConfig = PropertyOccupiedCheckStepConfig()
        whenever(mockState.isOccupied).thenReturn(null)

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertEquals(null, result)
    }
}
