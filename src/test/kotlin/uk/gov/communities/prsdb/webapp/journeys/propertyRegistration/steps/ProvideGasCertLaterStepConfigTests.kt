package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_GAS_SAFETY_URL
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyDetailState
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class ProvideGasCertLaterStepConfigTests {
    @Mock
    lateinit var mockState: GasSafetyDetailState

    @InjectMocks
    lateinit var stepConfig: ProvideGasCertLaterStepConfig

    @Test
    fun `chooseTemplate returns occupied template when isOccupied is true`() {
        whenever(mockState.isOccupied).thenReturn(true)

        assertEquals("forms/provideGasCertificateLaterForOccupiedProperty", setupStepConfig().chooseTemplate(mockState))
    }

    @Test
    fun `chooseTemplate returns unoccupied template when isOccupied is false`() {
        whenever(mockState.isOccupied).thenReturn(false)

        assertEquals("forms/provideGasCertificateLaterForUnoccupiedProperty", setupStepConfig().chooseTemplate(mockState))
    }

    @Test
    fun `getStepSpecificContent includes the landlord gas safety URL`() {
        val content = setupStepConfig().getStepSpecificContent(mockState)

        assertEquals(LANDLORD_GAS_SAFETY_URL, content["landlordGasSafetyUrl"])
    }

    @Test
    fun `getStepSpecificContent uses Continue when isOccupied is true`() {
        whenever(mockState.isOccupied).thenReturn(true)

        val content = setupStepConfig().getStepSpecificContent(mockState)

        assertEquals("forms.buttons.continue", content["submitButtonText"])
    }

    @Test
    fun `getStepSpecificContent uses Save and continue when isOccupied is false`() {
        whenever(mockState.isOccupied).thenReturn(false)

        val content = setupStepConfig().getStepSpecificContent(mockState)

        assertEquals("forms.buttons.saveAndContinue", content["submitButtonText"])
    }

    private fun setupStepConfig(): ProvideGasCertLaterStepConfig {
        stepConfig.urlPath = ProvideGasCertLaterStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
