package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.PropertyComplianceJourneyState
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
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
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.UploadService
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyComplianceBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
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
    private lateinit var mockGasSafetyEngineerNumberStep: GasSafetyEngineerNumberStep

    @Mock
    private lateinit var mockGasSafetyExemptionReasonStep: GasSafetyExemptionReasonStep

    @Mock
    private lateinit var mockGasSafetyExemptionOtherReasonStep: GasSafetyExemptionOtherReasonStep

    @Mock
    private lateinit var mockEicrExemptionReasonStep: EicrExemptionReasonStep

    @Mock
    private lateinit var mockEicrExemptionOtherReasonStep: EicrExemptionOtherReasonStep

    @Mock
    private lateinit var mockEpcExpiryCheckStep: EpcExpiryCheckStep

    @Mock
    private lateinit var mockEpcExemptionReasonStep: EpcExemptionReasonStep

    @Mock
    private lateinit var mockMeesExemptionReasonStep: MeesExemptionReasonStep

    @InjectMocks
    private lateinit var stepConfig: PropertyComplianceCyaStepConfig

    private val propertyId = 123L
    private val fileUploadId = 456L
    private val validGasSafetyIssueDate = LocalDate.now().minusDays(1)
    private val gasEngineerNumber = "1234567"
    private val validEicrIssueDate = LocalDate.now().minusDays(1)
    private val dashboardUri = URI.create("https://example.com/dashboard")
    private val gasSafetyExemptionOtherReason = "Gas safety exemption reason"
    private val eicrExemptionOtherReason = "EICR exemption reason"
    private val epcUrl = "https://example.com/epc"

    @BeforeEach
    fun setup() {
        // Setup common stubs used across multiple tests
        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockState.gasSafetyEngineerNumberStep).thenReturn(mockGasSafetyEngineerNumberStep)
        whenever(mockState.gasSafetyExemptionReasonStep).thenReturn(mockGasSafetyExemptionReasonStep)
        whenever(mockState.gasSafetyExemptionOtherReasonStep).thenReturn(mockGasSafetyExemptionOtherReasonStep)
        whenever(mockState.eicrExemptionReasonStep).thenReturn(mockEicrExemptionReasonStep)
        whenever(mockState.eicrExemptionOtherReasonStep).thenReturn(mockEicrExemptionOtherReasonStep)
        whenever(mockState.epcExpiryCheckStep).thenReturn(mockEpcExpiryCheckStep)
        whenever(mockState.epcExemptionReasonStep).thenReturn(mockEpcExemptionReasonStep)
        whenever(mockState.meesExemptionReasonStep).thenReturn(mockMeesExemptionReasonStep)
        whenever(mockMessageSource.getMessage(any<String>(), anyOrNull(), anyOrNull())).thenAnswer { it.getArgument(0) }
    }

    @Test
    fun `afterStepDataIsAdded creates a propertyCompliance record with valid certificates`() {
        // Arrange
        val epcDetails = MockEpcData.createEpcDataModel()

        setupValidCertificatesState()
        setupFullyCompliantPropertyCompliance()
        // TODO PDJB-467 - do we need to set up the correct compliance or just make sure it returns something?
        // This compliance might not exactly match our state anyway

        // Act
        stepConfig.afterStepDataIsAdded(mockState)

        // Assert
        verify(mockPropertyComplianceService).createPropertyCompliance(
            propertyOwnershipId = propertyId,
            gasSafetyCertUploadId = fileUploadId,
            gasSafetyCertIssueDate = validGasSafetyIssueDate,
            gasSafetyCertEngineerNum = gasEngineerNumber,
            gasSafetyCertExemptionReason = null,
            gasSafetyCertExemptionOtherReason = null,
            eicrUploadId = fileUploadId,
            eicrIssueDate = validEicrIssueDate,
            eicrExemptionReason = null,
            eicrExemptionOtherReason = null,
            epcUrl = epcUrl,
            epcExpiryDate = epcDetails.expiryDate.toJavaLocalDate(),
            tenancyStartedBeforeEpcExpiry = null,
            epcEnergyRating = epcDetails.energyRating,
            epcExemptionReason = null,
            epcMeesExemptionReason = null,
        )
        verify(mockPropertyComplianceService).addToPropertiesWithComplianceAddedThisSession(propertyId)
        verify(mockPropertyOwnershipService).deleteIncompleteComplianceForm(propertyId)
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
            PropertyComplianceBuilder.createWithCertOtherExemptions(gasSafetyExemptionOtherReason, eicrExemptionOtherReason)
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
            gasSafetyCertExemptionOtherReason = "Gas safety exemption reason",
            eicrUploadId = null,
            eicrIssueDate = null,
            eicrExemptionReason = EicrExemptionReason.OTHER,
            eicrExemptionOtherReason = "EICR exemption reason",
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
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyId)
        val expectedCompliance =
            PropertyComplianceBuilder()
                .withPropertyOwnership(propertyOwnership)
                .withGasSafetyCert()
                .withEicr()
                .withExpiredEpc()
                .withLowEpcRating()
                .withTenancyStartedBeforeEpcExpiry()
                .withMeesExemption(MeesExemptionReason.PROPERTY_DEVALUATION)
                .build()

        setupCreatePropertyComplianceStub(expectedCompliance)
        setupMeesExemptionState(
            MockEpcData.createEpcDataModel(
                energyRating = expectedCompliance.epcEnergyRating!!,
                expiryDate = expectedCompliance.epcExpiryDate?.toKotlinLocalDate()!!,
            ),
        )

        // Act
        stepConfig.afterStepDataIsAdded(mockState)

        // Assert
        verify(mockPropertyComplianceService).createPropertyCompliance(
            propertyOwnershipId = propertyId,
            gasSafetyCertUploadId = fileUploadId,
            gasSafetyCertIssueDate = validGasSafetyIssueDate,
            gasSafetyCertEngineerNum = gasEngineerNumber,
            gasSafetyCertExemptionReason = null,
            gasSafetyCertExemptionOtherReason = null,
            eicrUploadId = fileUploadId,
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
        // TODO PDJB-467 - the email tests are flaky.
        // Is it to do with this setup?
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
        val expectedCompliance = PropertyComplianceBuilder.createWithMissingCerts()
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

    @Test
    fun `afterStepDataIsAdded deletes the incomplete property compliance from the database`() {
        // Arrange
        setupValidCertificatesState()
        setupFullyCompliantPropertyCompliance()

        // Act
        stepConfig.afterStepDataIsAdded(mockState)

        // Assert
        verify(mockPropertyOwnershipService).deleteIncompleteComplianceForm(propertyId)
    }

    private fun setupValidCertificatesState() {
        val epcDetails = MockEpcData.createEpcDataModel()

        // Mock gas safety certificate steps
        whenever(mockState.getGasSafetyCertificateFileUploadIdIfReachable()).thenReturn(fileUploadId)
        whenever(mockState.getGasSafetyCertificateIssueDateIfReachable()).thenReturn(validGasSafetyIssueDate.toKotlinLocalDate())
        val gasSafetyEngineerNumFormModel = GasSafeEngineerNumFormModel().apply { engineerNumber = gasEngineerNumber }
        whenever(mockState.gasSafetyEngineerNumberStep.formModelIfReachableOrNull).thenReturn(gasSafetyEngineerNumFormModel)

        // Mock EICR certificate steps
        whenever(mockState.getEicrCertificateFileUploadId()).thenReturn(fileUploadId)
        whenever(mockState.getEicrCertificateIssueDate()).thenReturn(validEicrIssueDate.toKotlinLocalDate())

        // Mock EPC
        whenever(mockState.acceptedEpc).thenReturn(epcDetails)
        whenever(mockEpcCertificateUrlProvider.getEpcCertificateUrl(epcDetails.certificateNumber)).thenReturn(epcUrl)

        whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(dashboardUri)
    }

    private fun setupFullyCompliantPropertyCompliance(): PropertyCompliance {
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyId)
        val expectedCompliance =
            PropertyComplianceBuilder()
                .withPropertyOwnership(propertyOwnership)
                .withGasSafetyCert(validGasSafetyIssueDate)
                .withEicr(validEicrIssueDate)
                .withEpc()
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

    private fun setupMeesExemptionState(epcDetails: EpcDataModel) {
        // Mock gas safety and EICR certificates (valid)
        whenever(mockState.getGasSafetyCertificateFileUploadIdIfReachable()).thenReturn(fileUploadId)
        whenever(mockState.getGasSafetyCertificateIssueDateIfReachable()).thenReturn(validGasSafetyIssueDate.toKotlinLocalDate())
        val gasSafetyEngineerNumFormModel = GasSafeEngineerNumFormModel().apply { engineerNumber = gasEngineerNumber }
        whenever(mockGasSafetyEngineerNumberStep.formModelIfReachableOrNull).thenReturn(gasSafetyEngineerNumFormModel)

        whenever(mockState.getEicrCertificateFileUploadId()).thenReturn(fileUploadId)
        whenever(mockState.getEicrCertificateIssueDate()).thenReturn(validEicrIssueDate.toKotlinLocalDate())

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

    private fun setupStateWithMissingCertificates() {
        whenever(mockState.getGasSafetyCertificateFileUploadIdIfReachable()).thenReturn(null)
        whenever(mockState.getEicrCertificateFileUploadId()).thenReturn(null)
        whenever(mockState.acceptedEpc).thenReturn(null)
    }

    private fun setupCreatePropertyComplianceStub(expectedCompliance: PropertyCompliance) =
        whenever(
            mockPropertyComplianceService.createPropertyCompliance(
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
            ),
        ).thenReturn(expectedCompliance)
}
