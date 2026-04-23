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
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.CertificateUpload
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckGasCertUploadsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.GasCertIssueDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasCertMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasSupplyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

@ExtendWith(MockitoExtension::class)
class GasSafetyRegistrationCyaSummaryRowsFactoryTests {
    @Mock
    lateinit var mockState: GasSafetyState

    private val mockHasGasSupplyStep: HasGasSupplyStep = mock()
    private val mockHasGasCertStep: HasGasCertStep = mock()
    private val mockGasCertIssueDateStep: GasCertIssueDateStep = mock()
    private val mockCheckGasCertUploadsStep: CheckGasCertUploadsStep = mock()

    private fun setupCommonStateMocks() {
        whenever(mockState.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)
        whenever(mockState.hasGasCertStep).thenReturn(mockHasGasCertStep)
        whenever(mockHasGasSupplyStep.currentJourneyId).thenReturn("test-journey-id")
        whenever(mockHasGasCertStep.currentJourneyId).thenReturn("test-journey-id")
    }

    @Nested
    inner class NoGasSupply {
        @Test
        fun `createGasSupplyRows returns single row with false when no gas supply`() {
            whenever(mockState.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)
            whenever(mockHasGasSupplyStep.currentJourneyId).thenReturn("test-journey-id")
            whenever(mockHasGasSupplyStep.outcome).thenReturn(YesOrNo.NO)

            val factory = GasSafetyRegistrationCyaSummaryRowsFactory(mockState)

            val gasSupplyRows = factory.createGasSupplyRows()
            assertEquals(1, gasSupplyRows.size)
            assertEquals(false, gasSupplyRows[0].fieldValue)

            val certRows = factory.createCertRows()
            assertEquals(emptyList<SummaryListRowViewModel>(), certRows)

            assertEquals("checkGasSafety.noGasSupplyInsetText", factory.getInsetTextKey())
        }
    }

    @Nested
    inner class UploadedCertificate {
        @Test
        fun `factory returns correct content for valid cert with uploads`() {
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

            val factory = GasSafetyRegistrationCyaSummaryRowsFactory(mockState)

            val gasSupplyRows = factory.createGasSupplyRows()
            assertEquals(1, gasSupplyRows.size)
            assertEquals(true, gasSupplyRows[0].fieldValue)

            val certRows = factory.createCertRows()
            assertEquals(3, certRows.size)
            assertEquals(true, certRows[0].fieldValue)
            assertEquals(issueDate, certRows[1].fieldValue)
            assertEquals(listOf("cert.pdf"), certRows[2].fieldValue)

            assertNull(factory.getInsetTextKey())
        }

        @Test
        fun `factory sorts uploads by map key and returns file names`() {
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

            val factory = GasSafetyRegistrationCyaSummaryRowsFactory(mockState)

            val certRows = factory.createCertRows()
            assertEquals(listOf("first.pdf", "second.pdf", "third.pdf"), certRows[2].fieldValue)
        }
    }

    @Nested
    inner class ProvideLater {
        @Test
        fun `factory returns correct content for provide this later when occupied`() {
            setupCommonStateMocks()
            whenever(mockHasGasSupplyStep.outcome).thenReturn(YesOrNo.YES)
            whenever(mockHasGasCertStep.outcome).thenReturn(HasGasCertMode.PROVIDE_THIS_LATER)
            whenever(mockState.isOccupied).thenReturn(true)

            val factory = GasSafetyRegistrationCyaSummaryRowsFactory(mockState)

            val gasSupplyRows = factory.createGasSupplyRows()
            assertEquals(2, gasSupplyRows.size)
            assertEquals(true, gasSupplyRows[0].fieldValue)
            assertEquals("checkGasSafety.provideThisLater.occupied", gasSupplyRows[1].fieldValue)

            val certRows = factory.createCertRows()
            assertEquals(emptyList<SummaryListRowViewModel>(), certRows)

            assertNull(factory.getInsetTextKey())
        }

        @Test
        fun `factory returns correct content for provide this later when unoccupied`() {
            setupCommonStateMocks()
            whenever(mockHasGasSupplyStep.outcome).thenReturn(YesOrNo.YES)
            whenever(mockHasGasCertStep.outcome).thenReturn(HasGasCertMode.PROVIDE_THIS_LATER)
            whenever(mockState.isOccupied).thenReturn(false)

            val factory = GasSafetyRegistrationCyaSummaryRowsFactory(mockState)

            val gasSupplyRows = factory.createGasSupplyRows()
            assertEquals(2, gasSupplyRows.size)
            assertEquals(true, gasSupplyRows[0].fieldValue)
            assertEquals("checkGasSafety.provideThisLater.unoccupied", gasSupplyRows[1].fieldValue)

            val certRows = factory.createCertRows()
            assertEquals(emptyList<SummaryListRowViewModel>(), certRows)

            assertNull(factory.getInsetTextKey())
        }
    }

    @Nested
    inner class NoCert {
        @Test
        fun `factory returns correct content for no cert when occupied`() {
            setupCommonStateMocks()
            whenever(mockHasGasSupplyStep.outcome).thenReturn(YesOrNo.YES)
            whenever(mockHasGasCertStep.outcome).thenReturn(HasGasCertMode.NO_CERTIFICATE)
            whenever(mockState.isOccupied).thenReturn(true)

            val factory = GasSafetyRegistrationCyaSummaryRowsFactory(mockState)

            val gasSupplyRows = factory.createGasSupplyRows()
            assertEquals(2, gasSupplyRows.size)
            assertEquals(true, gasSupplyRows[0].fieldValue)
            assertEquals(false, gasSupplyRows[1].fieldValue)

            val certRows = factory.createCertRows()
            assertEquals(emptyList<SummaryListRowViewModel>(), certRows)

            assertEquals("checkGasSafety.occupiedNoCertInsetText", factory.getInsetTextKey())
        }

        @Test
        fun `factory returns correct content for no cert when unoccupied`() {
            setupCommonStateMocks()
            whenever(mockHasGasSupplyStep.outcome).thenReturn(YesOrNo.YES)
            whenever(mockHasGasCertStep.outcome).thenReturn(HasGasCertMode.NO_CERTIFICATE)
            whenever(mockState.isOccupied).thenReturn(false)

            val factory = GasSafetyRegistrationCyaSummaryRowsFactory(mockState)

            val gasSupplyRows = factory.createGasSupplyRows()
            assertEquals(2, gasSupplyRows.size)
            assertEquals(true, gasSupplyRows[0].fieldValue)
            assertEquals("checkGasSafety.provideThisLater.unoccupied", gasSupplyRows[1].fieldValue)

            val certRows = factory.createCertRows()
            assertEquals(emptyList<SummaryListRowViewModel>(), certRows)

            assertNull(factory.getInsetTextKey())
        }
    }

    @Nested
    inner class CertExpired {
        @Test
        fun `factory returns correct content for expired cert when occupied`() {
            setupCommonStateMocks()
            whenever(mockHasGasSupplyStep.outcome).thenReturn(YesOrNo.YES)
            whenever(mockHasGasCertStep.outcome).thenReturn(HasGasCertMode.HAS_CERTIFICATE)
            whenever(mockState.getGasSafetyCertificateIsOutdated()).thenReturn(true)
            whenever(mockState.isOccupied).thenReturn(true)

            val factory = GasSafetyRegistrationCyaSummaryRowsFactory(mockState)

            val gasSupplyRows = factory.createGasSupplyRows()
            assertEquals(2, gasSupplyRows.size)
            assertEquals(true, gasSupplyRows[0].fieldValue)
            assertEquals(false, gasSupplyRows[1].fieldValue)

            val certRows = factory.createCertRows()
            assertEquals(emptyList<SummaryListRowViewModel>(), certRows)

            assertEquals("checkGasSafety.occupiedNoCertInsetText", factory.getInsetTextKey())
        }

        @Test
        fun `factory returns correct content for expired cert when unoccupied`() {
            setupCommonStateMocks()
            whenever(mockHasGasSupplyStep.outcome).thenReturn(YesOrNo.YES)
            whenever(mockHasGasCertStep.outcome).thenReturn(HasGasCertMode.HAS_CERTIFICATE)
            whenever(mockState.getGasSafetyCertificateIsOutdated()).thenReturn(true)
            whenever(mockState.isOccupied).thenReturn(false)

            val factory = GasSafetyRegistrationCyaSummaryRowsFactory(mockState)

            val gasSupplyRows = factory.createGasSupplyRows()
            assertEquals(2, gasSupplyRows.size)
            assertEquals(true, gasSupplyRows[0].fieldValue)
            assertEquals("checkGasSafety.provideThisLater.unoccupied", gasSupplyRows[1].fieldValue)

            val certRows = factory.createCertRows()
            assertEquals(emptyList<SummaryListRowViewModel>(), certRows)

            assertNull(factory.getInsetTextKey())
        }
    }
}
