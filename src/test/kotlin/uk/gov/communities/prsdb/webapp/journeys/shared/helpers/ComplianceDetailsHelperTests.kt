package uk.gov.communities.prsdb.webapp.journeys.shared.helpers

import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.CertificateUpload
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.ElectricalSafetyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckGasCertUploadsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.GasCertIssueDateStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasCertMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasSupplyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider

@ExtendWith(MockitoExtension::class)
class ComplianceDetailsHelperTests {
    @Mock
    lateinit var mockEpcCertificateUrlProvider: EpcCertificateUrlProvider

    private val helper by lazy { ComplianceDetailsHelper(mockEpcCertificateUrlProvider) }

    @Nested
    inner class GetGasSafetyCyaContent {
        @Mock
        lateinit var mockState: GasSafetyState

        private val mockHasGasSupplyStep: HasGasSupplyStep = mock()
        private val mockHasGasCertStep: HasGasCertStep = mock()

        @Test
        fun `no gas supply returns gasSupplyRows with 1 row, empty certRows, and noGasSupply inset text key`() {
            whenever(mockState.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)
            whenever(mockHasGasSupplyStep.currentJourneyId).thenReturn("test-journey-id")
            whenever(mockHasGasSupplyStep.outcome).thenReturn(YesOrNo.NO)

            val content = helper.getGasSafetyCyaContent(mockState)

            @Suppress("UNCHECKED_CAST")
            val gasSupplyRows = content["gasSupplyRows"] as List<SummaryListRowViewModel>

            @Suppress("UNCHECKED_CAST")
            val gasCertRows = content["gasCertRows"] as List<SummaryListRowViewModel>
            val insetTextKey = content["gasInsetTextKey"]

            assertEquals(1, gasSupplyRows.size)
            assertEquals(0, gasCertRows.size)
            assertEquals("checkGasSafety.noGasSupplyInsetText", insetTextKey)
        }

        @Test
        fun `uploaded certificate returns gasSupplyRows with 1 row, certRows with 3 rows, and null inset text key`() {
            val mockGasCertIssueDateStep: GasCertIssueDateStep = mock()
            val mockCheckGasCertUploadsStep: CheckGasCertUploadsStep = mock()

            whenever(mockState.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)
            whenever(mockState.hasGasCertStep).thenReturn(mockHasGasCertStep)
            whenever(mockHasGasSupplyStep.currentJourneyId).thenReturn("test-journey-id")
            whenever(mockHasGasCertStep.currentJourneyId).thenReturn("test-journey-id")
            whenever(mockHasGasSupplyStep.outcome).thenReturn(YesOrNo.YES)
            whenever(mockHasGasCertStep.outcome).thenReturn(HasGasCertMode.HAS_CERTIFICATE)
            whenever(mockState.getGasSafetyCertificateIsOutdated()).thenReturn(false)
            whenever(mockState.getGasSafetyCertificateIssueDateIfReachable()).thenReturn(LocalDate(2024, 1, 15))
            whenever(mockState.gasCertIssueDateStep).thenReturn(mockGasCertIssueDateStep)
            whenever(mockState.checkGasCertUploadsStep).thenReturn(mockCheckGasCertUploadsStep)
            whenever(mockGasCertIssueDateStep.currentJourneyId).thenReturn("test-journey-id")
            whenever(mockCheckGasCertUploadsStep.currentJourneyId).thenReturn("test-journey-id")
            whenever(mockState.gasUploadMap).thenReturn(mapOf(1 to CertificateUpload(1L, "cert.pdf")))

            val content = helper.getGasSafetyCyaContent(mockState)

            @Suppress("UNCHECKED_CAST")
            val gasSupplyRows = content["gasSupplyRows"] as List<SummaryListRowViewModel>

            @Suppress("UNCHECKED_CAST")
            val gasCertRows = content["gasCertRows"] as List<SummaryListRowViewModel>
            val insetTextKey = content["gasInsetTextKey"]

            assertEquals(1, gasSupplyRows.size)
            assertEquals(3, gasCertRows.size)
            assertNull(insetTextKey)
        }
    }

    @Nested
    inner class GetElectricalSafetyCyaContent {
        @Mock
        lateinit var mockState: ElectricalSafetyState

        private val mockHasElectricalCertStep: HasElectricalCertStep = mock()

        @Test
        fun `provide later for occupied property returns 1 row and null inset text key`() {
            whenever(mockState.hasElectricalCertStep).thenReturn(mockHasElectricalCertStep)
            whenever(mockHasElectricalCertStep.currentJourneyId).thenReturn("test-journey-id")
            whenever(mockHasElectricalCertStep.outcome).thenReturn(HasElectricalCertMode.PROVIDE_THIS_LATER)
            whenever(mockState.isOccupied).thenReturn(true)

            val content = helper.getElectricalSafetyCyaContent(mockState)

            @Suppress("UNCHECKED_CAST")
            val rows = content["electricalRows"] as List<SummaryListRowViewModel>
            val insetTextKey = content["electricalInsetTextKey"]

            assertEquals(1, rows.size)
            assertNull(insetTextKey)
        }

        @Test
        fun `no cert for occupied property returns 1 row and occupiedNoCert inset text key`() {
            whenever(mockState.hasElectricalCertStep).thenReturn(mockHasElectricalCertStep)
            whenever(mockHasElectricalCertStep.currentJourneyId).thenReturn("test-journey-id")
            whenever(mockHasElectricalCertStep.outcome).thenReturn(HasElectricalCertMode.NO_CERTIFICATE)
            whenever(mockState.isOccupied).thenReturn(true)

            val content = helper.getElectricalSafetyCyaContent(mockState)

            @Suppress("UNCHECKED_CAST")
            val rows = content["electricalRows"] as List<SummaryListRowViewModel>
            val insetTextKey = content["electricalInsetTextKey"]

            assertEquals(1, rows.size)
            assertEquals("checkElectricalSafety.occupiedNoCertInsetText", insetTextKey)
        }
    }

    @Nested
    inner class GetEpcCyaContent {
        @Mock
        lateinit var mockState: EpcState

        private val mockHasEpcStep: HasEpcStep = mock()

        @Test
        fun `skipped occupied returns all expected keys with null epcCardTitle and non-empty nonEpcRows`() {
            whenever(mockState.hasEpcStep).thenReturn(mockHasEpcStep)
            whenever(mockHasEpcStep.outcome).thenReturn(HasEpcMode.PROVIDE_LATER)
            whenever(mockHasEpcStep.currentJourneyId).thenReturn("test-journey-id")
            whenever(mockState.isOccupied).thenReturn(true)

            val content = helper.getEpcCyaContent(mockState)

            assertEquals(
                setOf(
                    "epcCardTitle",
                    "epcCardActions",
                    "epcCardRows",
                    "epcExpiredTextKey",
                    "tenancyCheckRows",
                    "lowRatingTextKey",
                    "exemptionReasonRows",
                    "nonEpcRows",
                    "epcInsetTextKey",
                ),
                content.keys,
            )
            assertNull(content["epcCardTitle"])
            assertNull(content["epcCardActions"])
            assertNull(content["epcCardRows"])

            @Suppress("UNCHECKED_CAST")
            val nonEpcRows = content["nonEpcRows"] as List<SummaryListRowViewModel>
            assertTrue(nonEpcRows.isNotEmpty())
        }
    }
}
