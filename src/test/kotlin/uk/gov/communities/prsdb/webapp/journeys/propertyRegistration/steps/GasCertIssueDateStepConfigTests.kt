package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class GasCertIssueDateStepConfigTests {
    @Mock
    lateinit var mockState: GasSafetyState

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
        val stepConfig = GasCertIssueDateStepConfig()
        stepConfig.routeSegment = GasCertIssueDateStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
