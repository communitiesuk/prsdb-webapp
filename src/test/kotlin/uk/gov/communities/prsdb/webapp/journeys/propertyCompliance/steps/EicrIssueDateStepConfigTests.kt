package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.EicrState
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class EicrIssueDateStepConfigTests {
    @Mock
    lateinit var mockEicrState: EicrState

    val routeSegment = EicrIssueDateStep.ROUTE_SEGMENT

    @Test
    fun `mode returns null when getEicrCertificateIsOutdated returns null`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockEicrState.getEicrCertificateIsOutdated()).thenReturn(null)

        // Act
        val result = stepConfig.mode(mockEicrState)

        // Assert
        assertNull(result)
    }

    @Test
    fun `mode returns EICR_CERTIFICATE_OUTDATED when getEicrCertificateIsOutdated returns true`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockEicrState.getEicrCertificateIsOutdated()).thenReturn(true)

        // Act
        val result = stepConfig.mode(mockEicrState)

        // Assert
        assertEquals(EicrIssueDateMode.EICR_CERTIFICATE_OUTDATED, result)
    }

    @Test
    fun `mode returns EICR_CERTIFICATE_IN_DATE when getEicrCertificateIsOutdated returns false`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockEicrState.getEicrCertificateIsOutdated()).thenReturn(false)

        // Act
        val result = stepConfig.mode(mockEicrState)

        // Assert
        assertEquals(EicrIssueDateMode.EICR_CERTIFICATE_IN_DATE, result)
    }

    private fun setupStepConfig(): EicrIssueDateStepConfig {
        val stepConfig = EicrIssueDateStepConfig()
        stepConfig.routeSegment = routeSegment
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
