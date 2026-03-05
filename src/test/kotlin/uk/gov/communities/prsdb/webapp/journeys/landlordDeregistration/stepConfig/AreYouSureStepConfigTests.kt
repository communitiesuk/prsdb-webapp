package uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.stepConfig

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.landlordDeregistration.LandlordDeregistrationJourneyState
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class AreYouSureStepConfigTests {
    @Mock
    lateinit var mockState: LandlordDeregistrationJourneyState

    @Test
    fun `mode returns null when form model is not present`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(AreYouSureStep.ROUTE_SEGMENT)).thenReturn(null)

        val result = stepConfig.mode(mockState)

        assertNull(result)
    }

    @Test
    fun `mode returns null when wantsToProceed is null`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(AreYouSureStep.ROUTE_SEGMENT)).thenReturn(mapOf("wantsToProceed" to null))

        val result = stepConfig.mode(mockState)

        assertNull(result)
    }

    @Test
    fun `mode returns WANTS_TO_PROCEED when wantsToProceed is true`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(AreYouSureStep.ROUTE_SEGMENT)).thenReturn(mapOf("wantsToProceed" to "true"))

        val result = stepConfig.mode(mockState)

        assertEquals(AreYouSureMode.WANTS_TO_PROCEED, result)
    }

    @Test
    fun `mode returns DOES_NOT_WANT_TO_PROCEED when wantsToProceed is false`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(AreYouSureStep.ROUTE_SEGMENT)).thenReturn(mapOf("wantsToProceed" to "false"))

        val result = stepConfig.mode(mockState)

        assertEquals(AreYouSureMode.DOES_NOT_WANT_TO_PROCEED, result)
    }

    @Test
    fun `enrichSubmittedDataBeforeValidation adds userHasRegisteredProperties from state`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.userHasRegisteredProperties).thenReturn(true)

        val result = stepConfig.enrichSubmittedDataBeforeValidation(mockState, mapOf("wantsToProceed" to "true"))

        assertTrue(result["userHasRegisteredProperties"] as Boolean)
    }

    @Test
    fun `enrichSubmittedDataBeforeValidation adds false when user has no registered properties`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.userHasRegisteredProperties).thenReturn(false)

        val result = stepConfig.enrichSubmittedDataBeforeValidation(mockState, mapOf("wantsToProceed" to "true"))

        assertEquals(false, result["userHasRegisteredProperties"])
    }

    private fun setupStepConfig(): AreYouSureStepConfig {
        val stepConfig = AreYouSureStepConfig()
        stepConfig.routeSegment = AreYouSureStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
