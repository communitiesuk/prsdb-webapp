package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.NullSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_THIS_LATER_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.journeys.UnrecoverableJourneyStateException
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyDetailState
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class HasGasSupplyStepConfigTests {
    @Mock
    lateinit var mockJourneyState: GasSafetyDetailState

    val routeSegment = HasGasSupplyStep.ROUTE_SEGMENT

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
    fun `mode returns null when hasGasSupply is null and action is not provideThisLater`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockJourneyState.getStepData(routeSegment))
            .thenReturn(mapOf("hasGasSupply" to null, "action" to "saveAndContinue"))

        // Act
        val result = stepConfig.mode(mockJourneyState)

        // Assert
        assertNull(result)
    }

    @Test
    fun `mode returns HAS_SUPPLY when hasGasSupply is true and action is not provideThisLater`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockJourneyState.getStepData(routeSegment))
            .thenReturn(mapOf("hasGasSupply" to "true", "action" to "saveAndContinue"))

        // Act
        val result = stepConfig.mode(mockJourneyState)

        // Assert
        assertEquals(HasGasSupplyMode.HAS_SUPPLY, result)
    }

    @Test
    fun `mode returns NO_SUPPLY when hasGasSupply is false and action is not provideThisLater`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockJourneyState.getStepData(routeSegment))
            .thenReturn(mapOf("hasGasSupply" to "false", "action" to "saveAndContinue"))

        // Act
        val result = stepConfig.mode(mockJourneyState)

        // Assert
        assertEquals(HasGasSupplyMode.NO_SUPPLY, result)
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(booleans = [true, false])
    fun `mode returns PROVIDE_LATER when action is provideThisLater and allowProvideCertificateLaterRoute is true`(
        hasGasSupply: Boolean?,
    ) {
        whenever(mockJourneyState.allowProvideCertificateLaterRoute).thenReturn(true)

        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockJourneyState.getStepData(routeSegment)).thenReturn(
            mapOf("hasGasSupply" to hasGasSupply, "action" to PROVIDE_THIS_LATER_BUTTON_ACTION_NAME),
        )

        // Act
        val result = stepConfig.mode(mockJourneyState)

        // Assert
        assertEquals(HasGasSupplyMode.PROVIDE_LATER, result)
    }

    @Test
    fun `mode throws an error when action is provideThisLater but allowProvideCertificateLaterRoute is false`() {
        // Arrange
        val stepConfig = setupStepConfig()

        whenever(mockJourneyState.allowProvideCertificateLaterRoute).thenReturn(false)
        whenever(mockJourneyState.journeyId).thenReturn("test-journey-id")
        whenever(mockJourneyState.getStepData(routeSegment)).thenReturn(
            mapOf("hasGasSupply" to "true", "action" to PROVIDE_THIS_LATER_BUTTON_ACTION_NAME),
        )

        // Act, assert
        assertThrows<UnrecoverableJourneyStateException> { stepConfig.mode(mockJourneyState) }
    }

    private fun setupStepConfig(): HasGasSupplyStepConfig {
        val stepConfig = HasGasSupplyStepConfig()
        stepConfig.urlPath = routeSegment
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
