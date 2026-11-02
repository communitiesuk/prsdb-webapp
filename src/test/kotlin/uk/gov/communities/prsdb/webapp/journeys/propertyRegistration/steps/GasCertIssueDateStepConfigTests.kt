package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
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
class GasCertIssueDateStepConfigTests {
    @Mock
    lateinit var mockState: GasSafetyDetailState

    // TODO PDJB-1926: Remove the feature flag manager when GAS_CERTIFICATE_14_MONTH_EXPIRY is removed.
    @Mock
    lateinit var featureFlagManager: FeatureFlagManager

    @Test
    fun `chooseTemplate returns the gas certificate issue date form`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(featureFlagManager.checkFeature(GAS_CERTIFICATE_14_MONTH_EXPIRY)).thenReturn(true)

        // Act
        val result = stepConfig.chooseTemplate(mockState)

        // Assert
        assertEquals("forms/gasCertIssueDateForm", result)
    }

    // TODO PDJB-1926: Remove this template test when GAS_CERTIFICATE_14_MONTH_EXPIRY is removed.
    @Test
    fun `chooseTemplate returns the original date form without 14 month expiry support`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(featureFlagManager.checkFeature(GAS_CERTIFICATE_14_MONTH_EXPIRY)).thenReturn(false)

        // Act
        val result = stepConfig.chooseTemplate(mockState)

        // Assert
        assertEquals("forms/dateForm", result)
    }

    @Test
    fun `getStepSpecificContent returns the issue date heading and hint`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(featureFlagManager.checkFeature(GAS_CERTIFICATE_14_MONTH_EXPIRY)).thenReturn(true)

        // Act
        val result = stepConfig.getStepSpecificContent(mockState)

        // Assert
        assertEquals(
            "propertyCompliance.gasSafetyTask.gasSafetyCertIssueDate.fieldSetHeading",
            result["fieldSetHeading"],
        )
        assertEquals(
            "propertyCompliance.gasSafetyTask.gasSafetyCertIssueDate.fieldSetHint",
            result["fieldSetHint"],
        )
        assertEquals("forms.buttons.saveAndContinue", result["submitButtonText"])
    }

    // TODO PDJB-1926: Remove this content test when GAS_CERTIFICATE_14_MONTH_EXPIRY is removed.
    @Test
    fun `getStepSpecificContent returns the original issue date heading and hint without 14 month expiry support`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(featureFlagManager.checkFeature(GAS_CERTIFICATE_14_MONTH_EXPIRY)).thenReturn(false)

        // Act
        val result = stepConfig.getStepSpecificContent(mockState)

        // Assert
        assertEquals(
            "propertyCompliance.gasSafetyTask.gasSafetyCertIssueDate.beforePdjb1898.fieldSetHeading",
            result["fieldSetHeading"],
        )
        assertEquals(
            "propertyCompliance.gasSafetyTask.gasSafetyCertIssueDate.beforePdjb1898.fieldSetHint",
            result["fieldSetHint"],
        )
        assertEquals("forms.buttons.saveAndContinue", result["submitButtonText"])
    }

    @Test
    fun `mode returns null when getGasSafetyCertificateIsOutdated returns null`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.getGasSafetyCertificateIsOutdated()).thenReturn(null)

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertNull(result)
    }

    @Test
    fun `mode returns GAS_SAFETY_CERTIFICATE_OUTDATED when getGasSafetyCertificateIsOutdated returns true`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.getGasSafetyCertificateIsOutdated()).thenReturn(true)

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertEquals(GasCertIssueDateMode.GAS_SAFETY_CERTIFICATE_OUTDATED, result)
    }

    @Test
    fun `mode returns GAS_SAFETY_CERTIFICATE_IN_DATE when getGasSafetyCertificateIsOutdated returns false`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.getGasSafetyCertificateIsOutdated()).thenReturn(false)

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertEquals(GasCertIssueDateMode.GAS_SAFETY_CERTIFICATE_IN_DATE, result)
    }

    private fun setupStepConfig(): GasCertIssueDateStepConfig {
        val stepConfig = GasCertIssueDateStepConfig(featureFlagManager)
        stepConfig.urlPath = GasCertIssueDateStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
