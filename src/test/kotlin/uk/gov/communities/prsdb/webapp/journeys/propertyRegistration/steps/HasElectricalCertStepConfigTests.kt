package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.NullSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_THIS_LATER_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class HasElectricalCertStepConfigTests {
    @Mock
    lateinit var mockJourneyState: JourneyState

    val routeSegment = HasElectricalCertStep.ROUTE_SEGMENT

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
    fun `mode returns null when electricalCertType is null and action is not provideThisLater`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockJourneyState.getStepData(routeSegment))
            .thenReturn(mapOf("electricalCertType" to null, "action" to "saveAndContinue"))

        // Act
        val result = stepConfig.mode(mockJourneyState)

        // Assert
        assertNull(result)
    }

    @Test
    fun `mode returns HAS_EIC when electricalCertType is EIC and action is not provideThisLater`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockJourneyState.getStepData(routeSegment))
            .thenReturn(mapOf("electricalCertType" to "EIC", "action" to "saveAndContinue"))

        // Act
        val result = stepConfig.mode(mockJourneyState)

        // Assert
        assertEquals(HasElectricalCertMode.HAS_EIC, result)
    }

    @Test
    fun `mode returns HAS_EICR when electricalCertType is EICR and action is not provideThisLater`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockJourneyState.getStepData(routeSegment))
            .thenReturn(mapOf("electricalCertType" to "EICR", "action" to "saveAndContinue"))

        // Act
        val result = stepConfig.mode(mockJourneyState)

        // Assert
        assertEquals(HasElectricalCertMode.HAS_EICR, result)
    }

    @Test
    fun `mode returns NO_CERTIFICATE when electricalCertType is NONE and action is not provideThisLater`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockJourneyState.getStepData(routeSegment))
            .thenReturn(mapOf("electricalCertType" to "NONE", "action" to "saveAndContinue"))

        // Act
        val result = stepConfig.mode(mockJourneyState)

        // Assert
        assertEquals(HasElectricalCertMode.NO_CERTIFICATE, result)
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = ["EIC", "EICR", "NONE"])
    fun `mode returns PROVIDE_THIS_LATER when action is provideThisLater`(electricalCertType: String?) {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockJourneyState.getStepData(routeSegment)).thenReturn(
            mapOf("electricalCertType" to electricalCertType, "action" to PROVIDE_THIS_LATER_BUTTON_ACTION_NAME),
        )

        // Act
        val result = stepConfig.mode(mockJourneyState)

        // Assert
        assertEquals(HasElectricalCertMode.PROVIDE_THIS_LATER, result)
    }

    private fun setupStepConfig(): HasElectricalCertStepConfig {
        val stepConfig = HasElectricalCertStepConfig()
        stepConfig.routeSegment = routeSegment
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
