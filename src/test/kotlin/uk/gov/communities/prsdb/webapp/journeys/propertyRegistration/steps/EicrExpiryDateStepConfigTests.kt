package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyDetailState
import uk.gov.communities.prsdb.webapp.journeys.shared.ElectricalCertExpiryStatus
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class EicrExpiryDateStepConfigTests {
    @Mock
    lateinit var mockState: ElectricalSafetyDetailState

    @Test
    fun `getStepSpecificContent returns EICR heading`() {
        val stepConfig = setupStepConfig()

        val content = stepConfig.getStepSpecificContent(mockState)

        assertEquals(
            "propertyCompliance.electricalSafetyTask.electricalCertExpiryDate.eicr.fieldSetHeading",
            content["fieldSetHeading"],
        )
    }

    @Test
    fun `mode returns null when getEicrIsOutdated returns null`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.getEicrIsOutdated()).thenReturn(null)

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertNull(result)
    }

    @Test
    fun `mode returns ELECTRICAL_SAFETY_CERTIFICATE_OUTDATED when getEicrIsOutdated returns true`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.getEicrIsOutdated()).thenReturn(true)

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertEquals(ElectricalCertExpiryStatus.ELECTRICAL_SAFETY_CERTIFICATE_OUTDATED, result)
    }

    @Test
    fun `mode returns ELECTRICAL_SAFETY_CERTIFICATE_IN_DATE when getEicrIsOutdated returns false`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.getEicrIsOutdated()).thenReturn(false)

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertEquals(ElectricalCertExpiryStatus.ELECTRICAL_SAFETY_CERTIFICATE_IN_DATE, result)
    }

    private fun setupStepConfig(): EicrExpiryDateStepConfig {
        val stepConfig = EicrExpiryDateStepConfig()
        stepConfig.urlPath = EicrExpiryDateStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
