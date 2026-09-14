package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
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
    fun `chooseTemplate returns occupied template when letting agents is disabled`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.isOccupied).thenReturn(true)
        whenever(featureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)).thenReturn(false)

        // Act
        val result = stepConfig.chooseTemplate(mockState)

        // Assert
        assertEquals("forms/provideGasCertificateLaterForOccupiedProperty", result)
    }

    @Test
    fun `chooseTemplate returns unoccupied template when letting agents is disabled`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.isOccupied).thenReturn(false)
        whenever(featureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)).thenReturn(false)

        // Act
        val result = stepConfig.chooseTemplate(mockState)

        // Assert
        assertEquals("forms/provideGasCertificateLaterForUnoccupiedProperty", result)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `chooseTemplate returns shared template when letting agents is enabled`(isOccupied: Boolean) {
        whenever(mockState.isOccupied).thenReturn(isOccupied)
        whenever(featureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)).thenReturn(true)

        assertEquals("forms/provideGasSafetyDetailsLater", setupStepConfig().chooseTemplate(mockState))
    }

    @Test
    fun `chooseTemplate propagates an error reading occupancy`() {
        val stateException = IllegalStateException("Occupancy has not been set")
        whenever(mockState.isOccupied).thenThrow(stateException)

        val exception = assertThrows<IllegalStateException> { setupStepConfig().chooseTemplate(mockState) }

        assertSame(stateException, exception)
    }

    @Test
    fun `getStepSpecificContent includes occupied property details`() {
        whenever(mockState.isOccupied).thenReturn(true)

        val content = setupStepConfig().getStepSpecificContent(mockState)

        assertEquals(true, content["isOccupied"])
        assertEquals(LANDLORD_GAS_SAFETY_URL, content["landlordGasSafetyUrl"])
        assertEquals("forms.buttons.continue", content["submitButtonText"])
    }

    @Test
    fun `getStepSpecificContent includes unoccupied property details`() {
        whenever(mockState.isOccupied).thenReturn(false)

        val content = setupStepConfig().getStepSpecificContent(mockState)

        assertEquals(false, content["isOccupied"])
        assertEquals(LANDLORD_GAS_SAFETY_URL, content["landlordGasSafetyUrl"])
        assertEquals("forms.buttons.saveAndContinue", content["submitButtonText"])
    }

    private fun setupStepConfig(): ProvideGasCertLaterStepConfig {
        stepConfig.urlPath = ProvideGasCertLaterStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
