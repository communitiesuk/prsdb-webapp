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
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyDetailState
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class HasGasCertStepConfigTests {
    @Mock
    lateinit var mockJourneyState: GasSafetyDetailState

    val routeSegment = HasGasCertStep.ROUTE_SEGMENT

    private fun setupStepConfig(): HasGasCertStepConfig {
        val stepConfig = HasGasCertStepConfig()
        stepConfig.urlPath = routeSegment
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }

    @Test
    fun `mode returns null when form model is not present`() {
        val stepConfig = setupStepConfig()
        whenever(mockJourneyState.getStepData(routeSegment)).thenReturn(null)

        val result = stepConfig.mode(mockJourneyState)

        assertNull(result)
    }

    @Test
    fun `mode returns null when hasCert is null`() {
        val stepConfig = setupStepConfig()
        whenever(mockJourneyState.getStepData(routeSegment)).thenReturn(mapOf("hasCert" to null))

        val result = stepConfig.mode(mockJourneyState)

        assertNull(result)
    }

    @Test
    fun `mode returns YES when hasCert is true`() {
        val stepConfig = setupStepConfig()
        whenever(mockJourneyState.getStepData(routeSegment)).thenReturn(mapOf("hasCert" to "true"))

        val result = stepConfig.mode(mockJourneyState)

        assertEquals(HasGasCertMode.YES, result)
    }

    @Test
    fun `mode returns NO when hasCert is false`() {
        val stepConfig = setupStepConfig()
        whenever(mockJourneyState.getStepData(routeSegment)).thenReturn(mapOf("hasCert" to "false"))

        val result = stepConfig.mode(mockJourneyState)

        assertEquals(HasGasCertMode.NO, result)
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(booleans = [true, false])
    fun `getStepSpecificContent never shows the secondary submit button`(hasCert: Boolean?) {
        val stepConfig = setupStepConfig()

        val content = stepConfig.getStepSpecificContent(mockJourneyState)

        assertEquals(false, content["showSecondarySubmitButton"])
    }
}
