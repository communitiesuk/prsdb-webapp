package uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.exceptions.CyaDataHasChangedException
import uk.gov.communities.prsdb.webapp.journeys.shared.states.MissingComplianceState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ReactivationConfirmMissingComplianceFormModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class AbstractConfirmMissingComplianceStepConfigTests {
    @Mock
    lateinit var mockState: MissingComplianceState

    private val routeSegment = "confirm-missing-compliance"

    @Test
    fun `mode returns null when form model is not present`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(routeSegment)).thenReturn(null)

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertNull(result)
    }

    @Test
    fun `mode returns null when wantsToProceed is null`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(routeSegment)).thenReturn(mapOf("wantsToProceed" to null))

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertNull(result)
    }

    @Test
    fun `mode returns CONFIRMED when wantsToProceed is true`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(routeSegment)).thenReturn(mapOf("wantsToProceed" to "true"))

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertEquals(ConfirmMissingComplianceMode.CONFIRMED, result)
    }

    @Test
    fun `mode returns GO_BACK when wantsToProceed is false`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(routeSegment)).thenReturn(mapOf("wantsToProceed" to "false"))

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertEquals(ConfirmMissingComplianceMode.GO_BACK, result)
    }

    @Test
    fun `chooseTemplate returns correct template path`() {
        // Arrange
        val stepConfig = setupStepConfig()

        // Act
        val result = stepConfig.chooseTemplate(mockState)

        // Assert
        assertEquals("forms/confirmMissingCompliance", result)
    }

    @Test
    fun `getStepSpecificContent includes the missing certificate flags and journey specific keys`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.isGasCertMissing).thenReturn(true)
        whenever(mockState.isElectricalCertMissing).thenReturn(false)
        whenever(mockState.isEpcMissing).thenReturn(true)

        // Act
        val content = stepConfig.getStepSpecificContent(mockState)

        // Assert
        assertEquals(true, content["isGasMissing"])
        assertEquals(false, content["isElectricalMissing"])
        assertEquals(true, content["isEpcMissing"])
        assertEquals("test.areYouSure.heading", content["areYouSureHeading"])
        assertEquals("test.button", content["submitButtonText"])
    }

    @Test
    fun `afterStepIsReached throws when answers changed since confirmation`() {
        // Arrange
        val stepConfig = setupStepConfig()
        whenever(mockState.answersChangedSinceConfirmation).thenReturn(true)

        // Act & Assert
        assertThrows<CyaDataHasChangedException> { stepConfig.afterStepIsReached(mockState) }
    }

    private fun setupStepConfig(): AbstractConfirmMissingComplianceStepConfig<ReactivationConfirmMissingComplianceFormModel> {
        val stepConfig =
            object : AbstractConfirmMissingComplianceStepConfig<ReactivationConfirmMissingComplianceFormModel>() {
                override val formModelClass = ReactivationConfirmMissingComplianceFormModel::class
                override val areYouSureHeadingKey = "test.areYouSure.heading"
                override val submitButtonKey = "test.button"
            }
        stepConfig.urlPath = routeSegment
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
