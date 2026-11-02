package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.PropertyManagerState
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class RemovePropertyManagerStepConfigTests {
    @Mock
    lateinit var mockState: PropertyManagerState

    @Mock
    lateinit var mockPropertyManagerDetailsStep: PropertyManagerDetailsStep

    @Test
    fun `afterStepDataIsAdded clears the property manager details when the user confirms removal`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(RemovePropertyManagerStep.ROUTE_SEGMENT)).thenReturn(mapOf("answer" to "true"))
        whenever(mockState.propertyManagerDetailsStep).thenReturn(mockPropertyManagerDetailsStep)

        stepConfig.afterStepDataIsAdded(mockState)

        verify(mockPropertyManagerDetailsStep).clearFormData()
    }

    @Test
    fun `afterStepDataIsAdded keeps the property manager details when the user does not confirm removal`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(RemovePropertyManagerStep.ROUTE_SEGMENT)).thenReturn(mapOf("answer" to "false"))

        stepConfig.afterStepDataIsAdded(mockState)

        verify(mockState, never()).propertyManagerDetailsStep
    }

    private fun setupStepConfig(): RemovePropertyManagerStepConfig {
        val stepConfig = RemovePropertyManagerStepConfig()
        stepConfig.urlPath = RemovePropertyManagerStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
