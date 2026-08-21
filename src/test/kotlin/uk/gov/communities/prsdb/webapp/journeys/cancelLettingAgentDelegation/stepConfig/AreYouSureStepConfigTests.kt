package uk.gov.communities.prsdb.webapp.journeys.cancelLettingAgentDelegation.stepConfig

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.cancelLettingAgentDelegation.CancelLettingAgentDelegationJourneyState
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class AreYouSureStepConfigTests {
    @Mock
    lateinit var mockState: CancelLettingAgentDelegationJourneyState

    @Test
    fun `chooseTemplate returns the shared areYouSureForm template`() {
        assertEquals("forms/areYouSureForm", setupStepConfig().chooseTemplate(mockState))
    }

    @Test
    fun `mode returns null when the form has not been submitted`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(AreYouSureStep.ROUTE_SEGMENT)).thenReturn(null)

        assertNull(stepConfig.mode(mockState))
    }

    @Test
    fun `mode returns null when wantsToProceed has not been answered`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(AreYouSureStep.ROUTE_SEGMENT)).thenReturn(emptyMap())

        assertNull(stepConfig.mode(mockState))
    }

    @Test
    fun `mode returns WANTS_TO_PROCEED when wantsToProceed is true`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(AreYouSureStep.ROUTE_SEGMENT))
            .thenReturn(mapOf("wantsToProceed" to "true"))

        assertEquals(AreYouSureMode.WANTS_TO_PROCEED, stepConfig.mode(mockState))
    }

    @Test
    fun `mode returns DOES_NOT_WANT_TO_PROCEED when wantsToProceed is false`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(AreYouSureStep.ROUTE_SEGMENT))
            .thenReturn(mapOf("wantsToProceed" to "false"))

        assertEquals(AreYouSureMode.DOES_NOT_WANT_TO_PROCEED, stepConfig.mode(mockState))
    }

    @Test
    fun `getStepSpecificContent uses the letting agent email as the heading parameter`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.lettingAgentEmail).thenReturn("letting.agent.one@example.com")

        val content = stepConfig.getStepSpecificContent(mockState)

        assertEquals("cancelLettingAgentDelegation.areYouSure.fieldSetHeading", content["fieldSetHeading"])
        assertEquals("letting.agent.one@example.com", content["optionalFieldSetHeadingParam"])
        assertEquals("cancelLettingAgentDelegation.areYouSure.fieldSetHint", content["fieldSetHint"])
        assertTrue(content.containsKey("radioOptions"))
    }

    @Test
    fun `getStepSpecificContent renders a Confirm button with no cancel link`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.lettingAgentEmail).thenReturn("letting.agent.one@example.com")

        val content = stepConfig.getStepSpecificContent(mockState)

        assertEquals("cancelLettingAgentDelegation.areYouSure.confirmButton", content["submitButtonTextKey"])
        assertFalse(content["showCancelLink"] as Boolean)
    }

    private fun setupStepConfig(): AreYouSureStepConfig {
        val stepConfig = AreYouSureStepConfig()
        stepConfig.urlPath = AreYouSureStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
