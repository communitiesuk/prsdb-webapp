package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.GAS_CERTIFICATE_14_MONTH_EXPIRY
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyDetailState
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class GasCertExpiredStepConfigTests {
    @Mock
    lateinit var mockState: GasSafetyDetailState

    @Mock
    lateinit var mockFeatureFlagManager: FeatureFlagManager

    @Test
    fun `chooseTemplate returns occupied template when isOccupied is true and the 14 month expiry flag is enabled`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.isOccupied).thenReturn(true)
        whenever(mockFeatureFlagManager.checkFeature(GAS_CERTIFICATE_14_MONTH_EXPIRY)).thenReturn(true)

        // Act
        val result = stepConfig.chooseTemplate(mockState)

        // Assert
        assertEquals("forms/gasSafetyCertificateExpiredForOccupiedProperty", result)
    }

    @Test
    fun `chooseTemplate returns unoccupied template when isOccupied is false and the 14 month expiry flag is enabled`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.isOccupied).thenReturn(false)
        whenever(mockFeatureFlagManager.checkFeature(GAS_CERTIFICATE_14_MONTH_EXPIRY)).thenReturn(true)

        // Act
        val result = stepConfig.chooseTemplate(mockState)

        // Assert
        assertEquals("forms/gasSafetyCertificateExpiredForUnoccupiedProperty", result)
    }

    @Test
    fun `chooseTemplate returns Before14MonthExpiry occupied template when isOccupied is true and the 14 month expiry flag is disabled`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.isOccupied).thenReturn(true)
        whenever(mockFeatureFlagManager.checkFeature(GAS_CERTIFICATE_14_MONTH_EXPIRY)).thenReturn(false)

        // Act
        val result = stepConfig.chooseTemplate(mockState)

        // Assert
        assertEquals("forms/gasSafetyCertificateExpiredForOccupiedPropertyBefore14MonthExpiry", result)
    }

    @Test
    fun `chooseTemplate returns Before14MonthExpiry unoccupied template when isOccupied is false and the 14 month expiry flag is disabled`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.isOccupied).thenReturn(false)
        whenever(mockFeatureFlagManager.checkFeature(GAS_CERTIFICATE_14_MONTH_EXPIRY)).thenReturn(false)

        // Act
        val result = stepConfig.chooseTemplate(mockState)

        // Assert
        assertEquals("forms/gasSafetyCertificateExpiredForUnoccupiedPropertyBefore14MonthExpiry", result)
    }

    private fun setupStepConfig(): GasCertExpiredStepConfig {
        val stepConfig = GasCertExpiredStepConfig(mockFeatureFlagManager)
        stepConfig.urlPath = GasCertExpiredStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
