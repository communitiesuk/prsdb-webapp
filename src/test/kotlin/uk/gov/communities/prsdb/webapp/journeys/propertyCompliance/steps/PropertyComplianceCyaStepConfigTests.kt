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
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.PropertyComplianceJourneyState
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafeEngineerNumFormModel
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

    @BeforeEach
    fun setup() {
        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockState.gasSafetyEngineerNumberStep).thenReturn(mockGasSafetyEngineerNumberStep)
        whenever(mockState.gasSafetyExemptionReasonStep).thenReturn(mockGasSafetyExemptionReasonStep)
        whenever(mockState.gasSafetyExemptionOtherReasonStep).thenReturn(mockGasSafetyExemptionOtherReasonStep)
        whenever(mockState.eicrExemptionReasonStep).thenReturn(mockEicrExemptionReasonStep)
        whenever(mockState.eicrExemptionOtherReasonStep).thenReturn(mockEicrExemptionOtherReasonStep)
        whenever(mockState.epcExpiryCheckStep).thenReturn(mockEpcExpiryCheckStep)
        whenever(mockState.epcExemptionReasonStep).thenReturn(mockEpcExemptionReasonStep)
        whenever(mockState.meesExemptionReasonStep).thenReturn(mockMeesExemptionReasonStep)
    }

    @Test
    fun `afterStepDataIsAdded creates a propertyCompliance record with valid certificates`() {
        // Arrange
        val gasSafetyIssueDate = LocalDate.now()
        val eicrIssueDate = LocalDate.now().minusDays(1)
        val epcDetails = MockEpcData.createEpcDataModel(energyRating = "C")

        setupValidCertificatesState(
            gasSafetyIssueDate,
            eicrIssueDate,
            epcDetails,
        )

        // Act
        stepConfig.afterStepDataIsAdded(mockState)

        // Assert
        verify(mockPropertyComplianceService).createPropertyCompliance(
            propertyOwnershipId = propertyId,
            gasSafetyCertUploadId = fileUploadId,
            gasSafetyCertIssueDate = gasSafetyIssueDate,
            gasSafetyCertEngineerNum = "1234567",
            gasSafetyCertExemptionReason = null,
            gasSafetyCertExemptionOtherReason = null,
            eicrUploadId = fileUploadId,
            eicrIssueDate = eicrIssueDate,
            eicrExemptionReason = null,
            eicrExemptionOtherReason = null,
            epcUrl = "epc.url.com",
            epcExpiryDate = epcDetails.expiryDate.toJavaLocalDate(),
            tenancyStartedBeforeEpcExpiry = null,
            epcEnergyRating = epcDetails.energyRating,
            epcExemptionReason = null,
            epcMeesExemptionReason = null,
        )
        verify(mockPropertyComplianceService).addToPropertiesWithComplianceAddedThisSession(propertyId)
        verify(mockPropertyOwnershipService).deleteIncompleteComplianceForm(propertyId)
    }

  /*  @Test
    fun `afterStepDataIsAdded creates a propertyCompliance record with exemptions`() {
        // Arrange
        setupExemptionsState()

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
        verify(mockPropertyComplianceService).addToPropertiesWithComplianceAddedThisSession(propertyId)
        verify(mockPropertyOwnershipService).deleteIncompleteComplianceForm(propertyId)
    }

    @Test
    fun `afterStepDataIsAdded creates a propertyCompliance with mees exemption and epc expiry data`() {
        // Arrange
        val epcDetails =
            MockEpcData.createEpcDataModel(
                energyRating = "F",
                expiryDate = LocalDate.now().minusDays(10).toKotlinLocalDate(),
            )

        setupMeesExemptionState(epcDetails)

        // Act
        stepConfig.afterStepDataIsAdded(mockState)

        // Assert
        verify(mockPropertyComplianceService).createPropertyCompliance(
            propertyOwnershipId = propertyId,
            gasSafetyCertUploadId = fileUploadId,
            gasSafetyCertIssueDate = any(),
            gasSafetyCertEngineerNum = any(),
            gasSafetyCertExemptionReason = null,
            gasSafetyCertExemptionOtherReason = null,
            eicrUploadId = fileUploadId,
            eicrIssueDate = any(),
            eicrExemptionReason = null,
            eicrExemptionOtherReason = null,
            epcUrl = any(),
            epcExpiryDate = epcDetails.expiryDate.toJavaLocalDate(),
            tenancyStartedBeforeEpcExpiry = true,
            epcEnergyRating = epcDetails.energyRating,
            epcExemptionReason = null,
            epcMeesExemptionReason = MeesExemptionReason.PROPERTY_DEVALUATION,
        )
    }

    @Test
    fun `afterStepDataIsAdded sends a FullPropertyComplianceConfirmationEmail for a fully compliant property`() {
        // Arrange
        val epcDetails = MockEpcData.createEpcDataModel(energyRating = "C")
        val expectedCompliance =
            PropertyComplianceBuilder()
                .withPropertyOwnership(MockLandlordData.createPropertyOwnership(id = propertyId))
                .withGasSafetyCert()
                .withEicr()
                .withEpc()
                .build()

        setupValidCertificatesState(LocalDate.now(), LocalDate.now(), epcDetails)
        whenever(
            mockPropertyComplianceService.createPropertyCompliance(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
            ),
        ).thenReturn(expectedCompliance)

        val dashboardUri = URI.create("https://example.com/dashboard")
        whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(dashboardUri)
        whenever(mockMessageSource.getMessage(any<String>(), any(), any())).thenAnswer { it.getArgument(0) }

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
        val expectedCompliance =
            PropertyComplianceBuilder()
                .withPropertyOwnership(MockLandlordData.createPropertyOwnership(id = propertyId))
                .build()

        setupExemptionsState()
        whenever(
            mockPropertyComplianceService.createPropertyCompliance(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
            ),
        ).thenReturn(expectedCompliance)

        val complianceInfoUri = URI.create("https://example.com/compliance-info/$propertyId")
        whenever(mockAbsoluteUrlProvider.buildComplianceInformationUri(propertyId)).thenReturn(complianceInfoUri)
        whenever(mockMessageSource.getMessage(any<String>(), any(), any())).thenAnswer { it.getArgument(0) }

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
        val epcDetails = MockEpcData.createEpcDataModel()
        val expectedCompliance = PropertyComplianceBuilder().build()

        setupValidCertificatesState(LocalDate.now(), LocalDate.now(), epcDetails)
        whenever(
            mockPropertyComplianceService.createPropertyCompliance(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
            ),
        ).thenReturn(expectedCompliance)
        whenever(mockMessageSource.getMessage(any<String>(), any(), any())).thenAnswer { it.getArgument(0) }
        whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI.create("https://example.com/dashboard"))

        // Act
        stepConfig.afterStepDataIsAdded(mockState)

        // Assert
        verify(mockPropertyComplianceService).addToPropertiesWithComplianceAddedThisSession(propertyId)
    }

    @Test
    fun `afterStepDataIsAdded deletes the incomplete property compliance from the database`() {
        // Arrange
        val epcDetails = MockEpcData.createEpcDataModel()
        val expectedCompliance = PropertyComplianceBuilder().build()

        setupValidCertificatesState(LocalDate.now(), LocalDate.now(), epcDetails)
        whenever(
            mockPropertyComplianceService.createPropertyCompliance(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
            ),
        ).thenReturn(expectedCompliance)
        whenever(mockMessageSource.getMessage(any<String>(), any(), any())).thenAnswer { it.getArgument(0) }
        whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI.create("https://example.com/dashboard"))

        // Act
        stepConfig.afterStepDataIsAdded(mockState)

        // Assert
        verify(mockPropertyOwnershipService).deleteIncompleteComplianceForm(propertyId)
    }*/

    // Helper methods to set up state

    private fun setupValidCertificatesState(
        gasSafetyIssueDate: LocalDate,
        eicrIssueDate: LocalDate,
        epcDetails: EpcDataModel,
    ): PropertyCompliance {
        // Mock gas safety certificate steps
        whenever(mockState.getGasSafetyCertificateFileUploadIdIfReachable()).thenReturn(fileUploadId)
        whenever(mockState.getGasSafetyCertificateIssueDateIfReachable()).thenReturn(gasSafetyIssueDate.toKotlinLocalDate())
        val gasSafetyEngineerNumFormModel = GasSafeEngineerNumFormModel().apply { engineerNumber = "1234567" }
        whenever(mockState.gasSafetyEngineerNumberStep.formModelIfReachableOrNull).thenReturn(gasSafetyEngineerNumFormModel)

        // Mock EICR certificate steps
        whenever(mockState.getEicrCertificateFileUploadId()).thenReturn(fileUploadId)
        whenever(mockState.getEicrCertificateIssueDate()).thenReturn(eicrIssueDate.toKotlinLocalDate())

        // Mock EPC
        whenever(mockState.acceptedEpc).thenReturn(epcDetails)
        whenever(mockEpcCertificateUrlProvider.getEpcCertificateUrl(epcDetails.certificateNumber)).thenReturn("epc.url.com")
        whenever(mockEpcExpiryCheckStep.formModelIfReachableOrNull).thenReturn(null)
        whenever(mockEpcExemptionReasonStep.formModelIfReachableOrNull).thenReturn(null)
        whenever(mockMeesExemptionReasonStep.formModelIfReachableOrNull).thenReturn(null)

        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyId)
        val expectedCompliance =
            PropertyComplianceBuilder()
                .withPropertyOwnership(propertyOwnership)
                .withGasSafetyCert(gasSafetyIssueDate)
                .withEicr(eicrIssueDate)
                .withEpc()
                .build()

        setupCreatePropertyComplianceStub(expectedCompliance)

        whenever(mockMessageSource.getMessage(any<String>(), anyOrNull(), anyOrNull())).thenAnswer { it.getArgument(0) }
        whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI.create("https://example.com/dashboard"))

        return expectedCompliance
    }

/*    private fun setupExemptionsState(): PropertyCompliance {
        val gasSafetyExemptionOtherReason = "Gas safety exemption reason"
        val eicrExemptionOtherReason = "EICR exemption reason"

        // Mock gas safety exemption
        whenever(mockState.getGasSafetyCertificateFileUploadIdIfReachable()).thenReturn(null)
        whenever(mockState.getGasSafetyCertificateIssueDateIfReachable()).thenReturn(null)
        whenever(mockGasSafetyEngineerNumberStep.formModelIfReachableOrNull).thenReturn(null)
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

        // Mock EICR exemption
        whenever(mockState.getEicrCertificateFileUploadId()).thenReturn(null)
        whenever(mockState.getEicrCertificateIssueDate()).thenReturn(null)
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

        // Mock EPC exemption
        whenever(mockState.acceptedEpc).thenReturn(null)
        whenever(mockEpcExpiryCheckStep.formModelIfReachableOrNull).thenReturn(null)
        val epcExemptionReasonFormModel =
            EpcExemptionReasonFormModel().apply {
                exemptionReason = EpcExemptionReason.DUE_FOR_DEMOLITION
            }
        whenever(mockEpcExemptionReasonStep.formModelIfReachableOrNull).thenReturn(epcExemptionReasonFormModel)
        whenever(mockMeesExemptionReasonStep.formModelIfReachableOrNull).thenReturn(null)

        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyId)
        val expectedCompliance =
            PropertyComplianceBuilder()
                .withPropertyOwnership(propertyOwnership)
                .withGasSafetyCertExemption(GasSafetyExemptionReason.OTHER)
                .withEicrExemption(EicrExemptionReason.OTHER)
                .withEpcExemption(EpcExemptionReason.DUE_FOR_DEMOLITION)
                .build()
        expectedCompliance.gasSafetyCertExemptionOtherReason = gasSafetyExemptionOtherReason
        expectedCompliance.eicrExemptionOtherReason = eicrExemptionOtherReason

        whenever(
            mockPropertyComplianceService.createPropertyCompliance(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
            ),
        ).thenReturn(expectedCompliance)
        whenever(mockMessageSource.getMessage(any<String>(), any(), any())).thenAnswer { it.getArgument(0) }
        whenever(mockAbsoluteUrlProvider.buildComplianceInformationUri(propertyId)).thenReturn(URI.create("https://example.com/compliance"))

        return expectedCompliance
    }

    private fun setupMeesExemptionState(epcDetails: EpcDataModel): PropertyCompliance {
        // Mock gas safety and EICR certificates (valid)
        whenever(mockState.getGasSafetyCertificateFileUploadIdIfReachable()).thenReturn(fileUploadId)
        whenever(mockState.getGasSafetyCertificateIssueDateIfReachable()).thenReturn(LocalDate.now().toKotlinLocalDate())
        val gasSafetyEngineerNumFormModel = GasSafeEngineerNumFormModel().apply { engineerNumber = "1234567" }
        whenever(mockGasSafetyEngineerNumberStep.formModelIfReachableOrNull).thenReturn(gasSafetyEngineerNumFormModel)
        whenever(mockGasSafetyExemptionReasonStep.formModelIfReachableOrNull).thenReturn(null)
        whenever(mockGasSafetyExemptionOtherReasonStep.formModelIfReachableOrNull).thenReturn(null)

        whenever(mockState.getEicrCertificateFileUploadId()).thenReturn(fileUploadId)
        whenever(mockState.getEicrCertificateIssueDate()).thenReturn(LocalDate.now().toKotlinLocalDate())
        whenever(mockEicrExemptionReasonStep.formModelIfReachableOrNull).thenReturn(null)
        whenever(mockEicrExemptionOtherReasonStep.formModelIfReachableOrNull).thenReturn(null)

        // Mock EPC with low rating, expired, and MEES exemption
        whenever(mockState.acceptedEpc).thenReturn(epcDetails)
        whenever(mockEpcCertificateUrlProvider.getEpcCertificateUrl(epcDetails.certificateNumber)).thenReturn("epc.url.com")
        val epcExpiryCheckFormModel = EpcExpiryCheckFormModel().apply { tenancyStartedBeforeExpiry = true }
        whenever(mockEpcExpiryCheckStep.formModelIfReachableOrNull).thenReturn(epcExpiryCheckFormModel)
        whenever(mockEpcExemptionReasonStep.formModelIfReachableOrNull).thenReturn(null)
        val meesExemptionReasonFormModel =
            MeesExemptionReasonFormModel().apply {
                exemptionReason = MeesExemptionReason.PROPERTY_DEVALUATION
            }
        whenever(mockMeesExemptionReasonStep.formModelIfReachableOrNull).thenReturn(meesExemptionReasonFormModel)

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

        whenever(
            mockPropertyComplianceService.createPropertyCompliance(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
            ),
        ).thenReturn(expectedCompliance)
        whenever(mockMessageSource.getMessage(any<String>(), any(), any())).thenAnswer { it.getArgument(0) }
        whenever(mockAbsoluteUrlProvider.buildComplianceInformationUri(propertyId)).thenReturn(URI.create("https://example.com/compliance"))

        return expectedCompliance
    }*/

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
