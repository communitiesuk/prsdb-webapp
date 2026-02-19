package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.helpers.extensions.MessageSourceExtensions.Companion.getMessageForKey
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.PropertyComplianceJourneyState
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcExpiryCheckFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafeEngineerNumFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionOtherReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyUploadCertificateFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.MeesExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.TodayOrPastDateFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.FullPropertyComplianceConfirmationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PartialPropertyComplianceConfirmationEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.UploadService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPropertyComplianceData
import java.net.URI

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

    private val propertyId = 123L
    private val gasSafetyCertUploadId = 1L
    private val eicrUploadId = 2L
    private val gasSafetyCertIssueDate = LocalDate(2024, 1, 1)
    private val eicrIssueDate = LocalDate(2024, 2, 1)
    private val engineerNumber = "12345678"
    private val epcCertificateNumber = "0000-0000-0000-0000-0001"
    private val epcExpiryDate = LocalDate(2025, 12, 31)
    private val epcUrl = "https://epc.url/certificate"

    // To test "including only data from relevant steps", populate the state as though the user has added answers for one branch of steps
    // then gone back and changed an answer to take the other branch.
    // E.g. added a gas safety certificate then gone back and added an exemption - the cya should only show the exemption,
    // and the created property compliance record should only include the exemption data, not the certificate data.
    @Test
    fun `afterStepDataIsAdded creates a propertyCompliance record with valid certificates including only data from relevant steps`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyId)
        val propertyCompliance = MockPropertyComplianceData.createPropertyCompliance(propertyOwnership = propertyOwnership)
        val epcData =
            MockEpcData.createEpcDataModel(
                certificateNumber = epcCertificateNumber,
                expiryDate = epcExpiryDate,
                energyRating = "C",
            )

        setupMockState(
            gasSafetyCertUploadId = gasSafetyCertUploadId,
            gasSafetyCertIssueDate = gasSafetyCertIssueDate,
            gasSafetyEngineerNumber = engineerNumber,
            eicrUploadId = eicrUploadId,
            eicrIssueDate = eicrIssueDate,
            acceptedEpc = epcData,
        )

        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyId)).thenReturn(propertyOwnership)
        whenever(mockEpcCertificateUrlProvider.getEpcCertificateUrl(epcCertificateNumber)).thenReturn(epcUrl)
        whenever(
            mockPropertyComplianceService.createPropertyCompliance(
                eq(propertyId),
                eq(gasSafetyCertUploadId),
                eq(gasSafetyCertIssueDate.toJavaLocalDate()),
                eq(engineerNumber),
                anyOrNull(),
                anyOrNull(),
                eq(eicrUploadId),
                eq(eicrIssueDate.toJavaLocalDate()),
                anyOrNull(),
                anyOrNull(),
                eq(epcUrl),
                eq(epcExpiryDate.toJavaLocalDate()),
                anyOrNull(),
                eq("C"),
                anyOrNull(),
                anyOrNull(),
            ),
        ).thenReturn(propertyCompliance)

        setupEmailMocks(propertyOwnership, propertyCompliance, isFullyCompliant = true)

        // Act
        stepConfig.afterStepDataIsAdded(mockState)

        // Assert
        verify(mockPropertyComplianceService).createPropertyCompliance(
            propertyOwnershipId = propertyId,
            gasSafetyCertUploadId = gasSafetyCertUploadId,
            gasSafetyCertIssueDate = gasSafetyCertIssueDate.toJavaLocalDate(),
            gasSafetyCertEngineerNum = engineerNumber,
            gasSafetyCertExemptionReason = null,
            gasSafetyCertExemptionOtherReason = null,
            eicrUploadId = eicrUploadId,
            eicrIssueDate = eicrIssueDate.toJavaLocalDate(),
            eicrExemptionReason = null,
            eicrExemptionOtherReason = null,
            epcUrl = epcUrl,
            epcExpiryDate = epcExpiryDate.toJavaLocalDate(),
            tenancyStartedBeforeEpcExpiry = null,
            epcEnergyRating = "C",
            epcExemptionReason = null,
            epcMeesExemptionReason = null,
        )
        verify(mockPropertyComplianceService).addToPropertiesWithComplianceAddedThisSession(propertyId)
        verify(mockPropertyOwnershipService).deleteIncompleteComplianceForm(propertyId)
    }

    @Test
    fun `afterStepDataIsAdded creates a propertyCompliance record with exemptions including only data from relevant steps`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyId)
        val propertyCompliance =
            MockPropertyComplianceData.createPropertyCompliance(
                propertyOwnership = propertyOwnership,
                gasSafetyCertExemptionReason = GasSafetyExemptionReason.OTHER,
                gasSafetyCertExemptionOtherReason = "Other gas safety reason",
                eicrExemptionReason = EicrExemptionReason.OTHER,
                eicrExemptionOtherReason = "Other eicr reason",
                epcExemptionReason = EpcExemptionReason.DUE_FOR_DEMOLITION,
            )

        setupMockState(
            gasSafetyExemptionReason = GasSafetyExemptionReason.OTHER,
            gasSafetyExemptionOtherReason = "Other gas safety reason",
            eicrExemptionReason = EicrExemptionReason.OTHER,
            eicrExemptionOtherReason = "Other eicr reason",
            epcExemptionReason = EpcExemptionReason.DUE_FOR_DEMOLITION,
        )

        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyId)).thenReturn(propertyOwnership)
        whenever(
            mockPropertyComplianceService.createPropertyCompliance(
                eq(propertyId),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                eq(GasSafetyExemptionReason.OTHER),
                eq("Other gas safety reason"),
                anyOrNull(),
                anyOrNull(),
                eq(EicrExemptionReason.OTHER),
                eq("Other eicr reason"),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                eq(EpcExemptionReason.DUE_FOR_DEMOLITION),
                anyOrNull(),
            ),
        ).thenReturn(propertyCompliance)

        setupEmailMocks(propertyOwnership, propertyCompliance, isFullyCompliant = true)

        // Act
        stepConfig.afterStepDataIsAdded(mockState)

        // Assert
        verify(mockPropertyComplianceService).createPropertyCompliance(
            propertyOwnershipId = propertyId,
            gasSafetyCertUploadId = null,
            gasSafetyCertIssueDate = null,
            gasSafetyCertEngineerNum = null,
            gasSafetyCertExemptionReason = GasSafetyExemptionReason.OTHER,
            gasSafetyCertExemptionOtherReason = "Other gas safety reason",
            eicrUploadId = null,
            eicrIssueDate = null,
            eicrExemptionReason = EicrExemptionReason.OTHER,
            eicrExemptionOtherReason = "Other eicr reason",
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
        val stepConfig = setupStepConfig()
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyId)
        val propertyCompliance =
            MockPropertyComplianceData.createPropertyCompliance(
                propertyOwnership = propertyOwnership,
                epcExpiryDate = epcExpiryDate.toJavaLocalDate(),
                tenancyStartedBeforeEpcExpiry = true,
                epcEnergyRating = "F",
                epcMeesExemptionReason = MeesExemptionReason.HIGH_COST,
            )
        val epcData =
            MockEpcData.createEpcDataModel(
                certificateNumber = epcCertificateNumber,
                expiryDate = epcExpiryDate,
                energyRating = "F",
            )

        setupMockState(
            acceptedEpc = epcData,
            tenancyStartedBeforeEpcExpiry = true,
            meesExemptionReason = MeesExemptionReason.HIGH_COST,
        )

        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyId)).thenReturn(propertyOwnership)
        whenever(mockEpcCertificateUrlProvider.getEpcCertificateUrl(epcCertificateNumber)).thenReturn(epcUrl)
        whenever(
            mockPropertyComplianceService.createPropertyCompliance(
                eq(propertyId),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                eq(epcUrl),
                eq(epcExpiryDate.toJavaLocalDate()),
                eq(true),
                eq("F"),
                anyOrNull(),
                eq(MeesExemptionReason.HIGH_COST),
            ),
        ).thenReturn(propertyCompliance)

        setupEmailMocks(propertyOwnership, propertyCompliance, isFullyCompliant = true)

        // Act
        stepConfig.afterStepDataIsAdded(mockState)

        // Assert
        verify(mockPropertyComplianceService).createPropertyCompliance(
            propertyOwnershipId = propertyId,
            gasSafetyCertUploadId = null,
            gasSafetyCertIssueDate = null,
            gasSafetyCertEngineerNum = null,
            gasSafetyCertExemptionReason = null,
            gasSafetyCertExemptionOtherReason = null,
            eicrUploadId = null,
            eicrIssueDate = null,
            eicrExemptionReason = null,
            eicrExemptionOtherReason = null,
            epcUrl = epcUrl,
            epcExpiryDate = epcExpiryDate.toJavaLocalDate(),
            tenancyStartedBeforeEpcExpiry = true,
            epcEnergyRating = "F",
            epcExemptionReason = null,
            epcMeesExemptionReason = MeesExemptionReason.HIGH_COST,
        )
        verify(mockPropertyComplianceService).addToPropertiesWithComplianceAddedThisSession(propertyId)
        verify(mockPropertyOwnershipService).deleteIncompleteComplianceForm(propertyId)
    }

    @Test
    fun `afterStepDataIsAdded sends a FullPropertyComplianceConfirmationEmail for a fully compliant property`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyId)
        val propertyCompliance = MockPropertyComplianceData.createPropertyCompliance(propertyOwnership = propertyOwnership)
        val epcData = MockEpcData.createEpcDataModel(certificateNumber = epcCertificateNumber, energyRating = "C")

        setupMockState(
            gasSafetyCertUploadId = gasSafetyCertUploadId,
            gasSafetyCertIssueDate = gasSafetyCertIssueDate,
            gasSafetyEngineerNumber = engineerNumber,
            eicrUploadId = eicrUploadId,
            eicrIssueDate = eicrIssueDate,
            acceptedEpc = epcData,
        )

        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyId)).thenReturn(propertyOwnership)
        whenever(mockEpcCertificateUrlProvider.getEpcCertificateUrl(epcCertificateNumber)).thenReturn(epcUrl)
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
        ).thenReturn(propertyCompliance)

        setupEmailMocks(propertyOwnership, propertyCompliance, isFullyCompliant = true)

        // Act
        stepConfig.afterStepDataIsAdded(mockState)

        // Assert
        verify(mockFullPropertyComplianceConfirmationEmailService).sendEmail(
            eq(propertyOwnership.primaryLandlord.email),
            any<FullPropertyComplianceConfirmationEmail>(),
        )
    }

    @Test
    fun `afterStepDataIsAdded sends a PartialPropertyComplianceConfirmationEmail for a property which is not fully compliant`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyId)
        val propertyCompliance =
            MockPropertyComplianceData.createPropertyCompliance(
                propertyOwnership = propertyOwnership,
                gasSafetyCertUpload = null,
                gasSafetyCertIssueDate = null,
                eicrFileUpload = null,
                eicrIssueDate = null,
                epcUrl = null,
            )

        setupMockState()

        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyId)).thenReturn(propertyOwnership)
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
        ).thenReturn(propertyCompliance)

        setupEmailMocks(propertyOwnership, propertyCompliance, isFullyCompliant = false)

        // Act
        stepConfig.afterStepDataIsAdded(mockState)

        // Assert
        verify(mockPartialPropertyComplianceConfirmationEmailService).sendEmail(
            eq(propertyOwnership.primaryLandlord.email),
            any<PartialPropertyComplianceConfirmationEmail>(),
        )
    }

    @Test
    fun `afterStepDataIsAdded adds the propertyId to the session`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyId)
        val propertyCompliance = MockPropertyComplianceData.createPropertyCompliance(propertyOwnership = propertyOwnership)

        setupMockState()

        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyId)).thenReturn(propertyOwnership)
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
        ).thenReturn(propertyCompliance)

        setupEmailMocks(propertyOwnership, propertyCompliance, isFullyCompliant = true)

        // Act
        stepConfig.afterStepDataIsAdded(mockState)

        // Assert
        verify(mockPropertyComplianceService).addToPropertiesWithComplianceAddedThisSession(propertyId)
    }

    @Test
    fun `afterStepDataIsAdded deletes the incomplete property compliance from the database`() {
        // Arrange
        val stepConfig = setupStepConfig()
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyId)
        val propertyCompliance = MockPropertyComplianceData.createPropertyCompliance(propertyOwnership = propertyOwnership)

        setupMockState()

        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyId)).thenReturn(propertyOwnership)
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
        ).thenReturn(propertyCompliance)

        setupEmailMocks(propertyOwnership, propertyCompliance, isFullyCompliant = true)

        // Act
        stepConfig.afterStepDataIsAdded(mockState)

        // Assert
        verify(mockPropertyOwnershipService).deleteIncompleteComplianceForm(propertyId)
    }

    private fun setupStepConfig(): PropertyComplianceCyaStepConfig =
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

    private fun setupMockState(
        gasSafetyCertUploadId: Long? = null,
        gasSafetyCertIssueDate: LocalDate? = null,
        gasSafetyEngineerNumber: String? = null,
        gasSafetyExemptionReason: GasSafetyExemptionReason? = null,
        gasSafetyExemptionOtherReason: String? = null,
        eicrUploadId: Long? = null,
        eicrIssueDate: LocalDate? = null,
        eicrExemptionReason: EicrExemptionReason? = null,
        eicrExemptionOtherReason: String? = null,
        acceptedEpc: EpcDataModel? = null,
        tenancyStartedBeforeEpcExpiry: Boolean? = null,
        epcExemptionReason: EpcExemptionReason? = null,
        meesExemptionReason: MeesExemptionReason? = null,
    ) {
        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockState.acceptedEpc).thenReturn(acceptedEpc)

        // Gas safety certificate
        val mockGasSafetyCertificateUploadStep = mock<GasSafetyCertificateUploadStep>()
        whenever(mockState.gasSafetyCertificateUploadStep).thenReturn(mockGasSafetyCertificateUploadStep)
        whenever(mockGasSafetyCertificateUploadStep.formModelIfReachableOrNull).thenReturn(
            gasSafetyCertUploadId?.let {
                GasSafetyUploadCertificateFormModel().apply {
                    fileUploadId = it
                }
            },
        )

        // Gas safety certificate issue date
        val mockGasSafetyIssueDateStep = mock<GasSafetyIssueDateStep>()
        whenever(mockState.gasSafetyIssueDateStep).thenReturn(mockGasSafetyIssueDateStep)
        whenever(mockGasSafetyIssueDateStep.formModelIfReachableOrNull).thenReturn(
            gasSafetyCertIssueDate?.let {
                TodayOrPastDateFormModel().apply {
                    day = it.dayOfMonth.toString()
                    month = it.monthNumber.toString()
                    year = it.year.toString()
                }
            },
        )

        // Gas safety engineer number
        val mockGasSafetyEngineerNumberStep = mock<GasSafetyEngineerNumberStep>()
        whenever(mockState.gasSafetyEngineerNumberStep).thenReturn(mockGasSafetyEngineerNumberStep)
        whenever(mockGasSafetyEngineerNumberStep.formModelIfReachableOrNull).thenReturn(
            gasSafetyEngineerNumber?.let {
                GasSafeEngineerNumFormModel().apply {
                    engineerNumber = it
                }
            },
        )

        // Gas safety exemption reason
        val mockGasSafetyExemptionReasonStep = mock<GasSafetyExemptionReasonStep>()
        whenever(mockState.gasSafetyExemptionReasonStep).thenReturn(mockGasSafetyExemptionReasonStep)
        whenever(mockGasSafetyExemptionReasonStep.formModelIfReachableOrNull).thenReturn(
            gasSafetyExemptionReason?.let {
                GasSafetyExemptionReasonFormModel().apply {
                    exemptionReason = it
                }
            },
        )

        // Gas safety exemption other reason
        val mockGasSafetyExemptionOtherReasonStep = mock<GasSafetyExemptionOtherReasonStep>()
        whenever(mockState.gasSafetyExemptionOtherReasonStep).thenReturn(mockGasSafetyExemptionOtherReasonStep)
        whenever(mockGasSafetyExemptionOtherReasonStep.formModelIfReachableOrNull).thenReturn(
            gasSafetyExemptionOtherReason?.let {
                GasSafetyExemptionOtherReasonFormModel().apply {
                    otherReason = it
                }
            },
        )

        // EICR upload
        val mockEicrUploadStep = mock<EicrUploadStep>()
        whenever(mockState.eicrUploadStep).thenReturn(mockEicrUploadStep)
        whenever(mockEicrUploadStep.formModelIfReachableOrNull).thenReturn(
            eicrUploadId?.let {
                uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrUploadCertificateFormModel().apply {
                    fileUploadId = it
                }
            },
        )

        // EICR issue date
        val mockEicrIssueDateStep = mock<EicrIssueDateStep>()
        whenever(mockState.eicrIssueDateStep).thenReturn(mockEicrIssueDateStep)
        whenever(mockEicrIssueDateStep.formModelIfReachableOrNull).thenReturn(
            eicrIssueDate?.let {
                TodayOrPastDateFormModel().apply {
                    day = it.dayOfMonth.toString()
                    month = it.monthNumber.toString()
                    year = it.year.toString()
                }
            },
        )

        // EICR exemption reason
        val mockEicrExemptionReasonStep = mock<EicrExemptionReasonStep>()
        whenever(mockState.eicrExemptionReasonStep).thenReturn(mockEicrExemptionReasonStep)
        whenever(mockEicrExemptionReasonStep.formModelIfReachableOrNull).thenReturn(
            eicrExemptionReason?.let {
                EicrExemptionReasonFormModel().apply {
                    exemptionReason = it
                }
            },
        )

        // EICR exemption other reason
        val mockEicrExemptionOtherReasonStep = mock<EicrExemptionOtherReasonStep>()
        whenever(mockState.eicrExemptionOtherReasonStep).thenReturn(mockEicrExemptionOtherReasonStep)
        whenever(mockEicrExemptionOtherReasonStep.formModelIfReachableOrNull).thenReturn(
            eicrExemptionOtherReason?.let {
                uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrExemptionOtherReasonFormModel().apply {
                    otherReason = it
                }
            },
        )

        // EPC expiry check
        val mockEpcExpiryCheckStep = mock<EpcExpiryCheckStep>()
        whenever(mockState.epcExpiryCheckStep).thenReturn(mockEpcExpiryCheckStep)
        whenever(mockEpcExpiryCheckStep.formModelIfReachableOrNull).thenReturn(
            tenancyStartedBeforeEpcExpiry?.let {
                EpcExpiryCheckFormModel().apply {
                    tenancyStartedBeforeExpiry = it
                }
            },
        )

        // EPC exemption reason
        val mockEpcExemptionReasonStep = mock<EpcExemptionReasonStep>()
        whenever(mockState.epcExemptionReasonStep).thenReturn(mockEpcExemptionReasonStep)
        whenever(mockEpcExemptionReasonStep.formModelIfReachableOrNull).thenReturn(
            epcExemptionReason?.let {
                EpcExemptionReasonFormModel().apply {
                    exemptionReason = it
                }
            },
        )

        // MEES exemption reason
        val mockMeesExemptionReasonStep = mock<MeesExemptionReasonStep>()
        whenever(mockState.meesExemptionReasonStep).thenReturn(mockMeesExemptionReasonStep)
        whenever(mockMeesExemptionReasonStep.formModelIfReachableOrNull).thenReturn(
            meesExemptionReason?.let {
                MeesExemptionReasonFormModel().apply {
                    exemptionReason = it
                }
            },
        )
    }

    private fun setupEmailMocks(
        propertyOwnership: uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership,
        propertyCompliance: uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance,
        isFullyCompliant: Boolean,
    ) {
        val compliantMsgKeys =
            listOf(
                "propertyCompliance.confirmation.compliant.bullet.gasSafety",
                "propertyCompliance.confirmation.compliant.bullet.eicr",
                "propertyCompliance.confirmation.compliant.bullet.epc",
                "propertyCompliance.confirmation.compliant.bullet.responsibilities",
            )
        val nonCompliantMsgKeys =
            if (isFullyCompliant) {
                emptyList()
            } else {
                listOf(
                    "propertyCompliance.confirmation.nonCompliant.bullet.gasSafety.missing",
                    "propertyCompliance.confirmation.nonCompliant.bullet.eicr.missing",
                    "propertyCompliance.confirmation.nonCompliant.bullet.epc.missing",
                )
            }

        compliantMsgKeys.forEach { key ->
            whenever(mockMessageSource.getMessageForKey(key)).thenReturn(key)
        }
        nonCompliantMsgKeys.forEach { key ->
            whenever(mockMessageSource.getMessageForKey(key)).thenReturn(key)
        }

        if (isFullyCompliant) {
            whenever(mockAbsoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI("http://dashboard"))
        } else {
            whenever(mockAbsoluteUrlProvider.buildComplianceInformationUri(propertyId)).thenReturn(URI("http://compliance-info"))
        }
    }
}
