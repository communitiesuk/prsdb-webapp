package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

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
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckElectricalCertUploadsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ElectricalCertExpiryDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertStep

@ExtendWith(MockitoExtension::class)
class ElectricalSafetyRegistrationCyaSummaryRowsFactoryTests {
    @Mock
    lateinit var mockState: ElectricalSafetyState

    private val mockHasElectricalCertStep: HasElectricalCertStep = mock()
    private val mockElectricalCertExpiryDateStep: ElectricalCertExpiryDateStep = mock()
    private val mockCheckElectricalCertUploadsStep: CheckElectricalCertUploadsStep = mock()

    private fun setupCommonStateMocks() {
        whenever(mockState.hasElectricalCertStep).thenReturn(mockHasElectricalCertStep)
        whenever(mockHasElectricalCertStep.currentJourneyId).thenReturn("test-journey-id")
    }

    @Nested
    inner class CertUploaded {
        @Test
        fun `factory returns correct content for EIC with uploads`() {
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

            val factory = ElectricalSafetyRegistrationCyaSummaryRowsFactory(mockState)

            val rows = factory.createRows()
            assertEquals(3, rows.size)
            assertEquals("checkElectricalSafety.eicLabel", rows[0].fieldValue)
            assertEquals(expiryDate, rows[1].fieldValue)
            assertEquals(listOf("cert.pdf"), rows[2].fieldValue)

            assertNull(factory.getInsetTextKey())
        }

        @Test
        fun `factory returns correct content for EICR with uploads`() {
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

            val factory = ElectricalSafetyRegistrationCyaSummaryRowsFactory(mockState)

            val rows = factory.createRows()
            assertEquals(3, rows.size)
            assertEquals("checkElectricalSafety.eicrLabel", rows[0].fieldValue)

            assertNull(factory.getInsetTextKey())
        }

        @Test
        fun `factory sorts uploads by map key and returns file names`() {
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

            val factory = ElectricalSafetyRegistrationCyaSummaryRowsFactory(mockState)

            val rows = factory.createRows()
            assertEquals(listOf("first.pdf", "second.pdf", "third.pdf"), rows[2].fieldValue)
        }
    }

    @Nested
    inner class ProvideLater {
        @Test
        fun `factory returns correct content for provide this later when occupied`() {
            setupCommonStateMocks()
            whenever(mockHasElectricalCertStep.outcome).thenReturn(HasElectricalCertMode.PROVIDE_THIS_LATER)
            whenever(mockState.isOccupied).thenReturn(true)

            val factory = ElectricalSafetyRegistrationCyaSummaryRowsFactory(mockState)

            val rows = factory.createRows()
            assertEquals(1, rows.size)
            assertEquals("checkElectricalSafety.provideThisLater.occupied", rows[0].fieldValue)

            assertNull(factory.getInsetTextKey())
        }

        @Test
        fun `factory returns correct content for provide this later when unoccupied`() {
            setupCommonStateMocks()
            whenever(mockHasElectricalCertStep.outcome).thenReturn(HasElectricalCertMode.PROVIDE_THIS_LATER)
            whenever(mockState.isOccupied).thenReturn(false)

            val factory = ElectricalSafetyRegistrationCyaSummaryRowsFactory(mockState)

            val rows = factory.createRows()
            assertEquals(1, rows.size)
            assertEquals("checkElectricalSafety.provideThisLater.unoccupied", rows[0].fieldValue)

            assertNull(factory.getInsetTextKey())
        }
    }

    @Nested
    inner class NoCert {
        @Test
        fun `factory returns correct content for no cert when occupied`() {
            setupCommonStateMocks()
            whenever(mockHasElectricalCertStep.outcome).thenReturn(HasElectricalCertMode.NO_CERTIFICATE)
            whenever(mockState.isOccupied).thenReturn(true)

            val factory = ElectricalSafetyRegistrationCyaSummaryRowsFactory(mockState)

            val rows = factory.createRows()
            assertEquals(1, rows.size)
            assertEquals("checkElectricalSafety.noneLabel", rows[0].fieldValue)

            assertEquals("checkElectricalSafety.occupiedNoCertInsetText", factory.getInsetTextKey())
        }

        @Test
        fun `factory returns correct content for no cert when unoccupied`() {
            setupCommonStateMocks()
            whenever(mockHasElectricalCertStep.outcome).thenReturn(HasElectricalCertMode.NO_CERTIFICATE)
            whenever(mockState.isOccupied).thenReturn(false)

            val factory = ElectricalSafetyRegistrationCyaSummaryRowsFactory(mockState)

            val rows = factory.createRows()
            assertEquals(1, rows.size)
            assertEquals("checkElectricalSafety.provideThisLater.unoccupied", rows[0].fieldValue)

            assertNull(factory.getInsetTextKey())
        }
    }

    @Nested
    inner class CertExpired {
        @Test
        fun `factory returns correct content for expired cert when occupied`() {
            setupCommonStateMocks()
            whenever(mockHasElectricalCertStep.outcome).thenReturn(HasElectricalCertMode.HAS_EIC)
            whenever(mockState.getElectricalCertificateIsOutdated()).thenReturn(true)
            whenever(mockState.isOccupied).thenReturn(true)

            val factory = ElectricalSafetyRegistrationCyaSummaryRowsFactory(mockState)

            val rows = factory.createRows()
            assertEquals(1, rows.size)
            assertEquals("checkElectricalSafety.noneLabel", rows[0].fieldValue)

            assertEquals("checkElectricalSafety.occupiedNoCertInsetText", factory.getInsetTextKey())
        }

        @Test
        fun `factory returns correct content for expired cert when unoccupied`() {
            setupCommonStateMocks()
            whenever(mockHasElectricalCertStep.outcome).thenReturn(HasElectricalCertMode.HAS_EICR)
            whenever(mockState.getElectricalCertificateIsOutdated()).thenReturn(true)
            whenever(mockState.isOccupied).thenReturn(false)

            val factory = ElectricalSafetyRegistrationCyaSummaryRowsFactory(mockState)

            val rows = factory.createRows()
            assertEquals(1, rows.size)
            assertEquals("checkElectricalSafety.provideThisLater.unoccupied", rows[0].fieldValue)

            assertNull(factory.getInsetTextKey())
        }
    }
}
