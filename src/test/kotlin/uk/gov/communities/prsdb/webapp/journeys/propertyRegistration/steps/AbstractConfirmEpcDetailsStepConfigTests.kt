package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData

@ExtendWith(MockitoExtension::class)
class AbstractConfirmEpcDetailsStepConfigTests {
    @Mock
    lateinit var mockState: EpcState

    private val routeSegment = "test-route-segment"

    private fun setupStepConfig(usingEpc: EpcDataModel? = null): AbstractConfirmEpcDetailsStepConfig {
        val stepConfig =
            object : AbstractConfirmEpcDetailsStepConfig() {
                override fun getStepSpecificContent(state: EpcState) = emptyMap<String, Any?>()

                override fun chooseTemplate(state: EpcState) = ""
            }
        stepConfig.usingEpc { usingEpc }
        stepConfig.routeSegment = routeSegment
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }

    @Nested
    inner class Mode {
        @Test
        fun `returns null when form model is not present`() {
            // Arrange
            val stepConfig = setupStepConfig()

            // Act
            val result = stepConfig.mode(mockState)

            // Assert
            assertNull(result)
        }

        @Test
        fun `returns null when matchedEpcIsCorrect is null`() {
            // Arrange
            val stepConfig = setupStepConfig()
            whenever(mockState.getStepData(routeSegment)).thenReturn(emptyMap())

            // Act
            val result = stepConfig.mode(mockState)

            // Assert
            assertNull(result)
        }

        @Test
        fun `returns NO when matchedEpcIsCorrect is false`() {
            // Arrange
            val stepConfig = setupStepConfig()
            whenever(mockState.getStepData(routeSegment)).thenReturn(mapOf("matchedEpcIsCorrect" to false))

            // Act
            val result = stepConfig.mode(mockState)

            // Assert
            assertEquals(YesOrNo.NO, result)
        }

        @Test
        fun `returns YES when matchedEpcIsCorrect is true`() {
            // Arrange
            val stepConfig = setupStepConfig()
            whenever(mockState.getStepData(routeSegment)).thenReturn(mapOf("matchedEpcIsCorrect" to true))

            // Act
            val result = stepConfig.mode(mockState)

            // Assert
            assertEquals(YesOrNo.YES, result)
        }
    }

    @Nested
    inner class AfterStepDataIsAdded {
        @Test
        fun `sets acceptedEpc when matchedEpcIsCorrect is true`() {
            // Arrange
            val epcData = MockEpcData.createEpcDataModel()
            val stepConfig = setupStepConfig(epcData)
            whenever(mockState.getStepData(routeSegment)).thenReturn(mapOf("matchedEpcIsCorrect" to true))

            // Act
            stepConfig.afterStepDataIsAdded(mockState)

            // Assert
            verify(mockState).acceptedEpc = epcData
        }

        @Test
        fun `clears acceptedEpc when matchedEpcIsCorrect is false and acceptedEpc matches the relevant EPC`() {
            // Arrange
            val epcData = MockEpcData.createEpcDataModel()
            val stepConfig = setupStepConfig(epcData)
            whenever(mockState.getStepData(routeSegment)).thenReturn(mapOf("matchedEpcIsCorrect" to false))
            whenever(mockState.acceptedEpc).thenReturn(epcData)

            // Act
            stepConfig.afterStepDataIsAdded(mockState)

            // Assert
            verify(mockState).acceptedEpc = null
        }

        @Test
        fun `does not clear acceptedEpc when matchedEpcIsCorrect is false and acceptedEpc does not match the relevant EPC`() {
            // Arrange
            val relevantEpc = MockEpcData.createEpcDataModel()
            val differentEpc = MockEpcData.createEpcDataModel(certificateNumber = MockEpcData.SECONDARY_EPC_CERTIFICATE_NUMBER)
            val stepConfig = setupStepConfig(relevantEpc)
            whenever(mockState.getStepData(routeSegment)).thenReturn(mapOf("matchedEpcIsCorrect" to false))
            whenever(mockState.acceptedEpc).thenReturn(differentEpc)

            // Act
            stepConfig.afterStepDataIsAdded(mockState)

            // Assert
            verify(mockState, never()).acceptedEpc = null
        }

        @Test
        fun `does not modify acceptedEpc when form model is not present`() {
            // Arrange
            val stepConfig = setupStepConfig(MockEpcData.createEpcDataModel())

            // Act
            stepConfig.afterStepDataIsAdded(mockState)

            // Assert
            verify(mockState, never()).acceptedEpc = MockEpcData.createEpcDataModel()
            verify(mockState, never()).acceptedEpc = null
        }
    }
}
