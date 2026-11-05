package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.PropertyManagerState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyManagerDetailsFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel
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
        whenever(mockState.getStepData(RemovePropertyManagerStep.ROUTE_SEGMENT)).thenReturn(mapOf("wantsToProceed" to "true"))
        whenever(mockState.propertyManagerDetailsStep).thenReturn(mockPropertyManagerDetailsStep)

        stepConfig.afterStepDataIsAdded(mockState)

        verify(mockPropertyManagerDetailsStep).clearFormData()
    }

    @Test
    fun `afterStepDataIsAdded keeps the property manager details when the user does not confirm removal`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(RemovePropertyManagerStep.ROUTE_SEGMENT)).thenReturn(mapOf("wantsToProceed" to "false"))

        stepConfig.afterStepDataIsAdded(mockState)

        verify(mockState, never()).propertyManagerDetailsStep
    }

    @Test
    fun `getStepSpecificContent includes the property manager's name and yes or no radios`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.propertyManagerDetailsStep).thenReturn(mockPropertyManagerDetailsStep)
        whenever(mockPropertyManagerDetailsStep.formModel).thenReturn(
            PropertyManagerDetailsFormModel().apply { name = "Jerry Mouse" },
        )

        val content = stepConfig.getStepSpecificContent(mockState)

        assertEquals(
            mapOf(
                "fieldSetHeadingParam" to "Jerry Mouse",
                "radioOptions" to RadiosViewModel.yesOrNoRadios(),
            ),
            content,
        )
    }

    private fun setupStepConfig(): RemovePropertyManagerStepConfig {
        val stepConfig = RemovePropertyManagerStepConfig()
        stepConfig.urlPath = RemovePropertyManagerStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
