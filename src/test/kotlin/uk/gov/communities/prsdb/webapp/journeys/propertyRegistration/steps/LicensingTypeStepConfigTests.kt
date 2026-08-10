package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.CONTINUE_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_THIS_LATER_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.journeys.UnrecoverableJourneyStateException
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.LicensingState
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class LicensingTypeStepConfigTests {
    @Mock
    lateinit var mockState: LicensingState

    @Mock
    lateinit var featureFlagManager: FeatureFlagManager

    val routeSegment = LicensingTypeStep.ROUTE_SEGMENT

    @Test
    fun `mode returns null when form model is not present`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(routeSegment)).thenReturn(null)

        val result = stepConfig.mode(mockState)

        assertNull(result)
    }

    @Test
    fun `mode returns null when licensingType is null and action is not provideThisLater`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(routeSegment))
            .thenReturn(mapOf("licensingType" to null, "action" to CONTINUE_BUTTON_ACTION_NAME))

        val result = stepConfig.mode(mockState)

        assertNull(result)
    }

    @Test
    fun `mode returns SELECTIVE_LICENCE when licensingType is SELECTIVE_LICENCE`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(routeSegment))
            .thenReturn(mapOf("licensingType" to "SELECTIVE_LICENCE", "action" to CONTINUE_BUTTON_ACTION_NAME))

        val result = stepConfig.mode(mockState)

        assertEquals(LicensingTypeMode.SELECTIVE_LICENCE, result)
    }

    @Test
    fun `mode returns PROVIDE_LATER when action is provideThisLater and route is allowed and FF is on`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.allowProvideLicensingLaterRoute).thenReturn(true)
        whenever(featureFlagManager.checkFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)).thenReturn(true)
        whenever(mockState.getStepData(routeSegment))
            .thenReturn(mapOf("licensingType" to null, "action" to PROVIDE_THIS_LATER_BUTTON_ACTION_NAME))

        val result = stepConfig.mode(mockState)

        assertEquals(LicensingTypeMode.PROVIDE_LATER, result)
    }

    @Test
    fun `mode throws UnrecoverableJourneyStateException when action is provideThisLater but allowProvideLicensingLaterRoute is false`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.allowProvideLicensingLaterRoute).thenReturn(false)
        whenever(mockState.journeyId).thenReturn("test-journey-id")
        whenever(mockState.getStepData(routeSegment))
            .thenReturn(mapOf("licensingType" to null, "action" to PROVIDE_THIS_LATER_BUTTON_ACTION_NAME))

        assertThrows<UnrecoverableJourneyStateException> { stepConfig.mode(mockState) }
    }

    @Test
    fun `mode throws UnrecoverableJourneyStateException when action is provideThisLater but FF is off`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.allowProvideLicensingLaterRoute).thenReturn(true)
        whenever(featureFlagManager.checkFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)).thenReturn(false)
        whenever(mockState.journeyId).thenReturn("test-journey-id")
        whenever(mockState.getStepData(routeSegment))
            .thenReturn(mapOf("licensingType" to null, "action" to PROVIDE_THIS_LATER_BUTTON_ACTION_NAME))

        assertThrows<UnrecoverableJourneyStateException> { stepConfig.mode(mockState) }
    }

    private fun setupStepConfig(): LicensingTypeStepConfig {
        val stepConfig = LicensingTypeStepConfig(featureFlagManager)
        stepConfig.urlPath = routeSegment
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
