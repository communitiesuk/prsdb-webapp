package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.EpcState
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData

@ExtendWith(MockitoExtension::class)
class EpcExpiryCheckStepConfigTests {
    @Mock
    lateinit var mockEpcState: EpcState

    val routeSegment = EpcExpiryCheckStep.ROUTE_SEGMENT

    @Test
    fun `mode returns null when form model is not present`() {
        // Arrange
        val stepConfig = setupStepConfig()

        // Act
        val result = stepConfig.mode(mockEpcState)

        // Assert
        assertNull(result)
    }

    @Test
    fun `mode returns null when tenancyStartedBeforeExpiry is null`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockEpcState.getStepData(routeSegment)).thenReturn(emptyMap())

        // Act
        val result = stepConfig.mode(mockEpcState)

        // Assert
        assertNull(result)
    }

    @Test
    fun `mode returns EPC_EXPIRED when tenancyStartedBeforeExpiry is false`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockEpcState.getStepData(routeSegment)).thenReturn(mapOf("tenancyStartedBeforeExpiry" to false))

        // Act
        val result = stepConfig.mode(mockEpcState)

        // Assert
        assertEquals(EpcExpiryCheckMode.EPC_EXPIRED, result)
    }

    @Test
    fun `mode throws when checking an acceptedEpc which is null`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockEpcState.getStepData(routeSegment)).thenReturn(mapOf("tenancyStartedBeforeExpiry" to true))
        whenever(mockEpcState.acceptedEpc).thenReturn(null)

        // Act, Assert
        assertThrows<PrsdbWebException> { stepConfig.mode(mockEpcState) }
    }

    @Test
    fun `mode returns EPC_COMPLIANT when tenancyStartedBeforeExpiry is true and energy rating higher than E`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val epcDetails =
            MockEpcData.createEpcDataModel(
                energyRating = "B",
            )
        whenever(mockEpcState.acceptedEpc).thenReturn(epcDetails)
        whenever(mockEpcState.getStepData(routeSegment)).thenReturn(mapOf("tenancyStartedBeforeExpiry" to true))

        // Act
        val result = stepConfig.mode(mockEpcState)

        // Assert
        assertEquals(EpcExpiryCheckMode.EPC_COMPLIANT, result)
    }

    @Test
    fun `mode returns EPC_COMPLIANT when tenancyStartedBeforeExpiry is true and energy rating is E (boundary)`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val epcDetails =
            MockEpcData.createEpcDataModel(
                energyRating = "E",
            )
        whenever(mockEpcState.acceptedEpc).thenReturn(epcDetails)
        whenever(mockEpcState.getStepData(routeSegment)).thenReturn(mapOf("tenancyStartedBeforeExpiry" to true))

        // Act
        val result = stepConfig.mode(mockEpcState)

        // Assert
        assertEquals(EpcExpiryCheckMode.EPC_COMPLIANT, result)
    }

    @Test
    fun `mode returns EPC_LOW_ENERGY_RATING when tenancyStartedBeforeExpiry is true and energy rating is worse than E`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val epcDetails =
            MockEpcData.createEpcDataModel(
                energyRating = "F",
            )
        whenever(mockEpcState.acceptedEpc).thenReturn(epcDetails)
        whenever(mockEpcState.getStepData(routeSegment)).thenReturn(mapOf("tenancyStartedBeforeExpiry" to true))

        // Act
        val result = stepConfig.mode(mockEpcState)

        // Assert
        assertEquals(EpcExpiryCheckMode.EPC_LOW_ENERGY_RATING, result)
    }

    private fun setupStepConfig(): EpcExpiryCheckStepConfig {
        val stepConfig = EpcExpiryCheckStepConfig()
        stepConfig.routeSegment = routeSegment
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
