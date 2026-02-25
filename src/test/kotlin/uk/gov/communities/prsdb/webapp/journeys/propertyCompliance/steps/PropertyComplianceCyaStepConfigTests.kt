package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import kotlinx.datetime.toKotlinLocalDate
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.constants.EICR_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.constants.GAS_SAFETY_CERT_VALIDITY_YEARS
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.PropertyComplianceJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.ExemptionMode
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrExemptionOtherReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcExpiryCheckFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafeEngineerNumFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionOtherReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.MeesExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.FullPropertyComplianceConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PartialPropertyComplianceConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.UploadService
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyComplianceBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPropertyComplianceData
import java.net.URI
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class PropertyComplianceCyaStepConfigTests {
    @Mock
    private lateinit var mockUploadService: UploadService

    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockEpcCertificateUrlProvider: EpcCertificateUrlProvider

    @Mock
    private lateinit var mockPropertyComplianceService: PropertyComplianceService

    @Mock
    private lateinit var mockMessageSource: MessageSource

    @Mock
    private lateinit var mockFullPropertyComplianceConfirmationEmailService:
        EmailNotificationService<FullPropertyComplianceConfirmationEmail>

    @Mock
    private lateinit var mockPartialPropertyComplianceConfirmationEmailService:
        EmailNotificationService<PartialPropertyComplianceConfirmationEmail>

    @Mock
    private lateinit var mockAbsoluteUrlProvider: AbsoluteUrlProvider

    @Mock
    private lateinit var mockState: PropertyComplianceJourneyState

    @Mock
    private lateinit var mockGasSafetyStep: GasSafetyStep

    @Mock
    private lateinit var mockGasSafetyIssueDateStep: GasSafetyIssueDateStep

    @Mock
    private lateinit var mockGasSafetyEngineerNumberStep: GasSafetyEngineerNumberStep

    @Mock
    private lateinit var mockGasSafetyCertificateUploadStep: GasSafetyCertificateUploadStep

    @Mock
    private lateinit var mockGasSafetyUploadConfirmationStep: GasSafetyUploadConfirmationStep

    @Mock
    private lateinit var mockGasSafetyOutdatedStep: GasSafetyOutdatedStep

    @Mock
    private lateinit var mockGasSafetyExemptionStep: GasSafetyExemptionStep

    @Mock
    private lateinit var mockGasSafetyExemptionReasonStep: GasSafetyExemptionReasonStep

    @Mock
    private lateinit var mockGasSafetyExemptionOtherReasonStep: GasSafetyExemptionOtherReasonStep

    @Mock
    private lateinit var mockGasSafetyExemptionConfirmationStep: GasSafetyExemptionConfirmationStep

    @Mock
    private lateinit var mockGasSafetyExemptionMissingStep: GasSafetyExemptionMissingStep

    @Mock
    private lateinit var mockEicrStep: EicrStep

    @Mock
    private lateinit var mockEicrIssueDateStep: EicrIssueDateStep

    @Mock
    private lateinit var mockEicrUploadConfirmationStep: EicrUploadConfirmationStep

    @Mock
    private lateinit var mockEicrOutdatedStep: EicrOutdatedStep

    @Mock
    private lateinit var mockEicrExemptionStep: EicrExemptionStep

    @Mock
    private lateinit var mockEicrExemptionReasonStep: EicrExemptionReasonStep

    @Mock
    private lateinit var mockEicrExemptionOtherReasonStep: EicrExemptionOtherReasonStep

    @Mock
    private lateinit var mockEicrExemptionConfirmationStep: EicrExemptionConfirmationStep

    @Mock
    private lateinit var mockEicrExemptionMissingStep: EicrExemptionMissingStep

    @Mock
    private lateinit var mockEpcExpiryCheckStep: EpcExpiryCheckStep

    @Mock
    private lateinit var mockEpcExemptionReasonStep: EpcExemptionReasonStep

    @Mock
    private lateinit var mockEpcExemptionConfirmationStep: EpcExemptionConfirmationStep

    @Mock
    private lateinit var mockMeesExemptionReasonStep: MeesExemptionReasonStep

    @Mock
    private lateinit var mockEpcExpiredStep: EpcExpiredStep

    @Mock
    private lateinit var mockEpcMissingStep: EpcMissingStep

    @Mock
    private lateinit var mockEpcNotFoundStep: EpcNotFoundStep

    @Mock
    private lateinit var mockEpcQuestionStep: EpcQuestionStep

    @Mock
    private lateinit var mockMeesExemptionCheckStep: MeesExemptionCheckStep

    @Mock
    private lateinit var mockLowEnergyRatingStep: LowEnergyRatingStep

    private lateinit var stepConfig: PropertyComplianceCyaStepConfig

    private val propertyId = 123L
    private val gasFileUploadId = 123L
    private val validGasSafetyIssueDate = LocalDate.now().minusDays(5)
    private val gasEngineerNumber = "1234567"
    private val eicrFileUploadId = 456L
    private val validEicrIssueDate = LocalDate.now().minusDays(5)
    private val dashboardUri = URI.create("https://example.com/dashboard")
    private val gasSafetyExemptionOtherReason = "Gas safety exemption reason"
    private val eicrExemptionOtherReason = "EICR exemption reason"
    private val epcUrl = "https://example.com/epc"
    private val validEpcExpiryDate = LocalDate.now().plusDays(10)
    private val expiredEpcExpiryDate = LocalDate.now().minusDays(5)
    private val validEpcEnergyRating = "C"
    private val lowEpcEnergyRating = "F"
    private val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyId)
    private val gasSafetyFileUpload = MockPropertyComplianceData.createFileUpload(gasFileUploadId)
    private val eicrFileUpload = MockPropertyComplianceData.createFileUpload(eicrFileUploadId)
    private val childJourneyId = "child-journey-123"

    @BeforeEach
    fun setup() {
        stepConfig =
            PropertyComplianceCyaStepConfig(
                mockUploadService,
                mockPropertyOwnershipService,
                mockEpcCertificateUrlProvider,
                mockPropertyComplianceService,
                mockMessageSource,
                mockFullPropertyComplianceConfirmationEmailService,
                mockPartialPropertyComplianceConfirmationEmailService,
                mockAbsoluteUrlProvider,
            )

        whenever(mockState.cyaChildJourneyIdIfInitialized).thenReturn(childJourneyId)
        stepConfig.afterStepIsReached(mockState) // This initiliazed childJourneyId
    }

    @Nested
    inner class AfterStepDataIsAddedTests {
        @BeforeEach
        fun setUp() {
            whenever(mockState.propertyId).thenReturn(propertyId)
            whenever(mockState.gasSafetyEngineerNumberStep).thenReturn(mockGasSafetyEngineerNumberStep)
            whenever(mockState.gasSafetyExemptionReasonStep).thenReturn(mockGasSafetyExemptionReasonStep)
            whenever(mockState.gasSafetyExemptionOtherReasonStep).thenReturn(mockGasSafetyExemptionOtherReasonStep)
            whenever(mockState.eicrExemptionReasonStep).thenReturn(mockEicrExemptionReasonStep)
            whenever(mockState.eicrExemptionOtherReasonStep).thenReturn(mockEicrExemptionOtherReasonStep)
            whenever(mockState.epcExpiryCheckStep).thenReturn(mockEpcExpiryCheckStep)
            whenever(mockState.epcExemptionReasonStep).thenReturn(mockEpcExemptionReasonStep)
            whenever(mockState.meesExemptionReasonStep).thenReturn(mockMeesExemptionReasonStep)
            whenever(mockState.propertyId).thenReturn(propertyId)
            whenever(mockMessageSource.getMessage(any<String>(), anyOrNull(), anyOrNull())).thenAnswer { it.getArgument(0) }
        }

        @Test
        fun `afterStepDataIsAdded creates a propertyCompliance record with valid certificates`() {
            // Arrange
            setupValidCertificatesState()
            setupFullyCompliantPropertyCompliance()

            // Act
            stepConfig.afterStepDataIsAdded(mockState)

            // Assert
            verify(mockPropertyComplianceService).createPropertyCompliance(
                propertyOwnershipId = propertyId,
                gasSafetyCertUploadId = gasFileUploadId,
                gasSafetyCertIssueDate = validGasSafetyIssueDate,
                gasSafetyCertEngineerNum = gasEngineerNumber,
                gasSafetyCertExemptionReason = null,
                gasSafetyCertExemptionOtherReason = null,
                eicrUploadId = eicrFileUploadId,
                eicrIssueDate = validEicrIssueDate,
                eicrExemptionReason = null,
                eicrExemptionOtherReason = null,
                epcUrl = epcUrl,
                epcExpiryDate = validEpcExpiryDate,
                tenancyStartedBeforeEpcExpiry = null,
                epcEnergyRating = validEpcEnergyRating,
                epcExemptionReason = null,
                epcMeesExemptionReason = null,
            )
            verify(mockPropertyComplianceService).addToPropertiesWithComplianceAddedThisSession(propertyId)
            // TODO PDJB-467 - verify that the savedJourneyState is deleted from the database
        }

        @Test
        fun `afterStepDataIsAdded creates a propertyCompliance record with exemptions`() {
            // Arrange
            setupExemptionsState()

            val epcExemptionReasonFormModel =
                EpcExemptionReasonFormModel().apply {
                    exemptionReason = EpcExemptionReason.DUE_FOR_DEMOLITION
                }
            whenever(mockEpcExemptionReasonStep.formModelIfReachableOrNull).thenReturn(epcExemptionReasonFormModel)

            val expectedCompliance =
                PropertyComplianceBuilder()
                    .withPropertyOwnership(propertyOwnership)
                    .withGasSafetyCertOtherExemption(gasSafetyExemptionOtherReason)
                    .withEicrOtherExemption(eicrExemptionOtherReason)
                    .withEpcExemption(EpcExemptionReason.DUE_FOR_DEMOLITION)
                    .build()
            setupCreatePropertyComplianceStub(expectedCompliance)

            // Act
            stepConfig.afterStepDataIsAdded(mockState)

            // Assert
            verify(mockPropertyComplianceService).createPropertyCompliance(
                propertyOwnershipId = propertyId,
                gasSafetyCertUploadId = null,
                gasSafetyCertIssueDate = null,
                gasSafetyCertEngineerNum = null,
                gasSafetyCertExemptionReason = GasSafetyExemptionReason.OTHER,
                gasSafetyCertExemptionOtherReason = gasSafetyExemptionOtherReason,
                eicrUploadId = null,
                eicrIssueDate = null,
                eicrExemptionReason = EicrExemptionReason.OTHER,
                eicrExemptionOtherReason = eicrExemptionOtherReason,
                epcUrl = null,
                epcExpiryDate = null,
                tenancyStartedBeforeEpcExpiry = null,
                epcEnergyRating = null,
                epcExemptionReason = EpcExemptionReason.DUE_FOR_DEMOLITION,
                epcMeesExemptionReason = null,
            )
        }

        @Test
        fun `afterStepDataIsAdded creates a propertyCompliance with mees exemption and epc expiry data`() {
            // Arrange
            val expectedCompliance =
                PropertyComplianceBuilder()
                    .withPropertyOwnership(propertyOwnership)
                    .withGasSafetyCert(validGasSafetyIssueDate, gasEngineerNumber, gasSafetyFileUpload)
                    .withEicr(validEicrIssueDate, eicrFileUpload)
                    .withEpc(
                        expiryDate = expiredEpcExpiryDate,
                        energyRating = lowEpcEnergyRating,
                        epcUrl = epcUrl,
                    ).withTenancyStartedBeforeEpcExpiry(true)
                    .withMeesExemption(MeesExemptionReason.PROPERTY_DEVALUATION)
                    .build()

            setupCreatePropertyComplianceStub(expectedCompliance)
            setupMeesExemptionState()

            // Act
            stepConfig.afterStepDataIsAdded(mockState)

            // Assert
            verify(mockPropertyComplianceService).createPropertyCompliance(
                propertyOwnershipId = propertyId,
                gasSafetyCertUploadId = gasFileUploadId,
                gasSafetyCertIssueDate = validGasSafetyIssueDate,
                gasSafetyCertEngineerNum = gasEngineerNumber,
                gasSafetyCertExemptionReason = null,
                gasSafetyCertExemptionOtherReason = null,
                eicrUploadId = eicrFileUploadId,
                eicrIssueDate = validEicrIssueDate,
                eicrExemptionReason = null,
                eicrExemptionOtherReason = null,
                epcUrl = epcUrl,
                epcExpiryDate = expectedCompliance.epcExpiryDate,
                tenancyStartedBeforeEpcExpiry = true,
                epcEnergyRating = expectedCompliance.epcEnergyRating,
                epcExemptionReason = null,
                epcMeesExemptionReason = MeesExemptionReason.PROPERTY_DEVALUATION,
            )
        }

        @Test
        fun `afterStepDataIsAdded sends a FullPropertyComplianceConfirmationEmail for a fully compliant property`() {
            // Arrange
            setupValidCertificatesState()
            val expectedCompliance = setupFullyCompliantPropertyCompliance()

            // Act
            stepConfig.afterStepDataIsAdded(mockState)

            // Assert
            verify(mockFullPropertyComplianceConfirmationEmailService).sendEmail(
                eq(expectedCompliance.propertyOwnership.primaryLandlord.email),
                any<FullPropertyComplianceConfirmationEmail>(),
            )
        }

        @Test
        fun `afterStepDataIsAdded sends a PartialPropertyComplianceConfirmationEmail for a property which is not fully compliant`() {
            // Arrange
            // Create a compliance with missing certificates (non-compliant)
            val expectedCompliance = PropertyComplianceBuilder().withPropertyOwnership(propertyOwnership).build()
            setupCreatePropertyComplianceStub(expectedCompliance)

            setupStateWithMissingCertificates()

            val complianceInfoUri = URI.create("https://example.com/compliance-info/")
            whenever(
                mockAbsoluteUrlProvider.buildComplianceInformationUri(expectedCompliance.propertyOwnership.id),
            ).thenReturn(complianceInfoUri)

            // Act
            stepConfig.afterStepDataIsAdded(mockState)

            // Assert
            verify(mockPartialPropertyComplianceConfirmationEmailService).sendEmail(
                eq(expectedCompliance.propertyOwnership.primaryLandlord.email),
                any<PartialPropertyComplianceConfirmationEmail>(),
            )
        }

        @Test
        fun `afterStepDataIsAdded adds the propertyId to the session`() {
            // Arrange
            setupValidCertificatesState()
            setupFullyCompliantPropertyCompliance()

            // Act
            stepConfig.afterStepDataIsAdded(mockState)

            // Assert
            verify(mockPropertyComplianceService).addToPropertiesWithComplianceAddedThisSession(propertyId)
        }

        @Disabled
        @Test
        fun `afterStepDataIsAdded deletes the incomplete property compliance from the database`() {
            // Arrange
            setupValidCertificatesState()
            setupFullyCompliantPropertyCompliance()

            // Act
            stepConfig.afterStepDataIsAdded(mockState)

            // Assert
            // TODO PDJB-467 - enabled this and verify that the savedJourneyState is deleted from the database
        }
    }

    private fun setupValidCertificatesState() {
        val epcDetails =
            MockEpcData.createEpcDataModel(
                expiryDate = validEpcExpiryDate.toKotlinLocalDate(),
                energyRating = validEpcEnergyRating,
            )

        setupValidGasAndEicrStates()

        // Mock EPC
        whenever(mockState.acceptedEpc).thenReturn(epcDetails)
        whenever(mockEpcCertificateUrlProvider.getEpcCertificateUrl(epcDetails.certificateNumber)).thenReturn(epcUrl)

        whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(dashboardUri)
    }

    private fun setupFullyCompliantPropertyCompliance(): PropertyCompliance {
        val expectedCompliance =
            PropertyComplianceBuilder()
                .withPropertyOwnership(propertyOwnership)
                .withGasSafetyCert(validGasSafetyIssueDate, gasEngineerNumber, gasSafetyFileUpload)
                .withEicr(validEicrIssueDate, eicrFileUpload)
                .withEpc(validEpcExpiryDate, validEpcEnergyRating, epcUrl)
                .build()

        setupCreatePropertyComplianceStub(expectedCompliance)

        return expectedCompliance
    }

    private fun setupExemptionsState() {
        // Mock gas safety exemption
        val gasSafetyExemptionReasonFormModel =
            GasSafetyExemptionReasonFormModel().apply {
                exemptionReason = GasSafetyExemptionReason.OTHER
            }
        whenever(mockGasSafetyExemptionReasonStep.formModelIfReachableOrNull).thenReturn(gasSafetyExemptionReasonFormModel)
        val gasSafetyExemptionOtherReasonFormModel =
            GasSafetyExemptionOtherReasonFormModel().apply {
                otherReason = gasSafetyExemptionOtherReason
            }
        whenever(mockGasSafetyExemptionOtherReasonStep.formModelIfReachableOrNull).thenReturn(gasSafetyExemptionOtherReasonFormModel)
        whenever(mockState.getGasSafetyCertificateFileUploadIdIfReachable()).thenReturn(null)

        // Mock EICR exemption
        val eicrExemptionReasonFormModel =
            EicrExemptionReasonFormModel().apply {
                exemptionReason = EicrExemptionReason.OTHER
            }
        whenever(mockEicrExemptionReasonStep.formModelIfReachableOrNull).thenReturn(eicrExemptionReasonFormModel)
        val eicrExemptionOtherReasonFormModel =
            EicrExemptionOtherReasonFormModel().apply {
                otherReason = eicrExemptionOtherReason
            }
        whenever(mockEicrExemptionOtherReasonStep.formModelIfReachableOrNull).thenReturn(eicrExemptionOtherReasonFormModel)
        whenever(mockState.getEicrCertificateFileUploadId()).thenReturn(null)

        whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(dashboardUri)
    }

    private fun setupMeesExemptionState() {
        setupValidGasAndEicrStates()

        val epcDetails =
            MockEpcData.createEpcDataModel(
                energyRating = lowEpcEnergyRating,
                expiryDate = expiredEpcExpiryDate.toKotlinLocalDate(),
            )

        // Mock EPC with low rating, expired, and MEES exemption
        whenever(mockState.acceptedEpc).thenReturn(epcDetails)
        whenever(mockEpcCertificateUrlProvider.getEpcCertificateUrl(epcDetails.certificateNumber)).thenReturn(epcUrl)

        val epcExpiryCheckFormModel =
            EpcExpiryCheckFormModel().apply {
                tenancyStartedBeforeExpiry = true
            }
        whenever(mockEpcExpiryCheckStep.formModelIfReachableOrNull).thenReturn(epcExpiryCheckFormModel)

        val meesExemptionReasonFormModel =
            MeesExemptionReasonFormModel().apply {
                exemptionReason = MeesExemptionReason.PROPERTY_DEVALUATION
            }
        whenever(mockMeesExemptionReasonStep.formModelIfReachableOrNull).thenReturn(meesExemptionReasonFormModel)

        whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(dashboardUri)
    }

    private fun setupValidGasAndEicrStates() {
        // Mock gas safety certificate steps
        whenever(mockState.getGasSafetyCertificateFileUploadIdIfReachable()).thenReturn(gasFileUploadId)
        whenever(mockState.getGasSafetyCertificateIssueDateIfReachable()).thenReturn(validGasSafetyIssueDate.toKotlinLocalDate())
        val gasSafetyEngineerNumFormModel = GasSafeEngineerNumFormModel().apply { engineerNumber = gasEngineerNumber }
        whenever(mockState.gasSafetyEngineerNumberStep.formModelIfReachableOrNull).thenReturn(gasSafetyEngineerNumFormModel)

        // Mock EICR certificate steps
        whenever(mockState.getEicrCertificateFileUploadId()).thenReturn(eicrFileUploadId)
        whenever(mockState.getEicrCertificateIssueDate()).thenReturn(validEicrIssueDate.toKotlinLocalDate())
    }

    private fun setupStateWithMissingCertificates() {
        whenever(mockState.getGasSafetyCertificateFileUploadIdIfReachable()).thenReturn(null)
        whenever(mockState.getEicrCertificateFileUploadId()).thenReturn(null)
        whenever(mockState.acceptedEpc).thenReturn(null)
    }

    private fun setupCreatePropertyComplianceStub(expectedCompliance: PropertyCompliance) {
        reset(mockPropertyComplianceService)
        whenever(
            mockPropertyComplianceService.createPropertyCompliance(
                expectedCompliance.propertyOwnership.id,
                expectedCompliance.gasSafetyFileUpload?.id,
                expectedCompliance.gasSafetyCertIssueDate,
                expectedCompliance.gasSafetyCertEngineerNum,
                expectedCompliance.gasSafetyCertExemptionReason,
                expectedCompliance.gasSafetyCertExemptionOtherReason,
                expectedCompliance.eicrFileUpload?.id,
                expectedCompliance.eicrIssueDate,
                expectedCompliance.eicrExemptionReason,
                expectedCompliance.eicrExemptionOtherReason,
                expectedCompliance.epcUrl,
                expectedCompliance.epcExpiryDate,
                expectedCompliance.tenancyStartedBeforeEpcExpiry,
                expectedCompliance.epcEnergyRating,
                expectedCompliance.epcExemptionReason,
                expectedCompliance.epcMeesExemptionReason,
            ),
        ).thenReturn(expectedCompliance)
    }

    // Tests for getGasSafetyData, getEicrData, and getEpcData methods

    @Nested
    inner class GetGasSafetyDataTests {
        @BeforeEach
        fun setup() {
            whenever(mockState.gasSafetyStep).thenReturn(mockGasSafetyStep)
            // These are all checked by getGasSafetyCertStatusRow in GasSafetyCyaSummaryRowsFactory
            whenever(mockState.gasSafetyUploadConfirmationStep).thenReturn(mockGasSafetyUploadConfirmationStep)
            whenever(mockState.gasSafetyExemptionConfirmationStep).thenReturn(mockGasSafetyExemptionConfirmationStep)
            whenever(mockState.gasSafetyExemptionMissingStep).thenReturn(mockGasSafetyExemptionMissingStep)
            whenever(mockState.gasSafetyOutdatedStep).thenReturn(mockGasSafetyOutdatedStep)
            // This gets passed into GasSafetyCyaSummaryRowsFactory even if not used
            whenever(mockState.gasSafetyExemptionStep).thenReturn(mockGasSafetyExemptionStep)
        }

        @Test
        fun `getGasSafetyData returns correct rows when in-date certificate has been provided`() {
            // Arrange
            setupStateWithValidGasCertificate()

            val downloadUrl = "https://example.com/gas-cert.pdf"
            val expectedRows =
                listOf(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.gasSafety.certificate",
                        "forms.checkComplianceAnswers.gasSafety.download",
                        Destination.VisitableStep(mockState.gasSafetyStep, childJourneyId),
                        downloadUrl,
                    ),
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.certificate.issueDate",
                        validGasSafetyIssueDate.toKotlinLocalDate(),
                        Destination.VisitableStep(mockState.gasSafetyIssueDateStep, childJourneyId),
                    ),
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.certificate.validUntil",
                        validGasSafetyIssueDate.plusYears(GAS_SAFETY_CERT_VALIDITY_YEARS.toLong()).toKotlinLocalDate(),
                        Destination.Nowhere(),
                    ),
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.gasSafety.engineerNumber",
                        gasEngineerNumber,
                        Destination.VisitableStep(mockState.gasSafetyEngineerNumberStep, childJourneyId),
                    ),
                )

            // Act
            val rows = stepConfig.getGasSafetyData(mockState)

            // Assert
            assertIterableEquals(expectedRows, rows)
        }

        @Test
        fun `getGasSafetyData returns correct rows when expired certificate has been provided`() {
            // Arrange
            val expiredGasIssueDate = LocalDate.now().minusYears(2)
            setupStateWithExpiredGasCertificate()

            val expectedRows =
                listOf(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.gasSafety.certificate",
                        "forms.checkComplianceAnswers.certificate.expired",
                        Destination.VisitableStep(mockState.gasSafetyStep, childJourneyId),
                    ),
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.certificate.issueDate",
                        expiredGasIssueDate.toKotlinLocalDate(),
                        Destination.VisitableStep(mockState.gasSafetyIssueDateStep, childJourneyId),
                    ),
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.certificate.validUntil",
                        expiredGasIssueDate.plusYears(GAS_SAFETY_CERT_VALIDITY_YEARS.toLong()).toKotlinLocalDate(),
                        Destination.Nowhere(),
                    ),
                )

            // Act
            val rows = stepConfig.getGasSafetyData(mockState)

            // Assert
            assertIterableEquals(expectedRows, rows)
        }

        @Test
        fun `getGasSafetyData returns correct rows when certificate is missing`() {
            // Arrange
            setupStateWithMissingGasCertificate()

            val expectedRows =
                listOf(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.gasSafety.certificate",
                        "forms.checkComplianceAnswers.certificate.notAdded",
                        Destination.VisitableStep(mockState.gasSafetyStep, childJourneyId),
                    ),
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.certificate.exemption",
                        "commonText.none",
                        Destination.VisitableStep(mockState.gasSafetyExemptionStep, childJourneyId),
                    ),
                )

            // Act
            val rows = stepConfig.getGasSafetyData(mockState)

            // Assert
            assertIterableEquals(expectedRows, rows)
        }

        @Test
        fun `getGasSafetyData returns correct rows when exemption has been provided`() {
            // Arrange
            val exemptionReason = GasSafetyExemptionReason.NO_GAS_SUPPLY
            setupStateWithGasSafetyExemption(exemptionReason, null)

            val expectedRows =
                listOf(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.gasSafety.certificate",
                        "forms.checkComplianceAnswers.certificate.notRequired",
                        Destination.VisitableStep(mockState.gasSafetyStep, childJourneyId),
                    ),
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.certificate.exemption",
                        exemptionReason,
                        Destination.VisitableStep(mockState.gasSafetyExemptionStep, childJourneyId),
                    ),
                )

            // Act
            val rows = stepConfig.getGasSafetyData(mockState)

            // Assert
            assertIterableEquals(expectedRows, rows)
        }

        @Test
        fun `getGasSafetyData returns correct rows when OTHER exemption has been provided`() {
            // Arrange
            val otherReason = "Custom gas safety exemption reason"
            setupStateWithGasSafetyExemption(GasSafetyExemptionReason.OTHER, otherReason)

            val expectedRows =
                listOf(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.gasSafety.certificate",
                        "forms.checkComplianceAnswers.certificate.notRequired",
                        Destination.VisitableStep(mockState.gasSafetyStep, childJourneyId),
                    ),
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.certificate.exemption",
                        listOf(GasSafetyExemptionReason.OTHER, otherReason),
                        Destination.VisitableStep(mockState.gasSafetyExemptionStep, childJourneyId),
                    ),
                )

            // Act
            val rows = stepConfig.getGasSafetyData(mockState)

            // Assert
            assertIterableEquals(expectedRows, rows)
        }
    }

    @Nested
    inner class GetEicrDataTests {
        @BeforeEach
        fun setup() {
            whenever(mockState.eicrStep).thenReturn(mockEicrStep)
            whenever(mockState.eicrIssueDateStep).thenReturn(mockEicrIssueDateStep)
            whenever(mockState.eicrUploadConfirmationStep).thenReturn(mockEicrUploadConfirmationStep)
            whenever(mockState.eicrOutdatedStep).thenReturn(mockEicrOutdatedStep)
            whenever(mockState.eicrExemptionStep).thenReturn(mockEicrExemptionStep)
            whenever(mockState.eicrExemptionReasonStep).thenReturn(mockEicrExemptionReasonStep)
            whenever(mockState.eicrExemptionOtherReasonStep).thenReturn(mockEicrExemptionOtherReasonStep)
            whenever(mockState.eicrExemptionConfirmationStep).thenReturn(mockEicrExemptionConfirmationStep)
            whenever(mockState.eicrExemptionMissingStep).thenReturn(mockEicrExemptionMissingStep)
        }

        @Test
        fun `getEicrData returns correct rows when in-date certificate has been provided`() {
            // Arrange
            setupStateWithValidEicrCertificate()

            val downloadUrl = "https://example.com/eicr.pdf"
            val expectedRows =
                listOf(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.eicr.certificate",
                        "forms.checkComplianceAnswers.eicr.download",
                        Destination.VisitableStep(mockState.eicrStep, childJourneyId),
                        downloadUrl,
                    ),
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.certificate.issueDate",
                        validEicrIssueDate.toKotlinLocalDate(),
                        Destination.VisitableStep(mockState.eicrIssueDateStep, childJourneyId),
                    ),
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.certificate.validUntil",
                        validEicrIssueDate.plusYears(EICR_VALIDITY_YEARS.toLong()).toKotlinLocalDate(),
                        Destination.Nowhere(),
                    ),
                )

            // Act
            val rows = stepConfig.getEicrData(mockState)

            // Assert
            assertIterableEquals(expectedRows, rows)
        }

        @Test
        fun `getEicrData returns correct rows when expired certificate has been provided`() {
            // Arrange
            val expiredEicrIssueDate = LocalDate.now().minusYears(6)
            setupStateWithExpiredEicrCertificate(expiredEicrIssueDate)

            val expectedRows =
                listOf(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.eicr.certificate",
                        "forms.checkComplianceAnswers.certificate.expired",
                        Destination.VisitableStep(mockState.eicrStep, childJourneyId),
                    ),
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.certificate.issueDate",
                        expiredEicrIssueDate.toKotlinLocalDate(),
                        Destination.VisitableStep(mockState.eicrIssueDateStep, childJourneyId),
                    ),
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.certificate.validUntil",
                        expiredEicrIssueDate.plusYears(EICR_VALIDITY_YEARS.toLong()).toKotlinLocalDate(),
                        Destination.Nowhere(),
                    ),
                )

            // Act
            val rows = stepConfig.getEicrData(mockState)

            // Assert
            assertIterableEquals(expectedRows, rows)
        }

        @Test
        fun `getEicrData returns correct rows when certificate is missing`() {
            // Arrange
            setupStateWithMissingEicrCertificate()

            val expectedRows =
                listOf(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.eicr.certificate",
                        "forms.checkComplianceAnswers.certificate.notAdded",
                        Destination.VisitableStep(mockState.eicrStep, childJourneyId),
                    ),
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.certificate.exemption",
                        "commonText.none",
                        Destination.VisitableStep(mockState.eicrExemptionStep, childJourneyId),
                    ),
                )

            // Act
            val rows = stepConfig.getEicrData(mockState)

            // Assert
            assertIterableEquals(expectedRows, rows)
        }

        @Test
        fun `getEicrData returns correct rows when exemption has been provided`() {
            // Arrange
            val exemptionReason = EicrExemptionReason.LONG_LEASE
            setupStateWithEicrExemption(exemptionReason, null)

            val expectedRows =
                listOf(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.eicr.certificate",
                        "forms.checkComplianceAnswers.certificate.notRequired",
                        Destination.VisitableStep(mockState.eicrStep, childJourneyId),
                    ),
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.certificate.exemption",
                        exemptionReason,
                        Destination.VisitableStep(mockState.eicrExemptionStep, childJourneyId),
                    ),
                )

            // Act
            val rows = stepConfig.getEicrData(mockState)

            // Assert
            assertIterableEquals(expectedRows, rows)
        }

        @Test
        fun `getEicrData returns correct rows when OTHER exemption has been provided`() {
            // Arrange
            val otherReason = "Custom EICR exemption reason"
            setupStateWithEicrExemption(EicrExemptionReason.OTHER, otherReason)

            val expectedRows =
                listOf(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.eicr.certificate",
                        "forms.checkComplianceAnswers.certificate.notRequired",
                        Destination.VisitableStep(mockState.eicrStep, childJourneyId),
                    ),
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.certificate.exemption",
                        listOf(EicrExemptionReason.OTHER, otherReason),
                        Destination.VisitableStep(mockState.eicrExemptionStep, childJourneyId),
                    ),
                )

            // Act
            val rows = stepConfig.getEicrData(mockState)

            // Assert
            assertIterableEquals(expectedRows, rows)
        }
    }

    @Nested
    inner class GetEpcDataTests {
        @BeforeEach
        fun setup() {
            whenever(mockState.epcExpiryCheckStep).thenReturn(mockEpcExpiryCheckStep)
            whenever(mockState.epcExemptionReasonStep).thenReturn(mockEpcExemptionReasonStep)
            whenever(mockState.epcExemptionConfirmationStep).thenReturn(mockEpcExemptionConfirmationStep)
            whenever(mockState.meesExemptionReasonStep).thenReturn(mockMeesExemptionReasonStep)
            whenever(mockState.epcExpiredStep).thenReturn(mockEpcExpiredStep)
            whenever(mockState.epcMissingStep).thenReturn(mockEpcMissingStep)
            whenever(mockState.epcNotFoundStep).thenReturn(mockEpcNotFoundStep)
            whenever(mockState.epcQuestionStep).thenReturn(mockEpcQuestionStep)
            whenever(mockState.meesExemptionCheckStep).thenReturn(mockMeesExemptionCheckStep)
            whenever(mockState.lowEnergyRatingStep).thenReturn(mockLowEnergyRatingStep)
        }

        @Test
        fun `getEpcData returns correct rows when in-date EPC has been provided`() {
            // Arrange
            val epcDetails = MockEpcData.createEpcDataModel(energyRating = validEpcEnergyRating)
            setupStateWithValidEpc(epcDetails)

            val expectedRows =
                listOf(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.epc.certificate",
                        "forms.checkComplianceAnswers.epc.view",
                        Destination.VisitableStep(mockState.epcQuestionStep, childJourneyId),
                        epcUrl,
                        valueUrlOpensNewTab = true,
                    ),
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.epc.expiryDate",
                        epcDetails.expiryDate,
                        Destination.Nowhere(),
                    ),
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.epc.energyRating",
                        validEpcEnergyRating.uppercase(),
                        Destination.Nowhere(),
                    ),
                )

            // Act
            val rows = stepConfig.getEpcData(mockState)

            // Assert
            assertIterableEquals(expectedRows, rows)
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `getEpcData returns correct rows when expired EPC has been provided`(tenancyStartedBeforeExpiry: Boolean) {
            // Arrange
            val epcDetails = MockEpcData.createEpcDataModel(expiryDate = expiredEpcExpiryDate.toKotlinLocalDate())
            setupStateWithExpiredEpc(epcDetails, tenancyStartedBeforeExpiry = tenancyStartedBeforeExpiry)

            val expectedRows =
                listOf(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.epc.certificate",
                        "forms.checkComplianceAnswers.epc.viewExpired",
                        Destination.VisitableStep(mockState.epcQuestionStep, childJourneyId),
                        epcUrl,
                        valueUrlOpensNewTab = true,
                    ),
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.epc.expiryDate",
                        epcDetails.expiryDate,
                        Destination.Nowhere(),
                    ),
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.epc.expiryCheck",
                        tenancyStartedBeforeExpiry,
                        Destination.VisitableStep(mockState.epcExpiryCheckStep, childJourneyId),
                    ),
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.epc.energyRating",
                        validEpcEnergyRating.uppercase(),
                        Destination.Nowhere(),
                    ),
                )

            // Act
            val rows = stepConfig.getEpcData(mockState)

            // Assert
            assertIterableEquals(expectedRows, rows)
        }

        @Test
        fun `getEpcData returns correct rows when EPC is missing`() {
            // Arrange
            setupStateWithMissingEpc()

            val expectedRows =
                listOf(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.epc.certificate",
                        "forms.checkComplianceAnswers.certificate.notAdded",
                        Destination.VisitableStep(mockState.epcQuestionStep, childJourneyId),
                    ),
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.epc.exemption",
                        "commonText.none",
                        Destination.VisitableStep(mockState.epcQuestionStep, childJourneyId),
                    ),
                )

            // Act
            val rows = stepConfig.getEpcData(mockState)

            // Assert
            assertIterableEquals(expectedRows, rows)
        }

        @Test
        fun `getEpcData returns correct rows when EPC was not found`() {
            // Arrange
            setupStateWithEpcNotFound()

            val expectedRows =
                listOf(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.epc.certificate",
                        "forms.checkComplianceAnswers.certificate.notAdded",
                        Destination.VisitableStep(mockState.epcQuestionStep, childJourneyId),
                    ),
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.epc.exemption",
                        "commonText.none",
                        Destination.VisitableStep(mockState.epcQuestionStep, childJourneyId),
                    ),
                )

            // Act
            val rows = stepConfig.getEpcData(mockState)

            // Assert
            assertIterableEquals(expectedRows, rows)
        }

        @Test
        fun `getEpcData returns correct rows when EPC exemption has been provided`() {
            // Arrange
            val exemptionReason = EpcExemptionReason.DUE_FOR_DEMOLITION
            setupStateWithEpcExemption(exemptionReason)

            val expectedRows =
                listOf(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.epc.certificate",
                        "forms.checkComplianceAnswers.certificate.notRequired",
                        Destination.VisitableStep(mockState.epcQuestionStep, childJourneyId),
                    ),
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.epc.exemption",
                        exemptionReason,
                        Destination.VisitableStep(mockState.epcExemptionReasonStep, childJourneyId),
                    ),
                )

            // Act
            val rows = stepConfig.getEpcData(mockState)

            // Assert
            assertIterableEquals(expectedRows, rows)
        }

        @Test
        fun `getEpcData returns correct rows when in-date EPC has low rating with MEES exemption`() {
            // Arrange
            val epcDetails = MockEpcData.createEpcDataModel(energyRating = lowEpcEnergyRating)
            val meesExemption = MeesExemptionReason.HIGH_COST
            setupStateWithLowRatingEpcAndMeesExemption(epcDetails, meesExemption)

            val expectedRows =
                listOf(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.epc.certificate",
                        "forms.checkComplianceAnswers.epc.view",
                        Destination.VisitableStep(mockState.epcQuestionStep, childJourneyId),
                        epcUrl,
                        valueUrlOpensNewTab = true,
                    ),
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.epc.expiryDate",
                        epcDetails.expiryDate,
                        null,
                    ),
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.epc.energyRating",
                        lowEpcEnergyRating.uppercase(),
                        Destination.Nowhere(),
                    ),
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.epc.meesExemption",
                        meesExemption,
                        Destination.VisitableStep(mockState.meesExemptionReasonStep, childJourneyId),
                    ),
                )

            // Act
            val rows = stepConfig.getEpcData(mockState)

            // Assert
            assertIterableEquals(expectedRows, rows)
        }

        @Test
        fun `getEpcData returns correct rows when in-date EPC has low rating with no MEES exemption`() {
            // Arrange
            val epcDetails = MockEpcData.createEpcDataModel(energyRating = lowEpcEnergyRating)
            setupStateWithLowRatingEpcAndNoMeesExemption(epcDetails)

            val expectedRows =
                listOf(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.epc.certificate",
                        "forms.checkComplianceAnswers.epc.view",
                        Destination.VisitableStep(mockState.epcQuestionStep, childJourneyId),
                        epcUrl,
                        valueUrlOpensNewTab = true,
                    ),
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.epc.expiryDate",
                        epcDetails.expiryDate,
                        Destination.Nowhere(),
                    ),
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.epc.energyRating",
                        lowEpcEnergyRating.uppercase(),
                        Destination.Nowhere(),
                    ),
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.epc.meesExemption",
                        "commonText.none",
                        Destination.VisitableStep(mockState.lowEnergyRatingStep, childJourneyId),
                    ),
                )

            // Act
            val rows = stepConfig.getEpcData(mockState)

            // Assert
            assertIterableEquals(expectedRows, rows)
        }

        @Test
        fun `getEpcData returns correct rows when expired EPC has low rating with MEES exemption`() {
            // Arrange
            val epcDetails =
                MockEpcData.createEpcDataModel(
                    energyRating = lowEpcEnergyRating,
                    expiryDate = expiredEpcExpiryDate.toKotlinLocalDate(),
                )
            val meesExemption = MeesExemptionReason.PROPERTY_DEVALUATION
            setupStateWithExpiredLowRatingEpcAndMeesExemption(epcDetails, meesExemption, tenancyStartedBeforeExpiry = false)

            val expectedRows =
                listOf(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.epc.certificate",
                        "forms.checkComplianceAnswers.epc.viewExpired",
                        Destination.VisitableStep(mockState.epcQuestionStep, childJourneyId),
                        epcUrl,
                        valueUrlOpensNewTab = true,
                    ),
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.epc.expiryDate",
                        epcDetails.expiryDate,
                        Destination.Nowhere(),
                    ),
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.epc.expiryCheck",
                        false,
                        Destination.VisitableStep(mockState.epcExpiryCheckStep, childJourneyId),
                    ),
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.epc.energyRating",
                        lowEpcEnergyRating.uppercase(),
                        Destination.Nowhere(),
                    ),
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.epc.meesExemption",
                        meesExemption,
                        Destination.VisitableStep(mockState.meesExemptionReasonStep, childJourneyId),
                    ),
                )

            // Act
            val rows = stepConfig.getEpcData(mockState)

            // Assert
            assertIterableEquals(expectedRows, rows)
        }

        @Test
        fun `getEpcData returns correct rows when expired EPC has low rating with no MEES exemption`() {
            // Arrange
            val epcDetails =
                MockEpcData.createEpcDataModel(
                    energyRating = lowEpcEnergyRating,
                    expiryDate = expiredEpcExpiryDate.toKotlinLocalDate(),
                )
            setupStateWithExpiredLowRatingEpcAndNoMeesExemption(epcDetails, tenancyStartedBeforeExpiry = false)

            val expectedRows =
                listOf(
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.epc.certificate",
                        "forms.checkComplianceAnswers.epc.viewExpired",
                        Destination.VisitableStep(mockState.epcQuestionStep, childJourneyId),
                        epcUrl,
                        valueUrlOpensNewTab = true,
                    ),
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.epc.expiryDate",
                        epcDetails.expiryDate,
                        Destination.Nowhere(),
                    ),
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.epc.expiryCheck",
                        false,
                        Destination.VisitableStep(mockState.epcExpiryCheckStep, childJourneyId),
                    ),
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.epc.energyRating",
                        lowEpcEnergyRating.uppercase(),
                        Destination.Nowhere(),
                    ),
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "forms.checkComplianceAnswers.epc.meesExemption",
                        "commonText.none",
                        Destination.VisitableStep(mockState.lowEnergyRatingStep, childJourneyId),
                    ),
                )

            // Act
            val rows = stepConfig.getEpcData(mockState)

            // Assert
            assertIterableEquals(expectedRows, rows)
        }
    }

    // Helper methods for setting up state scenarios

    private fun setupStateWithValidGasCertificate() {
        whenever(mockState.gasSafetyStep).thenReturn(mockGasSafetyStep)
        whenever(mockGasSafetyStep.outcome).thenReturn(GasSafetyMode.HAS_CERTIFICATE)

        whenever(mockState.gasSafetyIssueDateStep).thenReturn(mockGasSafetyIssueDateStep)
        whenever(mockState.getGasSafetyCertificateIssueDateIfReachable()).thenReturn(validGasSafetyIssueDate.toKotlinLocalDate())
        whenever(mockGasSafetyIssueDateStep.outcome).thenReturn(GasSafetyIssueDateMode.GAS_SAFETY_CERTIFICATE_IN_DATE)

        whenever(mockState.gasSafetyEngineerNumberStep).thenReturn(mockGasSafetyEngineerNumberStep)
        val engineerFormModel = GasSafeEngineerNumFormModel().apply { engineerNumber = gasEngineerNumber }
        whenever(mockGasSafetyEngineerNumberStep.formModelIfReachableOrNull).thenReturn(engineerFormModel)
        whenever(mockGasSafetyEngineerNumberStep.outcome).thenReturn(Complete.COMPLETE)

        whenever(mockState.gasSafetyCertificateUploadStep).thenReturn(mockGasSafetyCertificateUploadStep)
        whenever(mockGasSafetyCertificateUploadStep.outcome).thenReturn(Complete.COMPLETE)

        whenever(mockState.gasSafetyUploadConfirmationStep).thenReturn(mockGasSafetyUploadConfirmationStep)
        whenever(mockState.getGasSafetyCertificateFileUploadIdIfReachable()).thenReturn(gasFileUploadId)
        whenever(mockUploadService.getFileUploadById(gasFileUploadId)).thenReturn(gasSafetyFileUpload)
        whenever(mockUploadService.getDownloadUrl(gasSafetyFileUpload, "gas_safety_certificate.pdf"))
            .thenReturn("https://example.com/gas-cert.pdf")
        whenever(mockGasSafetyUploadConfirmationStep.outcome).thenReturn(Complete.COMPLETE)
    }

    private fun setupStateWithExpiredGasCertificate() {
        whenever(mockGasSafetyStep.outcome).thenReturn(GasSafetyMode.HAS_CERTIFICATE)

        whenever(mockState.gasSafetyIssueDateStep).thenReturn(mockGasSafetyIssueDateStep)
        whenever(mockGasSafetyIssueDateStep.outcome).thenReturn(GasSafetyIssueDateMode.GAS_SAFETY_CERTIFICATE_OUTDATED)

        whenever(mockGasSafetyOutdatedStep.outcome).thenReturn(Complete.COMPLETE)
    }

    private fun setupStateWithMissingGasCertificate() {
        whenever(mockGasSafetyStep.outcome).thenReturn(GasSafetyMode.NO_CERTIFICATE)

        whenever(mockState.gasSafetyExemptionReasonStep).thenReturn(mockGasSafetyExemptionReasonStep)
        whenever(mockGasSafetyExemptionReasonStep.formModelIfReachableOrNull).thenReturn(null)

        whenever(mockGasSafetyExemptionMissingStep.outcome).thenReturn(Complete.COMPLETE)
    }

    private fun setupStateWithGasSafetyExemption(
        exemptionReason: GasSafetyExemptionReason,
        otherReason: String?,
    ) {
        whenever(mockGasSafetyStep.outcome).thenReturn(GasSafetyMode.NO_CERTIFICATE)

        whenever(mockState.gasSafetyExemptionReasonStep).thenReturn(mockGasSafetyExemptionReasonStep)
        val exemptionReasonFormModel =
            GasSafetyExemptionReasonFormModel().apply { this.exemptionReason = exemptionReason }
        whenever(mockGasSafetyExemptionReasonStep.formModelIfReachableOrNull).thenReturn(exemptionReasonFormModel)

        if (otherReason != null) {
            whenever(mockState.gasSafetyExemptionOtherReasonStep).thenReturn(mockGasSafetyExemptionOtherReasonStep)
            val otherReasonFormModel = GasSafetyExemptionOtherReasonFormModel().apply { this.otherReason = otherReason }
            whenever(mockGasSafetyExemptionOtherReasonStep.formModel).thenReturn(otherReasonFormModel)
        }

        whenever(mockGasSafetyExemptionConfirmationStep.outcome).thenReturn(Complete.COMPLETE)
    }

    private fun setupStateWithValidEicrCertificate() {
        setupStateForEicrCertificate(validEicrIssueDate, EicrIssueDateMode.EICR_CERTIFICATE_IN_DATE)
    }

    private fun setupStateWithExpiredEicrCertificate(issueDate: LocalDate) {
        setupStateForEicrCertificate(issueDate, EicrIssueDateMode.EICR_CERTIFICATE_OUTDATED)
    }

    private fun setupStateForEicrCertificate(
        issueDate: LocalDate,
        issueDateMode: EicrIssueDateMode,
    ) {
        whenever(mockEicrStep.outcome).thenReturn(EicrMode.HAS_CERTIFICATE)
        whenever(mockEicrIssueDateStep.outcome).thenReturn(issueDateMode)

        whenever(mockState.getEicrCertificateFileUploadId()).thenReturn(eicrFileUploadId)
        whenever(mockUploadService.getFileUploadById(eicrFileUploadId)).thenReturn(eicrFileUpload)
        whenever(mockUploadService.getDownloadUrl(eicrFileUpload, "eicr.pdf"))
            .thenReturn("https://example.com/eicr.pdf")

        whenever(mockState.getEicrCertificateIssueDate()).thenReturn(issueDate.toKotlinLocalDate())

        // Set EICR status step outcomes
        whenever(mockEicrUploadConfirmationStep.outcome).thenReturn(Complete.COMPLETE)
        whenever(mockEicrExemptionConfirmationStep.outcome).thenReturn(null)
        whenever(mockEicrExemptionMissingStep.outcome).thenReturn(null)
        whenever(mockEicrOutdatedStep.outcome).thenReturn(null)
    }

    private fun setupStateWithMissingEicrCertificate() {
        whenever(mockEicrStep.outcome).thenReturn(EicrMode.NO_CERTIFICATE)
        whenever(mockEicrIssueDateStep.outcome).thenReturn(null)

        whenever(mockEicrExemptionReasonStep.formModelIfReachableOrNull).thenReturn(null)

        // Set EICR status step outcomes
        whenever(mockEicrUploadConfirmationStep.outcome).thenReturn(null)
        whenever(mockEicrExemptionConfirmationStep.outcome).thenReturn(null)
        whenever(mockEicrExemptionMissingStep.outcome).thenReturn(Complete.COMPLETE)
        whenever(mockEicrOutdatedStep.outcome).thenReturn(null)
    }

    private fun setupStateWithEicrExemption(
        exemptionReason: EicrExemptionReason,
        otherReason: String?,
    ) {
        whenever(mockEicrStep.outcome).thenReturn(EicrMode.NO_CERTIFICATE)
        whenever(mockEicrIssueDateStep.outcome).thenReturn(null)

        whenever(mockEicrExemptionStep.outcome).thenReturn(ExemptionMode.HAS_EXEMPTION)

        val exemptionReasonFormModel =
            EicrExemptionReasonFormModel().apply { this.exemptionReason = exemptionReason }
        whenever(mockEicrExemptionReasonStep.formModelIfReachableOrNull).thenReturn(exemptionReasonFormModel)

        if (otherReason != null) {
            val otherReasonFormModel = EicrExemptionOtherReasonFormModel().apply { this.otherReason = otherReason }
            whenever(mockEicrExemptionOtherReasonStep.formModelIfReachableOrNull).thenReturn(otherReasonFormModel)
        }

        // Set EICR status step outcomes
        whenever(mockEicrUploadConfirmationStep.outcome).thenReturn(null)
        whenever(mockEicrExemptionConfirmationStep.outcome).thenReturn(Complete.COMPLETE)
        whenever(mockEicrExemptionMissingStep.outcome).thenReturn(null)
        whenever(mockEicrOutdatedStep.outcome).thenReturn(null)
    }

    private fun setupStateWithValidEpc(epcDetails: uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel) {
        whenever(mockState.acceptedEpc).thenReturn(epcDetails)
        whenever(mockEpcCertificateUrlProvider.getEpcCertificateUrl(epcDetails.certificateNumber)).thenReturn(epcUrl)

        whenever(mockEpcExpiryCheckStep.formModelIfReachableOrNull).thenReturn(null)
        whenever(mockMeesExemptionReasonStep.formModelIfReachableOrNull).thenReturn(null)

        whenever(mockMeesExemptionCheckStep.outcome).thenReturn(null)
        whenever(mockLowEnergyRatingStep.outcome).thenReturn(null)

        // Set outcomes for EPC status steps
        whenever(mockEpcExemptionConfirmationStep.outcome).thenReturn(null)
        whenever(mockEpcExpiredStep.outcome).thenReturn(null)
        whenever(mockEpcMissingStep.outcome).thenReturn(null)
        whenever(mockEpcNotFoundStep.outcome).thenReturn(null)
    }

    private fun setupStateWithExpiredEpc(
        epcDetails: uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel,
        tenancyStartedBeforeExpiry: Boolean,
    ) {
        whenever(mockState.acceptedEpc).thenReturn(epcDetails)
        whenever(mockEpcCertificateUrlProvider.getEpcCertificateUrl(epcDetails.certificateNumber)).thenReturn(epcUrl)

        val expiryCheckFormModel =
            EpcExpiryCheckFormModel().apply { this.tenancyStartedBeforeExpiry = tenancyStartedBeforeExpiry }
        whenever(mockEpcExpiryCheckStep.formModelIfReachableOrNull).thenReturn(expiryCheckFormModel)

        whenever(mockMeesExemptionReasonStep.formModelIfReachableOrNull).thenReturn(null)

        whenever(mockMeesExemptionCheckStep.outcome).thenReturn(null)
        whenever(mockLowEnergyRatingStep.outcome).thenReturn(null)

        // Set outcomes for EPC status steps
        whenever(mockEpcExemptionConfirmationStep.outcome).thenReturn(null)
        whenever(mockEpcExpiredStep.outcome).thenReturn(Complete.COMPLETE)
        whenever(mockEpcMissingStep.outcome).thenReturn(null)
        whenever(mockEpcNotFoundStep.outcome).thenReturn(null)
    }

    private fun setupStateWithMissingEpc() {
        whenever(mockState.acceptedEpc).thenReturn(null)
        whenever(mockEpcExemptionReasonStep.formModelIfReachableOrNull).thenReturn(null)

        // Set outcomes for EPC status steps
        whenever(mockEpcExemptionConfirmationStep.outcome).thenReturn(null)
        whenever(mockEpcExpiredStep.outcome).thenReturn(null)
        whenever(mockEpcMissingStep.outcome).thenReturn(Complete.COMPLETE)
        whenever(mockEpcNotFoundStep.outcome).thenReturn(null)
    }

    private fun setupStateWithEpcNotFound() {
        whenever(mockState.acceptedEpc).thenReturn(null)
        whenever(mockEpcExemptionReasonStep.formModelIfReachableOrNull).thenReturn(null)

        // Set outcomes for EPC status steps
        whenever(mockEpcExemptionConfirmationStep.outcome).thenReturn(null)
        whenever(mockEpcExpiredStep.outcome).thenReturn(null)
        whenever(mockEpcMissingStep.outcome).thenReturn(null)
        whenever(mockEpcNotFoundStep.outcome).thenReturn(Complete.COMPLETE)
    }

    private fun setupStateWithEpcExemption(exemptionReason: EpcExemptionReason) {
        whenever(mockState.acceptedEpc).thenReturn(null)

        val exemptionReasonFormModel =
            EpcExemptionReasonFormModel().apply { this.exemptionReason = exemptionReason }
        whenever(mockEpcExemptionReasonStep.formModelIfReachableOrNull).thenReturn(exemptionReasonFormModel)

        // Set outcomes for EPC status steps
        whenever(mockEpcExemptionConfirmationStep.outcome).thenReturn(Complete.COMPLETE)
        whenever(mockEpcExpiredStep.outcome).thenReturn(null)
        whenever(mockEpcMissingStep.outcome).thenReturn(null)
        whenever(mockEpcNotFoundStep.outcome).thenReturn(null)
    }

    private fun setupStateWithLowRatingEpcAndMeesExemption(
        epcDetails: uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel,
        meesExemption: MeesExemptionReason,
    ) {
        whenever(mockState.acceptedEpc).thenReturn(epcDetails)
        whenever(mockEpcCertificateUrlProvider.getEpcCertificateUrl(epcDetails.certificateNumber)).thenReturn(epcUrl)

        whenever(mockEpcExpiryCheckStep.formModelIfReachableOrNull).thenReturn(null)

        whenever(mockMeesExemptionCheckStep.outcome).thenReturn(ExemptionMode.HAS_EXEMPTION)

        val meesExemptionFormModel =
            MeesExemptionReasonFormModel().apply { this.exemptionReason = meesExemption }
        whenever(mockMeesExemptionReasonStep.formModelIfReachableOrNull).thenReturn(meesExemptionFormModel)

        whenever(mockLowEnergyRatingStep.outcome).thenReturn(null)

        // Set outcomes for EPC status steps
        whenever(mockEpcExemptionConfirmationStep.outcome).thenReturn(null)
        whenever(mockEpcExpiredStep.outcome).thenReturn(null)
        whenever(mockEpcMissingStep.outcome).thenReturn(null)
        whenever(mockEpcNotFoundStep.outcome).thenReturn(null)
    }

    private fun setupStateWithLowRatingEpcAndNoMeesExemption(epcDetails: uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel) {
        whenever(mockState.acceptedEpc).thenReturn(epcDetails)
        whenever(mockEpcCertificateUrlProvider.getEpcCertificateUrl(epcDetails.certificateNumber)).thenReturn(epcUrl)

        whenever(mockEpcExpiryCheckStep.formModelIfReachableOrNull).thenReturn(null)

        whenever(mockMeesExemptionCheckStep.outcome).thenReturn(ExemptionMode.NO_EXEMPTION)

        whenever(mockLowEnergyRatingStep.outcome).thenReturn(Complete.COMPLETE)

        whenever(mockMeesExemptionReasonStep.formModelIfReachableOrNull).thenReturn(null)

        // Set outcomes for EPC status steps
        whenever(mockEpcExemptionConfirmationStep.outcome).thenReturn(null)
        whenever(mockEpcExpiredStep.outcome).thenReturn(null)
        whenever(mockEpcMissingStep.outcome).thenReturn(null)
        whenever(mockEpcNotFoundStep.outcome).thenReturn(null)
    }

    private fun setupStateWithExpiredLowRatingEpcAndMeesExemption(
        epcDetails: uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel,
        meesExemption: MeesExemptionReason,
        tenancyStartedBeforeExpiry: Boolean,
    ) {
        whenever(mockState.acceptedEpc).thenReturn(epcDetails)
        whenever(mockEpcCertificateUrlProvider.getEpcCertificateUrl(epcDetails.certificateNumber)).thenReturn(epcUrl)

        val expiryCheckFormModel =
            EpcExpiryCheckFormModel().apply { this.tenancyStartedBeforeExpiry = tenancyStartedBeforeExpiry }
        whenever(mockEpcExpiryCheckStep.formModelIfReachableOrNull).thenReturn(expiryCheckFormModel)

        whenever(mockMeesExemptionCheckStep.outcome).thenReturn(ExemptionMode.HAS_EXEMPTION)

        val meesExemptionFormModel =
            MeesExemptionReasonFormModel().apply { this.exemptionReason = meesExemption }
        whenever(mockMeesExemptionReasonStep.formModelIfReachableOrNull).thenReturn(meesExemptionFormModel)

        whenever(mockLowEnergyRatingStep.outcome).thenReturn(null)

        // Set outcomes for EPC status steps
        whenever(mockEpcExemptionConfirmationStep.outcome).thenReturn(null)
        whenever(mockEpcExpiredStep.outcome).thenReturn(Complete.COMPLETE)
        whenever(mockEpcMissingStep.outcome).thenReturn(null)
        whenever(mockEpcNotFoundStep.outcome).thenReturn(null)
    }

    private fun setupStateWithExpiredLowRatingEpcAndNoMeesExemption(
        epcDetails: uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel,
        tenancyStartedBeforeExpiry: Boolean,
    ) {
        whenever(mockState.acceptedEpc).thenReturn(epcDetails)
        whenever(mockEpcCertificateUrlProvider.getEpcCertificateUrl(epcDetails.certificateNumber)).thenReturn(epcUrl)

        val expiryCheckFormModel =
            EpcExpiryCheckFormModel().apply { this.tenancyStartedBeforeExpiry = tenancyStartedBeforeExpiry }
        whenever(mockEpcExpiryCheckStep.formModelIfReachableOrNull).thenReturn(expiryCheckFormModel)

        whenever(mockMeesExemptionCheckStep.outcome).thenReturn(ExemptionMode.NO_EXEMPTION)

        whenever(mockLowEnergyRatingStep.outcome).thenReturn(Complete.COMPLETE)

        whenever(mockMeesExemptionReasonStep.formModelIfReachableOrNull).thenReturn(null)

        // Set outcomes for EPC status steps
        whenever(mockEpcExemptionConfirmationStep.outcome).thenReturn(null)
        whenever(mockEpcExpiredStep.outcome).thenReturn(Complete.COMPLETE)
        whenever(mockEpcMissingStep.outcome).thenReturn(null)
        whenever(mockEpcNotFoundStep.outcome).thenReturn(null)
    }
}
