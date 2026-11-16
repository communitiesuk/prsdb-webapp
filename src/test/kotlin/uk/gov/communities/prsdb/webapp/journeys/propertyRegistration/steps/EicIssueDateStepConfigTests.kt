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
class EicIssueDateStepConfigTests {
    @Mock
    lateinit var mockState: ElectricalSafetyDetailState

    @Test
    fun `getStepSpecificContent returns EIC issue date heading and save and continue button`() {
        val stepConfig = setupStepConfig()

        val content = stepConfig.getStepSpecificContent(mockState)

        assertEquals("propertyCompliance.electricalSafetyTask.eicIssueDate.fieldSetHeading", content["fieldSetHeading"])
        assertEquals("forms.buttons.saveAndContinue", content["submitButtonText"])
    }

    @Test
    fun `chooseTemplate returns the date form template`() {
        val stepConfig = setupStepConfig()

        assertEquals("forms/dateForm", stepConfig.chooseTemplate(mockState))
    }

    @Test
    fun `mode returns null when getEicIsOutdated returns null`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getEicIsOutdated()).thenReturn(null)

        assertNull(stepConfig.mode(mockState))
    }

    @Test
    fun `mode returns ELECTRICAL_SAFETY_CERTIFICATE_OUTDATED when getEicIsOutdated returns true`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getEicIsOutdated()).thenReturn(true)

        assertEquals(ElectricalCertExpiryStatus.ELECTRICAL_SAFETY_CERTIFICATE_OUTDATED, stepConfig.mode(mockState))
    }

    @Test
    fun `mode returns ELECTRICAL_SAFETY_CERTIFICATE_IN_DATE when getEicIsOutdated returns false`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getEicIsOutdated()).thenReturn(false)

        assertEquals(ElectricalCertExpiryStatus.ELECTRICAL_SAFETY_CERTIFICATE_IN_DATE, stepConfig.mode(mockState))
    }

    private fun setupStepConfig(): EicIssueDateStepConfig {
        val stepConfig = EicIssueDateStepConfig()
        stepConfig.urlPath = EicIssueDateStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
