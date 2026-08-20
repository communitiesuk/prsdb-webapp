package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class ConfirmChangeToLettingAgentStepConfigTests {
    @Mock
    lateinit var mockState: PropertyRegistrationJourneyState

    private val routeSegment = ConfirmChangeToLettingAgentStep.ROUTE_SEGMENT

    @Test
    fun `mode returns null when form model is not present in state`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(routeSegment)).thenReturn(null)

        val result = stepConfig.mode(mockState)

        assertNull(result)
    }

    @Test
    fun `mode returns COMPLETE when form model is present in state`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(routeSegment)).thenReturn(emptyMap())

        val result = stepConfig.mode(mockState)

        assertEquals(Complete.COMPLETE, result)
    }

    @Test
    fun `chooseTemplate returns whoProvidesChangeAreYouSureForm`() {
        val stepConfig = setupStepConfig()

        val result = stepConfig.chooseTemplate(mockState)

        assertEquals("forms/whoProvidesChangeAreYouSureForm", result)
    }

    private fun setupStepConfig(): ConfirmChangeToLettingAgentStepConfig {
        val stepConfig = ConfirmChangeToLettingAgentStepConfig()
        stepConfig.urlPath = routeSegment
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
