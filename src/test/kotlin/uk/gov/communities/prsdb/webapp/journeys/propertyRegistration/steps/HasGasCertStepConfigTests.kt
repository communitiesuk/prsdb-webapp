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
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_THIS_LATER_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.journeys.UnrecoverableJourneyStateException
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyDetailState
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class HasGasCertStepConfigTests {
    @Mock
    lateinit var mockJourneyState: GasSafetyDetailState

    @Mock
    lateinit var mockFeatureFlagManager: FeatureFlagManager

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
    fun `mode returns null when hasCert is null`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockJourneyState.getStepData(routeSegment)).thenReturn(mapOf("hasCert" to null))

        // Act
        val result = stepConfig.mode(mockJourneyState)

        // Assert
        assertNull(result)
    }

    @Test
    fun `mode returns HAS_CERTIFICATE when hasCert is true`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockJourneyState.getStepData(routeSegment)).thenReturn(mapOf("hasCert" to "true"))

        // Act
        val result = stepConfig.mode(mockJourneyState)

        // Assert
        assertEquals(HasGasCertMode.HAS_CERTIFICATE, result)
    }

    @Test
    fun `mode returns NO_CERTIFICATE when hasCert is false`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockJourneyState.getStepData(routeSegment)).thenReturn(mapOf("hasCert" to "false"))

        // Act
        val result = stepConfig.mode(mockJourneyState)

        // Assert
        assertEquals(HasGasCertMode.NO_CERTIFICATE, result)
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(booleans = [true, false])
    fun `mode returns PROVIDE_THIS_LATER when action is provideThisLater, allowProvideCertificateLaterRoute is true and flag is off`(
        hasCert: Boolean?,
    ) {
        whenever(mockJourneyState.allowProvideCertificateLaterRoute).thenReturn(true)
        whenever(mockFeatureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)).thenReturn(false)

        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockJourneyState.getStepData(routeSegment)).thenReturn(
            mapOf("hasCert" to hasCert, "action" to PROVIDE_THIS_LATER_BUTTON_ACTION_NAME),
        )

        // Act
        val result = stepConfig.mode(mockJourneyState)

        // Assert
        assertEquals(HasGasCertMode.PROVIDE_THIS_LATER, result)
    }

    @Test
    fun `mode throws an error when action is provideThisLater but allowProvideCertificateLaterRoute is false`() {
        // Arrange
        val stepConfig = setupStepConfig()

        whenever(mockJourneyState.allowProvideCertificateLaterRoute).thenReturn(false)
        whenever(mockJourneyState.journeyId).thenReturn("test-journey-id")
        whenever(mockJourneyState.getStepData(routeSegment)).thenReturn(
            mapOf("hasCert" to "true", "action" to PROVIDE_THIS_LATER_BUTTON_ACTION_NAME),
        )

        // Act, assert
        assertThrows<UnrecoverableJourneyStateException> { stepConfig.mode(mockJourneyState) }
    }

    @Test
    fun `mode throws an error when action is provideThisLater, allowProvideCertificateLaterRoute is true but flag is on`() {
        // Arrange
        val stepConfig = setupStepConfig()

        whenever(mockJourneyState.allowProvideCertificateLaterRoute).thenReturn(true)
        whenever(mockFeatureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)).thenReturn(true)
        whenever(mockJourneyState.journeyId).thenReturn("test-journey-id")
        whenever(mockJourneyState.getStepData(routeSegment)).thenReturn(
            mapOf("hasCert" to "true", "action" to PROVIDE_THIS_LATER_BUTTON_ACTION_NAME),
        )

        // Act, assert
        assertThrows<UnrecoverableJourneyStateException> { stepConfig.mode(mockJourneyState) }
    }

    private fun setupStepConfig(): HasGasCertStepConfig {
        val stepConfig = HasGasCertStepConfig(mockFeatureFlagManager)
        stepConfig.urlPath = routeSegment
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
