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
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyManagerDetailsFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class RemovePropertyManagerStepConfigTests {
    @Mock
    lateinit var mockState: PropertyManagerState

    @Mock
    lateinit var mockPropertyManagerDetailsStep: PropertyManagerDetailsStep

    @Mock
    lateinit var mockRemovePropertyManagerStep: RemovePropertyManagerStep

    @Test
    fun `afterStepDataIsAdded clears the property manager details and the removal answer when the user confirms removal`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(RemovePropertyManagerStep.ROUTE_SEGMENT)).thenReturn(mapOf("wantsToProceed" to "true"))
        whenever(mockState.propertyManagerDetailsStep).thenReturn(mockPropertyManagerDetailsStep)
        whenever(mockState.removePropertyManagerStep).thenReturn(mockRemovePropertyManagerStep)

        stepConfig.afterStepDataIsAdded(mockState)

        verify(mockPropertyManagerDetailsStep).clearFormData()
        verify(mockRemovePropertyManagerStep).clearFormData()
    }

    @Test
    fun `afterStepDataIsAdded clears only the removal answer when the user does not confirm removal`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(RemovePropertyManagerStep.ROUTE_SEGMENT)).thenReturn(mapOf("wantsToProceed" to "false"))
        whenever(mockState.removePropertyManagerStep).thenReturn(mockRemovePropertyManagerStep)

        stepConfig.afterStepDataIsAdded(mockState)

        verify(mockState, never()).propertyManagerDetailsStep
        verify(mockRemovePropertyManagerStep).clearFormData()
    }

    @Test
    fun `mode is YES when the property manager details have been cleared`() {
        whenever(mockState.propertyManagerDetailsStep).thenReturn(mockPropertyManagerDetailsStep)
        whenever(mockPropertyManagerDetailsStep.formModelOrNull).thenReturn(null)

        assertEquals(YesOrNo.YES, setupStepConfig().mode(mockState))
    }

    @Test
    fun `mode is NO when the property manager details are still present`() {
        whenever(mockState.propertyManagerDetailsStep).thenReturn(mockPropertyManagerDetailsStep)
        whenever(mockPropertyManagerDetailsStep.formModelOrNull).thenReturn(PropertyManagerDetailsFormModel())

        assertEquals(YesOrNo.NO, setupStepConfig().mode(mockState))
    }

    @Test
    fun `getStepSpecificContent includes the property manager's name, hint, radios and section header`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.propertyManagerDetailsStep).thenReturn(mockPropertyManagerDetailsStep)
        whenever(mockPropertyManagerDetailsStep.formModel).thenReturn(
            PropertyManagerDetailsFormModel().apply { name = "Jerry Mouse" },
        )

        val content = stepConfig.getStepSpecificContent(mockState)

        assertEquals(
            mapOf(
                "fieldSetHeading" to "registerProperty.propertyManager.remove.fieldSetHeading",
                "fieldSetHint" to "registerProperty.propertyManager.remove.fieldSetHint",
                "optionalFieldSetHeadingParam" to "Jerry Mouse",
                "radioOptions" to RadiosViewModel.yesOrNoRadios(),
                "submitButtonTextKey" to "forms.buttons.saveAndContinue",
                "showSectionHeader" to true,
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
