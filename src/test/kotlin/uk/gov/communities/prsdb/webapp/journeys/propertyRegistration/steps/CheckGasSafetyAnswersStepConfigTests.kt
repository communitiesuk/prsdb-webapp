package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.CertificateUpload
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class CheckGasSafetyAnswersStepConfigTests {
    @Mock
    lateinit var mockState: GasSafetyState

    private val mockHasGasSupplyStep: HasGasSupplyStep = mock()
    private val mockHasGasCertStep: HasGasCertStep = mock()
    private val mockGasCertIssueDateStep: GasCertIssueDateStep = mock()
    private val mockCheckGasCertUploadsStep: CheckGasCertUploadsStep = mock()

    private fun setupStepConfig(): CheckGasSafetyAnswersStepConfig {
        val stepConfig = CheckGasSafetyAnswersStepConfig()
        stepConfig.routeSegment = CheckGasSafetyAnswersStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }

    private fun setupCommonStateMocks() {
        whenever(mockState.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)
        whenever(mockState.hasGasCertStep).thenReturn(mockHasGasCertStep)
        whenever(mockHasGasSupplyStep.currentJourneyId).thenReturn("test-journey-id")
        whenever(mockHasGasCertStep.currentJourneyId).thenReturn("test-journey-id")
    }

    @Suppress("UNCHECKED_CAST")
    private fun getGasSupplyRows(content: Map<String, Any?>): List<SummaryListRowViewModel> =
        content["gasSupplyRows"] as List<SummaryListRowViewModel>

    @Suppress("UNCHECKED_CAST")
    private fun getCertRows(content: Map<String, Any?>): List<SummaryListRowViewModel> =
        content["certRows"] as List<SummaryListRowViewModel>

    @Test
    fun `chooseTemplate returns checkGasSafetyAnswersForm`() {
        val stepConfig = setupStepConfig()
        val result = stepConfig.chooseTemplate(mockState)
        assertEquals("forms/checkGasSafetyAnswersForm", result)
    }

    @Test
    fun `mode returns COMPLETE when form data exists`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(CheckGasSafetyAnswersStep.ROUTE_SEGMENT)).thenReturn(mapOf("key" to "value"))

        val result = stepConfig.mode(mockState)

        assertEquals(Complete.COMPLETE, result)
    }

    @Test
    fun `mode returns null when no form data exists`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(CheckGasSafetyAnswersStep.ROUTE_SEGMENT)).thenReturn(null)

        val result = stepConfig.mode(mockState)

        assertNull(result)
    }

    @Nested
    inner class NoGasSupply {
        @Test
        fun `getStepSpecificContent returns correct content when there is no gas supply`() {
            // Arrange
            val stepConfig = setupStepConfig()
            whenever(mockState.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)
            whenever(mockHasGasSupplyStep.currentJourneyId).thenReturn("test-journey-id")
            whenever(mockHasGasSupplyStep.outcome).thenReturn(YesOrNo.NO)

            // Act
            val content = stepConfig.getStepSpecificContent(mockState)

            // Assert
            val gasSupplyRows = getGasSupplyRows(content)
            assertEquals(1, gasSupplyRows.size)
            assertEquals(false, gasSupplyRows[0].fieldValue)

            val certRows = getCertRows(content)
            assertEquals(emptyList<SummaryListRowViewModel>(), certRows)

            assertEquals("checkGasSafety.noGasSupplyInsetText", content["insetTextKey"])
            assertEquals("forms.buttons.saveAndContinue", content["submitButtonText"])
        }
    }

    @Nested
    inner class UploadedCertificate {
        @Test
        fun `getStepSpecificContent returns correct content for valid cert with uploads`() {
            // Arrange
            val stepConfig = setupStepConfig()
            setupCommonStateMocks()
            whenever(mockHasGasSupplyStep.outcome).thenReturn(YesOrNo.YES)
            whenever(mockHasGasCertStep.outcome).thenReturn(HasGasCertMode.HAS_CERTIFICATE)
            whenever(mockState.getGasSafetyCertificateIsOutdated()).thenReturn(false)

            val issueDate = LocalDate(2024, 6, 15)
            whenever(mockState.getGasSafetyCertificateIssueDateIfReachable()).thenReturn(issueDate)

            whenever(mockState.gasCertIssueDateStep).thenReturn(mockGasCertIssueDateStep)
            whenever(mockState.checkGasCertUploadsStep).thenReturn(mockCheckGasCertUploadsStep)
            whenever(mockGasCertIssueDateStep.currentJourneyId).thenReturn("test-journey-id")
            whenever(mockCheckGasCertUploadsStep.currentJourneyId).thenReturn("test-journey-id")
            whenever(mockState.gasUploadMap).thenReturn(
                mapOf(1 to CertificateUpload(1L, "cert.pdf")),
            )

            // Act
            val content = stepConfig.getStepSpecificContent(mockState)

            // Assert
            val gasSupplyRows = getGasSupplyRows(content)
            assertEquals(1, gasSupplyRows.size)
            assertEquals(true, gasSupplyRows[0].fieldValue)

            val certRows = getCertRows(content)
            assertEquals(3, certRows.size)
            assertEquals(true, certRows[0].fieldValue)
            assertEquals(issueDate, certRows[1].fieldValue)
            assertEquals(listOf("cert.pdf"), certRows[2].fieldValue)

            assertNull(content["insetTextKey"])
            assertEquals("forms.buttons.saveAndContinue", content["submitButtonText"])
        }

        @Test
        fun `getStepSpecificContent sorts uploads by map key and returns file names`() {
            // Arrange
            val stepConfig = setupStepConfig()
            setupCommonStateMocks()
            whenever(mockHasGasSupplyStep.outcome).thenReturn(YesOrNo.YES)
            whenever(mockHasGasCertStep.outcome).thenReturn(HasGasCertMode.HAS_CERTIFICATE)
            whenever(mockState.getGasSafetyCertificateIsOutdated()).thenReturn(false)
            whenever(mockState.getGasSafetyCertificateIssueDateIfReachable()).thenReturn(LocalDate(2024, 6, 15))
            whenever(mockState.gasCertIssueDateStep).thenReturn(mockGasCertIssueDateStep)
            whenever(mockState.checkGasCertUploadsStep).thenReturn(mockCheckGasCertUploadsStep)
            whenever(mockGasCertIssueDateStep.currentJourneyId).thenReturn("test-journey-id")
            whenever(mockCheckGasCertUploadsStep.currentJourneyId).thenReturn("test-journey-id")
            whenever(mockState.gasUploadMap).thenReturn(
                mapOf(
                    3 to CertificateUpload(3L, "third.pdf"),
                    1 to CertificateUpload(1L, "first.pdf"),
                    2 to CertificateUpload(2L, "second.pdf"),
                ),
            )

            // Act
            val content = stepConfig.getStepSpecificContent(mockState)

            // Assert
            val certRows = getCertRows(content)
            assertEquals(listOf("first.pdf", "second.pdf", "third.pdf"), certRows[2].fieldValue)
        }
    }

    @Nested
    inner class ProvideLater {
        @Test
        fun `getStepSpecificContent returns correct content for provide this later when occupied`() {
            // Arrange
            val stepConfig = setupStepConfig()
            setupCommonStateMocks()
            whenever(mockHasGasSupplyStep.outcome).thenReturn(YesOrNo.YES)
            whenever(mockHasGasCertStep.outcome).thenReturn(HasGasCertMode.PROVIDE_THIS_LATER)
            whenever(mockState.isOccupied).thenReturn(true)

            // Act
            val content = stepConfig.getStepSpecificContent(mockState)

            // Assert
            val gasSupplyRows = getGasSupplyRows(content)
            assertEquals(2, gasSupplyRows.size)
            assertEquals(true, gasSupplyRows[0].fieldValue)
            assertEquals("checkGasSafety.provideThisLater.occupied", gasSupplyRows[1].fieldValue)

            val certRows = getCertRows(content)
            assertEquals(emptyList<SummaryListRowViewModel>(), certRows)

            assertNull(content["insetTextKey"])
        }

        @Test
        fun `getStepSpecificContent returns correct content for provide this later when unoccupied`() {
            // Arrange
            val stepConfig = setupStepConfig()
            setupCommonStateMocks()
            whenever(mockHasGasSupplyStep.outcome).thenReturn(YesOrNo.YES)
            whenever(mockHasGasCertStep.outcome).thenReturn(HasGasCertMode.PROVIDE_THIS_LATER)
            whenever(mockState.isOccupied).thenReturn(false)

            // Act
            val content = stepConfig.getStepSpecificContent(mockState)

            // Assert
            val gasSupplyRows = getGasSupplyRows(content)
            assertEquals(2, gasSupplyRows.size)
            assertEquals(true, gasSupplyRows[0].fieldValue)
            assertEquals("checkGasSafety.provideThisLater.unoccupied", gasSupplyRows[1].fieldValue)

            val certRows = getCertRows(content)
            assertEquals(emptyList<SummaryListRowViewModel>(), certRows)

            assertNull(content["insetTextKey"])
        }
    }

    @Nested
    inner class NoCert {
        @Test
        fun `getStepSpecificContent returns correct content for no cert when occupied`() {
            // Arrange
            val stepConfig = setupStepConfig()
            setupCommonStateMocks()
            whenever(mockHasGasSupplyStep.outcome).thenReturn(YesOrNo.YES)
            whenever(mockHasGasCertStep.outcome).thenReturn(HasGasCertMode.NO_CERTIFICATE)
            whenever(mockState.isOccupied).thenReturn(true)

            // Act
            val content = stepConfig.getStepSpecificContent(mockState)

            // Assert
            val gasSupplyRows = getGasSupplyRows(content)
            assertEquals(2, gasSupplyRows.size)
            assertEquals(true, gasSupplyRows[0].fieldValue)
            assertEquals(false, gasSupplyRows[1].fieldValue)

            val certRows = getCertRows(content)
            assertEquals(emptyList<SummaryListRowViewModel>(), certRows)

            assertEquals("checkGasSafety.occupiedNoCertInsetText", content["insetTextKey"])
        }

        @Test
        fun `getStepSpecificContent returns correct content for no cert when unoccupied`() {
            // Arrange
            val stepConfig = setupStepConfig()
            setupCommonStateMocks()
            whenever(mockHasGasSupplyStep.outcome).thenReturn(YesOrNo.YES)
            whenever(mockHasGasCertStep.outcome).thenReturn(HasGasCertMode.NO_CERTIFICATE)
            whenever(mockState.isOccupied).thenReturn(false)

            // Act
            val content = stepConfig.getStepSpecificContent(mockState)

            // Assert
            val gasSupplyRows = getGasSupplyRows(content)
            assertEquals(2, gasSupplyRows.size)
            assertEquals(true, gasSupplyRows[0].fieldValue)
            assertEquals("checkGasSafety.provideThisLater.unoccupied", gasSupplyRows[1].fieldValue)

            val certRows = getCertRows(content)
            assertEquals(emptyList<SummaryListRowViewModel>(), certRows)

            assertNull(content["insetTextKey"])
        }
    }

    @Nested
    inner class CertExpired {
        @Test
        fun `getStepSpecificContent returns correct content for expired cert when occupied`() {
            // Arrange
            val stepConfig = setupStepConfig()
            setupCommonStateMocks()
            whenever(mockHasGasSupplyStep.outcome).thenReturn(YesOrNo.YES)
            whenever(mockHasGasCertStep.outcome).thenReturn(HasGasCertMode.HAS_CERTIFICATE)
            whenever(mockState.getGasSafetyCertificateIsOutdated()).thenReturn(true)
            whenever(mockState.isOccupied).thenReturn(true)

            // Act
            val content = stepConfig.getStepSpecificContent(mockState)

            // Assert
            val gasSupplyRows = getGasSupplyRows(content)
            assertEquals(2, gasSupplyRows.size)
            assertEquals(true, gasSupplyRows[0].fieldValue)
            assertEquals(false, gasSupplyRows[1].fieldValue)

            val certRows = getCertRows(content)
            assertEquals(emptyList<SummaryListRowViewModel>(), certRows)

            assertEquals("checkGasSafety.occupiedNoCertInsetText", content["insetTextKey"])
        }

        @Test
        fun `getStepSpecificContent returns correct content for expired cert when unoccupied`() {
            // Arrange
            val stepConfig = setupStepConfig()
            setupCommonStateMocks()
            whenever(mockHasGasSupplyStep.outcome).thenReturn(YesOrNo.YES)
            whenever(mockHasGasCertStep.outcome).thenReturn(HasGasCertMode.HAS_CERTIFICATE)
            whenever(mockState.getGasSafetyCertificateIsOutdated()).thenReturn(true)
            whenever(mockState.isOccupied).thenReturn(false)

            // Act
            val content = stepConfig.getStepSpecificContent(mockState)

            // Assert
            val gasSupplyRows = getGasSupplyRows(content)
            assertEquals(2, gasSupplyRows.size)
            assertEquals(true, gasSupplyRows[0].fieldValue)
            assertEquals("checkGasSafety.provideThisLater.unoccupied", gasSupplyRows[1].fieldValue)

            val certRows = getCertRows(content)
            assertEquals(emptyList<SummaryListRowViewModel>(), certRows)

            assertNull(content["insetTextKey"])
        }
    }
}
