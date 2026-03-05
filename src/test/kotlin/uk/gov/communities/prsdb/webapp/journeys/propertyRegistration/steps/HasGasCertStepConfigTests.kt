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
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class HasGasCertStepConfigTests {
    @Mock
    lateinit var mockJourneyState: JourneyState

    val routeSegment = HasGasCertStep.ROUTE_SEGMENT

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
    fun `mode returns null when hasCert is null and action is not provideThisLater`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockJourneyState.getStepData(routeSegment))
            .thenReturn(mapOf("hasCert" to null, "action" to "saveAndContinue"))

        // Act
        val result = stepConfig.mode(mockJourneyState)

        // Assert
        assertNull(result)
    }

    @Test
    fun `mode returns HAS_CERTIFICATE when hasCert is true and action is not provideThisLater`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockJourneyState.getStepData(routeSegment))
            .thenReturn(mapOf("hasCert" to "true", "action" to "saveAndContinue"))

        // Act
        val result = stepConfig.mode(mockJourneyState)

        // Assert
        assertEquals(HasGasCertMode.HAS_CERTIFICATE, result)
    }

    @Test
    fun `mode returns NO_CERTIFICATE when hasCert is false and action is not provideThisLater`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockJourneyState.getStepData(routeSegment))
            .thenReturn(mapOf("hasCert" to "false", "action" to "saveAndContinue"))

        // Act
        val result = stepConfig.mode(mockJourneyState)

        // Assert
        assertEquals(HasGasCertMode.NO_CERTIFICATE, result)
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(booleans = [true, false])
    fun `mode returns PROVIDE_THIS_LATER when action is provideThisLater`(hasCert: Boolean?) {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockJourneyState.getStepData(routeSegment)).thenReturn(
            mapOf("hasCert" to hasCert, "action" to "provideThisLater"),
        )

        // Act
        val result = stepConfig.mode(mockJourneyState)

        // Assert
        assertEquals(HasGasCertMode.PROVIDE_THIS_LATER, result)
    }

    private fun setupStepConfig(): HasGasCertStepConfig {
        val stepConfig = HasGasCertStepConfig()
        stepConfig.routeSegment = routeSegment
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
