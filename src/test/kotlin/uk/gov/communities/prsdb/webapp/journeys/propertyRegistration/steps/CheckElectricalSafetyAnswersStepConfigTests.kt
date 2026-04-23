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
import uk.gov.communities.prsdb.webapp.constants.enums.HasElectricalSafetyCertificate
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.CertificateUpload
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class CheckElectricalSafetyAnswersStepConfigTests {
    @Mock
    lateinit var mockState: ElectricalSafetyState

    private val mockHasElectricalCertStep: HasElectricalCertStep = mock()
    private val mockElectricalCertExpiryDateStep: ElectricalCertExpiryDateStep = mock()
    private val mockCheckElectricalCertUploadsStep: CheckElectricalCertUploadsStep = mock()

    private fun setupStepConfig(): CheckElectricalSafetyAnswersStepConfig {
        val stepConfig = CheckElectricalSafetyAnswersStepConfig()
        stepConfig.routeSegment = CheckElectricalSafetyAnswersStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }

    private fun setupCommonStateMocks() {
        whenever(mockState.hasElectricalCertStep).thenReturn(mockHasElectricalCertStep)
        whenever(mockHasElectricalCertStep.currentJourneyId).thenReturn("test-journey-id")
    }

    @Suppress("UNCHECKED_CAST")
    private fun getRows(content: Map<String, Any?>) = content["rows"] as List<SummaryListRowViewModel>

    @Test
    fun `chooseTemplate returns checkElectricalSafetyAnswersForm`() {
        val stepConfig = setupStepConfig()
        val result = stepConfig.chooseTemplate(mockState)
        assertEquals("forms/checkElectricalSafetyAnswersForm", result)
    }

    @Test
    fun `mode returns COMPLETE when form data exists`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(CheckElectricalSafetyAnswersStep.ROUTE_SEGMENT)).thenReturn(mapOf("key" to "value"))

        val result = stepConfig.mode(mockState)

        assertEquals(Complete.COMPLETE, result)
    }

    @Test
    fun `mode returns null when no form data exists`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.getStepData(CheckElectricalSafetyAnswersStep.ROUTE_SEGMENT)).thenReturn(null)

        val result = stepConfig.mode(mockState)

        assertNull(result)
    }

    @Nested
    inner class CertUploaded {
        @Test
        fun `getStepSpecificContent returns correct content for EIC with uploads`() {
            // Arrange
            val stepConfig = setupStepConfig()
            setupCommonStateMocks()
            whenever(mockHasElectricalCertStep.outcome).thenReturn(HasElectricalCertMode.HAS_EIC)
            whenever(mockState.getElectricalCertificateIsOutdated()).thenReturn(false)
            whenever(mockState.getElectricalCertificateType()).thenReturn(HasElectricalSafetyCertificate.HAS_EIC)

            val expiryDate = LocalDate(2026, 6, 15)
            whenever(mockState.getElectricalCertificateExpiryDateIfReachable()).thenReturn(expiryDate)

            whenever(mockState.electricalCertExpiryDateStep).thenReturn(mockElectricalCertExpiryDateStep)
            whenever(mockState.checkElectricalCertUploadsStep).thenReturn(mockCheckElectricalCertUploadsStep)
            whenever(mockElectricalCertExpiryDateStep.currentJourneyId).thenReturn("test-journey-id")
            whenever(mockCheckElectricalCertUploadsStep.currentJourneyId).thenReturn("test-journey-id")
            whenever(mockState.electricalUploadMap).thenReturn(
                mapOf(1 to CertificateUpload(1L, "cert.pdf")),
            )

            // Act
            val content = stepConfig.getStepSpecificContent(mockState)

            // Assert
            val rows = getRows(content)
            assertEquals(3, rows.size)
            assertEquals("checkElectricalSafety.eicLabel", rows[0].fieldValue)
            assertEquals(expiryDate, rows[1].fieldValue)
            assertEquals(listOf("cert.pdf"), rows[2].fieldValue)

            assertNull(content["insetTextKey"])
            assertEquals("forms.buttons.saveAndContinue", content["submitButtonText"])
        }

        @Test
        fun `getStepSpecificContent returns correct content for EICR with uploads`() {
            // Arrange
            val stepConfig = setupStepConfig()
            setupCommonStateMocks()
            whenever(mockHasElectricalCertStep.outcome).thenReturn(HasElectricalCertMode.HAS_EICR)
            whenever(mockState.getElectricalCertificateIsOutdated()).thenReturn(false)
            whenever(mockState.getElectricalCertificateType()).thenReturn(HasElectricalSafetyCertificate.HAS_EICR)

            val expiryDate = LocalDate(2026, 6, 15)
            whenever(mockState.getElectricalCertificateExpiryDateIfReachable()).thenReturn(expiryDate)

            whenever(mockState.electricalCertExpiryDateStep).thenReturn(mockElectricalCertExpiryDateStep)
            whenever(mockState.checkElectricalCertUploadsStep).thenReturn(mockCheckElectricalCertUploadsStep)
            whenever(mockElectricalCertExpiryDateStep.currentJourneyId).thenReturn("test-journey-id")
            whenever(mockCheckElectricalCertUploadsStep.currentJourneyId).thenReturn("test-journey-id")
            whenever(mockState.electricalUploadMap).thenReturn(
                mapOf(1 to CertificateUpload(1L, "cert.pdf")),
            )

            // Act
            val content = stepConfig.getStepSpecificContent(mockState)

            // Assert
            val rows = getRows(content)
            assertEquals(3, rows.size)
            assertEquals("checkElectricalSafety.eicrLabel", rows[0].fieldValue)

            assertNull(content["insetTextKey"])
        }

        @Test
        fun `getStepSpecificContent sorts uploads by map key and returns file names`() {
            // Arrange
            val stepConfig = setupStepConfig()
            setupCommonStateMocks()
            whenever(mockHasElectricalCertStep.outcome).thenReturn(HasElectricalCertMode.HAS_EIC)
            whenever(mockState.getElectricalCertificateIsOutdated()).thenReturn(false)
            whenever(mockState.getElectricalCertificateType()).thenReturn(HasElectricalSafetyCertificate.HAS_EIC)
            whenever(mockState.getElectricalCertificateExpiryDateIfReachable()).thenReturn(LocalDate(2026, 6, 15))

            whenever(mockState.electricalCertExpiryDateStep).thenReturn(mockElectricalCertExpiryDateStep)
            whenever(mockState.checkElectricalCertUploadsStep).thenReturn(mockCheckElectricalCertUploadsStep)
            whenever(mockElectricalCertExpiryDateStep.currentJourneyId).thenReturn("test-journey-id")
            whenever(mockCheckElectricalCertUploadsStep.currentJourneyId).thenReturn("test-journey-id")
            whenever(mockState.electricalUploadMap).thenReturn(
                mapOf(
                    3 to CertificateUpload(3L, "third.pdf"),
                    1 to CertificateUpload(1L, "first.pdf"),
                    2 to CertificateUpload(2L, "second.pdf"),
                ),
            )

            // Act
            val content = stepConfig.getStepSpecificContent(mockState)

            // Assert
            val rows = getRows(content)
            assertEquals(listOf("first.pdf", "second.pdf", "third.pdf"), rows[2].fieldValue)
        }
    }

    @Nested
    inner class ProvideLater {
        @Test
        fun `getStepSpecificContent returns correct content for provide this later when occupied`() {
            // Arrange
            val stepConfig = setupStepConfig()
            setupCommonStateMocks()
            whenever(mockHasElectricalCertStep.outcome).thenReturn(HasElectricalCertMode.PROVIDE_THIS_LATER)
            whenever(mockState.isOccupied).thenReturn(true)

            // Act
            val content = stepConfig.getStepSpecificContent(mockState)

            // Assert
            val rows = getRows(content)
            assertEquals(1, rows.size)
            assertEquals("checkElectricalSafety.provideThisLater.occupied", rows[0].fieldValue)

            assertNull(content["insetTextKey"])
            assertEquals("forms.buttons.saveAndContinue", content["submitButtonText"])
        }

        @Test
        fun `getStepSpecificContent returns correct content for provide this later when unoccupied`() {
            // Arrange
            val stepConfig = setupStepConfig()
            setupCommonStateMocks()
            whenever(mockHasElectricalCertStep.outcome).thenReturn(HasElectricalCertMode.PROVIDE_THIS_LATER)
            whenever(mockState.isOccupied).thenReturn(false)

            // Act
            val content = stepConfig.getStepSpecificContent(mockState)

            // Assert
            val rows = getRows(content)
            assertEquals(1, rows.size)
            assertEquals("checkElectricalSafety.provideThisLater.unoccupied", rows[0].fieldValue)

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
            whenever(mockHasElectricalCertStep.outcome).thenReturn(HasElectricalCertMode.NO_CERTIFICATE)
            whenever(mockState.isOccupied).thenReturn(true)

            // Act
            val content = stepConfig.getStepSpecificContent(mockState)

            // Assert
            val rows = getRows(content)
            assertEquals(1, rows.size)
            assertEquals("checkElectricalSafety.noneLabel", rows[0].fieldValue)

            assertEquals("checkElectricalSafety.occupiedNoCertInsetText", content["insetTextKey"])
        }

        @Test
        fun `getStepSpecificContent returns correct content for no cert when unoccupied`() {
            // Arrange
            val stepConfig = setupStepConfig()
            setupCommonStateMocks()
            whenever(mockHasElectricalCertStep.outcome).thenReturn(HasElectricalCertMode.NO_CERTIFICATE)
            whenever(mockState.isOccupied).thenReturn(false)

            // Act
            val content = stepConfig.getStepSpecificContent(mockState)

            // Assert
            val rows = getRows(content)
            assertEquals(1, rows.size)
            assertEquals("checkElectricalSafety.provideThisLater.unoccupied", rows[0].fieldValue)

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
            whenever(mockHasElectricalCertStep.outcome).thenReturn(HasElectricalCertMode.HAS_EIC)
            whenever(mockState.getElectricalCertificateIsOutdated()).thenReturn(true)
            whenever(mockState.isOccupied).thenReturn(true)

            // Act
            val content = stepConfig.getStepSpecificContent(mockState)

            // Assert
            val rows = getRows(content)
            assertEquals(1, rows.size)
            assertEquals("checkElectricalSafety.noneLabel", rows[0].fieldValue)

            assertEquals("checkElectricalSafety.occupiedNoCertInsetText", content["insetTextKey"])
        }

        @Test
        fun `getStepSpecificContent returns correct content for expired cert when unoccupied`() {
            // Arrange
            val stepConfig = setupStepConfig()
            setupCommonStateMocks()
            whenever(mockHasElectricalCertStep.outcome).thenReturn(HasElectricalCertMode.HAS_EICR)
            whenever(mockState.getElectricalCertificateIsOutdated()).thenReturn(true)
            whenever(mockState.isOccupied).thenReturn(false)

            // Act
            val content = stepConfig.getStepSpecificContent(mockState)

            // Assert
            val rows = getRows(content)
            assertEquals(1, rows.size)
            assertEquals("checkElectricalSafety.provideThisLater.unoccupied", rows[0].fieldValue)

            assertNull(content["insetTextKey"])
        }
    }
}
