package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyState
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class ElectricalCertExpiryDateStepConfigTests {
    @Mock
    lateinit var mockState: ElectricalSafetyState

    @Test
    fun `mode returns null when getElectricalCertificateIsOutdated returns null`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.getElectricalCertificateIsOutdated()).thenReturn(null)

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertNull(result)
    }

    @Test
    fun `mode returns ELECTRICAL_SAFETY_CERTIFICATE_OUTDATED when getElectricalCertificateIsOutdated returns true`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.getElectricalCertificateIsOutdated()).thenReturn(true)

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertEquals(ElectricalCertExpiryDateMode.ELECTRICAL_SAFETY_CERTIFICATE_OUTDATED, result)
    }

    @Test
    fun `mode returns ELECTRICAL_SAFETY_CERTIFICATE_IN_DATE when getElectricalCertificateIsOutdated returns false`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.getElectricalCertificateIsOutdated()).thenReturn(false)

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertEquals(ElectricalCertExpiryDateMode.ELECTRICAL_SAFETY_CERTIFICATE_IN_DATE, result)
    }

    private fun setupStepConfig(): ElectricalCertExpiryDateStepConfig {
        val stepConfig = ElectricalCertExpiryDateStepConfig()
        stepConfig.routeSegment = ElectricalCertExpiryDateStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
