package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_GAS_SAFETY_URL
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyDetailState
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class ProvideGasCertLaterStepConfigTests {
    @Mock
    lateinit var mockState: GasSafetyDetailState

    @Mock
    lateinit var featureFlagManager: FeatureFlagManager

    @InjectMocks
    lateinit var stepConfig: ProvideGasCertLaterStepConfig

    @Test
    fun `chooseTemplate returns legacy occupied template when letting agents is disabled and isOccupied is true`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.isOccupied).thenReturn(true)
        whenever(featureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)).thenReturn(false)

        // Act
        val result = stepConfig.chooseTemplate(mockState)

        // Assert
        assertEquals("forms/provideGasCertificateLaterForOccupiedPropertyBeforeLettingAgents", result)
    }

    @Test
    fun `chooseTemplate returns legacy unoccupied template when letting agents is disabled and isOccupied is false`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.isOccupied).thenReturn(false)
        whenever(featureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)).thenReturn(false)

        // Act
        val result = stepConfig.chooseTemplate(mockState)

        // Assert
        assertEquals("forms/provideGasCertificateLaterForUnoccupiedPropertyBeforeLettingAgents", result)
    }

    @Test
    fun `chooseTemplate returns occupied template when letting agents is enabled and isOccupied is true`() {
        whenever(mockState.isOccupied).thenReturn(true)
        whenever(featureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)).thenReturn(true)

        assertEquals("forms/provideGasCertificateLaterForOccupiedProperty", setupStepConfig().chooseTemplate(mockState))
    }

    @Test
    fun `chooseTemplate returns unoccupied template when letting agents is enabled and isOccupied is false`() {
        whenever(mockState.isOccupied).thenReturn(false)
        whenever(featureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)).thenReturn(true)

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
