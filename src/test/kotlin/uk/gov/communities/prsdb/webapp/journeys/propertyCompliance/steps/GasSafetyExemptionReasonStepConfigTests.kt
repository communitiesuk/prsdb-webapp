package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class GasSafetyExemptionReasonStepConfigTests {
    @Mock
    lateinit var mockJourneyState: JourneyState

    val routeSegment = GasSafetyExemptionReasonStep.ROUTE_SEGMENT

    @Test
    fun `mode returns null when form model is not present`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockJourneyState.getStepData(routeSegment)).thenReturn(null)

        // Act
        val result = stepConfig.mode(mockJourneyState)

        // Assert
        assertNull(result)
    }

    @Test
    fun `mode returns OTHER_REASON_SELECTED when exemptionReason is OTHER`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockJourneyState.getStepData(routeSegment))
            .thenReturn(mapOf("exemptionReason" to GasSafetyExemptionReason.OTHER))

        // Act
        val result = stepConfig.mode(mockJourneyState)

        // Assert
        assertEquals(GasSafetyExemptionReasonMode.OTHER_REASON_SELECTED, result)
    }

    @Test
    fun `mode returns LISTED_REASON_SELECTED when exemptionReason is NO_GAS_SUPPLY`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockJourneyState.getStepData(routeSegment))
            .thenReturn(mapOf("exemptionReason" to GasSafetyExemptionReason.NO_GAS_SUPPLY))

        // Act
        val result = stepConfig.mode(mockJourneyState)

        // Assert
        assertEquals(GasSafetyExemptionReasonMode.LISTED_REASON_SELECTED, result)
    }

    @Test
    fun `mode returns LISTED_REASON_SELECTED when exemptionReason is LONG_LEASE`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockJourneyState.getStepData(routeSegment))
            .thenReturn(mapOf("exemptionReason" to GasSafetyExemptionReason.LONG_LEASE))

        // Act
        val result = stepConfig.mode(mockJourneyState)

        // Assert
        assertEquals(GasSafetyExemptionReasonMode.LISTED_REASON_SELECTED, result)
    }

    private fun setupStepConfig(): GasSafetyExemptionReasonStepConfig {
        val stepConfig = GasSafetyExemptionReasonStepConfig()
        stepConfig.routeSegment = routeSegment
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
