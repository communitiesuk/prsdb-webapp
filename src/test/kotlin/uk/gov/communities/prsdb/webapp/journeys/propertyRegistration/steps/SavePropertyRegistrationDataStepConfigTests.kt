package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import jakarta.persistence.EntityExistsException
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.ElectricalSafetyDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.ElectricalSafetyTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.EpcDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.EpcTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.GasSafetyDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.GasSafetyTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.JointLandlordsPropertyRegistrationTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.LicensingTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.OwnershipAndLandlordsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.PropertyDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.PropertyRegistrationAddressTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.RentIncludesBillsTask
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcExemptionFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcInDateAtStartOfTenancyCheckFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HasJointLandlordsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.MeesExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OccupancyFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OwnershipTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyTypeFormModel
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider
import uk.gov.communities.prsdb.webapp.services.PropertyRegistrationService
import uk.gov.communities.prsdb.webapp.testHelpers.JourneyTestHelper.Companion.setMockUser
import kotlin.test.assertNotEquals

@ExtendWith(MockitoExtension::class)
class SavePropertyRegistrationDataStepConfigTests {
    @Mock
    private lateinit var mockPropertyRegistrationService: PropertyRegistrationService

    @Mock
    private lateinit var mockEpcCertificateUrlProvider: EpcCertificateUrlProvider

    @Mock
    private lateinit var mockFeatureFlagManager: FeatureFlagManager

    @Mock
    private lateinit var mockState: PropertyRegistrationJourneyState

    @Mock
    private lateinit var mockAddressTask: PropertyRegistrationAddressTask

    @Mock
    private lateinit var mockPropertyDetailsTask: PropertyDetailsTask

    @Mock
    private lateinit var mockOwnershipAndLandlordsTask: OwnershipAndLandlordsTask

    private lateinit var stepConfig: SavePropertyRegistrationDataStepConfig

    @BeforeEach
    fun setUp() {
        stepConfig =
            SavePropertyRegistrationDataStepConfig(
                propertyRegistrationService = mockPropertyRegistrationService,
                epcCertificateUrlProvider = mockEpcCertificateUrlProvider,
                featureFlagManager = mockFeatureFlagManager,
            )
    }

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `mode returns COMPLETE`() {
        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertEquals(Complete.COMPLETE, result)
    }

    @Test
    fun `afterStepIsReached registers property and saves compliance data with all compliance fields from state`() {
        // Arrange
        val gasUploadIds = listOf(10L, 20L)
        val gasCertIssueDate = LocalDate(2024, 6, 15)

        val electricalUploadIds = listOf(30L)
        val electricalSafetyExpiryDate = LocalDate(2029, 3, 20)

        val certificateNumber = "1234-5678-9012-3456-7890"
        val epcUrl = "https://epc.example.com/$certificateNumber"
        val acceptedEpc =
            EpcDataModel(
                certificateNumber = certificateNumber,
                singleLineAddress = "1 Test St",
                energyRating = "B",
                expiryDate = LocalDate(2030, 1, 1),
            )
        val epcExemptionReason = EpcExemptionReason.PROTECTED_ARCHITECTURAL_OR_HISTORICAL_MERIT
        val meesExemptionReason = MeesExemptionReason.HIGH_COST

        setupStateForPropertyRegistration()
        setupStateForComplianceData(
            gasUploadIds = gasUploadIds,
            gasCertIssueDate = gasCertIssueDate,
            electricalUploadIds = electricalUploadIds,
            electricalCertExpiryDate = electricalSafetyExpiryDate,
            electricalCertType = CertificateType.Eicr,
            acceptedEpc = acceptedEpc,
            epcUrl = epcUrl,
            tenancyStartedBeforeEpcExpiry = true,
            epcExemptionReason = epcExemptionReason,
            meesExemptionReason = meesExemptionReason,
        )

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockPropertyRegistrationService).registerProperty(
            addressModel = any(),
            propertyType = any(),
            licenseType = any(),
            licenceNumber = any(),
            ownershipType = any(),
            isOccupied = any(),
            numberOfHouseholds = any(),
            numberOfPeople = any(),
            baseUserId = any(),
            numBedrooms = anyOrNull(),
            billsIncludedList = anyOrNull(),
            customBillsIncluded = anyOrNull(),
            furnishedStatus = anyOrNull(),
            rentFrequency = anyOrNull(),
            customRentFrequency = anyOrNull(),
            rentAmount = anyOrNull(),
            customPropertyType = anyOrNull(),
            jointLandlordEmails = anyOrNull(),
            markedJointLandlord = any(),
            hasGasSupply = eq(true),
            gasSafetyCertIssueDate = eq(gasCertIssueDate.toJavaLocalDate()),
            gasSafetyFileUploadIds = eq(gasUploadIds),
            gasSafetyCertProvideLater = eq(false),
            electricalSafetyFileUploadIds = eq(electricalUploadIds),
            electricalSafetyExpiryDate = eq(electricalSafetyExpiryDate.toJavaLocalDate()),
            electricalCertType = eq(CertificateType.Eicr),
            electricalSafetyCertProvideLater = eq(false),
            epcCertificateUrl = eq(epcUrl),
            epcExpiryDate = eq(acceptedEpc.expiryDate.toJavaLocalDate()),
            epcEnergyRating = eq(acceptedEpc.energyRating),
            tenancyStartedBeforeEpcExpiry = eq(true),
            epcExemptionReason = eq(epcExemptionReason),
            epcMeesExemptionReason = eq(meesExemptionReason),
            epcProvideLater = eq(false),
            licenseProvideLater = eq(false),
        )
    }

    @Test
    fun `afterStepIsReached passes licenseProvideLater as true when the user provides licensing later`() {
        // Arrange
        setupStateForPropertyRegistration()
        setupStateForComplianceDataWithNullValues()
        whenever(mockState.licensingTask.licensingTypeStep.outcome).thenReturn(LicensingTypeMode.PROVIDE_LATER)
        whenever(mockState.licensingTask.getLicensingType()).thenReturn(LicensingType.PROVIDE_LATER)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockPropertyRegistrationService).registerProperty(
            addressModel = any(),
            propertyType = any(),
            licenseType = anyOrNull(),
            licenceNumber = any(),
            ownershipType = any(),
            isOccupied = any(),
            numberOfHouseholds = any(),
            numberOfPeople = any(),
            baseUserId = any(),
            numBedrooms = anyOrNull(),
            billsIncludedList = anyOrNull(),
            customBillsIncluded = anyOrNull(),
            furnishedStatus = anyOrNull(),
            rentFrequency = anyOrNull(),
            customRentFrequency = anyOrNull(),
            rentAmount = anyOrNull(),
            customPropertyType = anyOrNull(),
            jointLandlordEmails = anyOrNull(),
            markedJointLandlord = any(),
            hasGasSupply = anyOrNull(),
            gasSafetyCertIssueDate = anyOrNull(),
            gasSafetyFileUploadIds = any(),
            gasSafetyCertProvideLater = anyOrNull(),
            electricalSafetyFileUploadIds = any(),
            electricalSafetyExpiryDate = anyOrNull(),
            electricalCertType = anyOrNull(),
            electricalSafetyCertProvideLater = anyOrNull(),
            epcCertificateUrl = anyOrNull(),
            epcExpiryDate = anyOrNull(),
            epcEnergyRating = anyOrNull(),
            tenancyStartedBeforeEpcExpiry = anyOrNull(),
            epcExemptionReason = anyOrNull(),
            epcMeesExemptionReason = anyOrNull(),
            epcProvideLater = anyOrNull(),
            licenseProvideLater = eq(true),
        )
    }

    @Test
    fun `afterStepIsReached sets isAddressAlreadyRegistered when EntityExistsException`() {
        // Arrange
        setupStateForPropertyRegistration()
        setupStateForComplianceData()
        whenever(mockState.gasSafetyTask.gasSafetyDetailsTask.gasUploadIds).thenReturn(emptyList())
        whenever(mockState.electricalSafetyTask.electricalSafetyDetailsTask.electricalUploadIds).thenReturn(emptyList())
        whenever(
            mockState.electricalSafetyTask.electricalSafetyDetailsTask.mapElectricalCertificateTypeToGlobalCertificateType(),
        ).thenReturn(null)
        whenever(
            mockPropertyRegistrationService.registerProperty(
                addressModel = any(),
                propertyType = any(),
                licenseType = any(),
                licenceNumber = any(),
                ownershipType = any(),
                isOccupied = any(),
                numberOfHouseholds = any(),
                numberOfPeople = any(),
                baseUserId = any(),
                numBedrooms = anyOrNull(),
                billsIncludedList = anyOrNull(),
                customBillsIncluded = anyOrNull(),
                furnishedStatus = anyOrNull(),
                rentFrequency = anyOrNull(),
                customRentFrequency = anyOrNull(),
                rentAmount = anyOrNull(),
                customPropertyType = anyOrNull(),
                jointLandlordEmails = anyOrNull(),
                markedJointLandlord = any(),
                hasGasSupply = anyOrNull(),
                gasSafetyCertIssueDate = anyOrNull(),
                gasSafetyFileUploadIds = any(),
                gasSafetyCertProvideLater = anyOrNull(),
                electricalSafetyFileUploadIds = any(),
                electricalSafetyExpiryDate = anyOrNull(),
                electricalCertType = anyOrNull(),
                electricalSafetyCertProvideLater = anyOrNull(),
                epcCertificateUrl = anyOrNull(),
                epcExpiryDate = anyOrNull(),
                epcEnergyRating = anyOrNull(),
                tenancyStartedBeforeEpcExpiry = anyOrNull(),
                epcExemptionReason = anyOrNull(),
                epcMeesExemptionReason = anyOrNull(),
                epcProvideLater = anyOrNull(),
                licenseProvideLater = anyOrNull(),
            ),
        ).thenThrow(EntityExistsException("Address already registered"))

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockAddressTask).isAddressAlreadyRegistered = true
    }

    @Test
    fun `afterStepIsReached passes nulls and empties when all compliance steps return no data`() {
        // Arrange
        val registrationNumberValue = 12345L

        setupStateForPropertyRegistration()
        setupStateForComplianceDataWithNullValues()

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockPropertyRegistrationService).registerProperty(
            addressModel = any(),
            propertyType = any(),
            licenseType = any(),
            licenceNumber = any(),
            ownershipType = any(),
            isOccupied = any(),
            numberOfHouseholds = any(),
            numberOfPeople = any(),
            baseUserId = any(),
            numBedrooms = anyOrNull(),
            billsIncludedList = anyOrNull(),
            customBillsIncluded = anyOrNull(),
            furnishedStatus = anyOrNull(),
            rentFrequency = anyOrNull(),
            customRentFrequency = anyOrNull(),
            rentAmount = anyOrNull(),
            customPropertyType = anyOrNull(),
            jointLandlordEmails = anyOrNull(),
            markedJointLandlord = any(),
            hasGasSupply = anyOrNull(),
            gasSafetyCertIssueDate = isNull(),
            gasSafetyFileUploadIds = eq(emptyList()),
            gasSafetyCertProvideLater = anyOrNull(),
            electricalSafetyFileUploadIds = eq(emptyList()),
            electricalSafetyExpiryDate = isNull(),
            electricalCertType = isNull(),
            electricalSafetyCertProvideLater = anyOrNull(),
            epcCertificateUrl = isNull(),
            epcExpiryDate = isNull(),
            epcEnergyRating = isNull(),
            tenancyStartedBeforeEpcExpiry = isNull(),
            epcExemptionReason = isNull(),
            epcMeesExemptionReason = isNull(),
            epcProvideLater = anyOrNull(),
            licenseProvideLater = anyOrNull(),
        )
    }

    @Test
    fun `resolveNextDestination deletes journey and returns default destination when address is not already registered`() {
        // Arrange
        val defaultDestination = Destination.ExternalUrl("redirect")
        whenever(mockState.propertyDetailsTask).thenReturn(mockPropertyDetailsTask)
        whenever(mockPropertyDetailsTask.addressTask).thenReturn(mockAddressTask)
        whenever(mockAddressTask.isAddressAlreadyRegistered).thenReturn(false)

        // Act
        val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

        // Assert
        verify(mockState).deleteJourney()
        assertEquals(defaultDestination, result)
    }

    @Test
    fun `resolveNextDestination redirects to already registered step when address is already registered`() {
        // Arrange
        val defaultDestination = Destination.ExternalUrl("redirect")
        val mockAlreadyRegisteredStep = mock<AlreadyRegisteredStep>()
        whenever(mockAlreadyRegisteredStep.currentJourneyId).thenReturn("test-journey-id")
        whenever(mockState.propertyDetailsTask).thenReturn(mockPropertyDetailsTask)
        whenever(mockPropertyDetailsTask.addressTask).thenReturn(mockAddressTask)
        whenever(mockAddressTask.isAddressAlreadyRegistered).thenReturn(true)
        whenever(mockAddressTask.alreadyRegisteredStep).thenReturn(mockAlreadyRegisteredStep)

        // Act
        val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

        // Assert
        verify(mockState, never()).deleteJourney()
        assertNotEquals(defaultDestination, result)
    }

    private fun setupStateForPropertyRegistration() {
        setMockUser("test-user")

        val mockOccupiedStep = mock<OccupiedStep>()
        val occupancyFormModel = OccupancyFormModel().apply { occupied = false }
        whenever(mockState.occupied).thenReturn(mockOccupiedStep)
        whenever(mockOccupiedStep.formModel).thenReturn(occupancyFormModel)

        val mockRentIncludesBillsTask = mock<RentIncludesBillsTask>()
        whenever(mockState.rentIncludesBillsTask).thenReturn(mockRentIncludesBillsTask)
        whenever(mockRentIncludesBillsTask.getBillsIncludedOrNull()).thenReturn(null)

        whenever(mockState.propertyDetailsTask).thenReturn(mockPropertyDetailsTask)
        whenever(mockPropertyDetailsTask.addressTask).thenReturn(mockAddressTask)
        whenever(mockAddressTask.getAddress()).thenReturn(
            AddressDataModel(singleLineAddress = "1 Test St", uprn = 12345L, localCouncilId = 1),
        )

        val mockPropertyTypeStep = mock<PropertyTypeStep>()
        val propertyTypeFormModel = PropertyTypeFormModel().apply { propertyType = PropertyType.DETACHED_HOUSE }
        whenever(mockPropertyDetailsTask.propertyTypeStep).thenReturn(mockPropertyTypeStep)
        whenever(mockPropertyTypeStep.formModel).thenReturn(propertyTypeFormModel)

        val mockLicensingTypeStep = mock<LicensingTypeStep>()
        val mockLicensingTask = mock<LicensingTask>()
        whenever(mockState.licensingTask).thenReturn(mockLicensingTask)
        whenever(mockLicensingTask.licensingTypeStep).thenReturn(mockLicensingTypeStep)
        whenever(mockLicensingTypeStep.outcome).thenReturn(LicensingTypeMode.SELECTIVE_LICENCE)

        whenever(mockLicensingTask.getLicenceNumberOrNull()).thenReturn(null)
        whenever(mockLicensingTask.getLicensingType()).thenReturn(LicensingType.SELECTIVE_LICENCE)

        val mockOwnershipTypeStep = mock<OwnershipTypeStep>()
        val ownershipTypeFormModel = OwnershipTypeFormModel().apply { ownershipType = OwnershipType.FREEHOLD }
        whenever(mockState.ownershipAndLandlordsTask).thenReturn(mockOwnershipAndLandlordsTask)
        whenever(mockOwnershipAndLandlordsTask.ownershipTypeStep).thenReturn(mockOwnershipTypeStep)
        whenever(mockOwnershipTypeStep.formModel).thenReturn(ownershipTypeFormModel)

        val mockJointLandlordsTask = mock<JointLandlordsPropertyRegistrationTask>()
        whenever(mockOwnershipAndLandlordsTask.jointLandlordsTask).thenReturn(mockJointLandlordsTask)

        val mockHasJointLandlordsStep = mock<HasJointLandlordsStep>()
        val hasJointLandlordsFormModel = HasJointLandlordsFormModel().apply { hasJointLandlords = false }
        whenever(mockJointLandlordsTask.hasJointLandlordsStep).thenReturn(mockHasJointLandlordsStep)
        whenever(mockHasJointLandlordsStep.formModel).thenReturn(hasJointLandlordsFormModel)
        whenever(mockJointLandlordsTask.inviteJointLandlordsTask).thenReturn(mock())
    }

    private fun setupStateForComplianceData(
        gasUploadIds: List<Long> = emptyList(),
        gasCertIssueDate: LocalDate? = null,
        electricalUploadIds: List<Long> = emptyList(),
        electricalCertExpiryDate: LocalDate? = null,
        electricalCertType: CertificateType? = null,
        acceptedEpc: EpcDataModel? = null,
        epcUrl: String? = null,
        tenancyStartedBeforeEpcExpiry: Boolean? = null,
        epcExemptionReason: EpcExemptionReason = EpcExemptionReason.PROTECTED_ARCHITECTURAL_OR_HISTORICAL_MERIT,
        meesExemptionReason: MeesExemptionReason = MeesExemptionReason.HIGH_COST,
    ) {
        val gasSafetyTask: GasSafetyDetailsTask = mock()
        val electricalSafetyDetailsTask: ElectricalSafetyDetailsTask = mock()

        whenever(gasSafetyTask.gasUploadIds).thenReturn(gasUploadIds)
        whenever(electricalSafetyDetailsTask.electricalUploadIds).thenReturn(electricalUploadIds)
        whenever(electricalSafetyDetailsTask.mapElectricalCertificateTypeToGlobalCertificateType()).thenReturn(electricalCertType)

        val mockHasGasSupplyStep = mock<HasGasSupplyStep>()
        whenever(gasSafetyTask.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)
        whenever(mockHasGasSupplyStep.outcome).thenReturn(YesOrNo.YES)

        val mockHasGasCertStep = mock<HasGasCertStep>()
        whenever(gasSafetyTask.hasGasCertStep).thenReturn(mockHasGasCertStep)
        whenever(mockHasGasCertStep.outcome).thenReturn(HasGasCertMode.HAS_CERTIFICATE)

        val mockHasElectricalCertStep = mock<HasElectricalCertStep>()
        whenever(electricalSafetyDetailsTask.hasElectricalCertStep).thenReturn(mockHasElectricalCertStep)
        whenever(mockHasElectricalCertStep.outcome).thenReturn(HasElectricalCertMode.HAS_EIC)

        val mockHasEpcStep = mock<HasEpcStep>()
        val mockEpcTask: EpcTask = mock()
        val mockEpcDetailsTask: EpcDetailsTask = mock()
        whenever(mockEpcTask.epcDetailsTask).thenReturn(mockEpcDetailsTask)
        whenever(mockEpcDetailsTask.hasEpcStep).thenReturn(mockHasEpcStep)
        whenever(mockHasEpcStep.outcome).thenReturn(HasEpcMode.HAS_EPC)

        whenever(gasSafetyTask.getGasSafetyCertificateIssueDateIfReachable()).thenReturn(gasCertIssueDate)
        whenever(electricalSafetyDetailsTask.getElectricalCertificateExpiryDateIfReachable()).thenReturn(electricalCertExpiryDate)

        if (acceptedEpc != null) {
            whenever(mockEpcCertificateUrlProvider.getEpcCertificateUrl(acceptedEpc.certificateNumber)).thenReturn(epcUrl)
        }

        whenever(mockEpcDetailsTask.acceptedEpcIfStillAccepted).thenReturn(acceptedEpc)

        val mockTenancyStep = mock<EpcInDateAtStartOfTenancyCheckStep>()
        val mockEpcExemptionStep = mock<EpcExemptionStep>()
        val mockMeesExemptionStep = mock<MeesExemptionStep>()
        whenever(mockEpcDetailsTask.epcInDateAtStartOfTenancyCheckStep).thenReturn(mockTenancyStep)
        whenever(mockTenancyStep.formModelIfReachableOrNull).thenReturn(
            EpcInDateAtStartOfTenancyCheckFormModel().apply {
                tenancyStartedBeforeExpiry = tenancyStartedBeforeEpcExpiry
            },
        )
        whenever(mockEpcDetailsTask.epcExemptionStep).thenReturn(mockEpcExemptionStep)
        whenever(mockEpcExemptionStep.formModelIfReachableOrNull).thenReturn(
            EpcExemptionFormModel().apply {
                exemptionReason = epcExemptionReason
            },
        )
        whenever(mockEpcDetailsTask.meesExemptionStep).thenReturn(mockMeesExemptionStep)
        whenever(mockMeesExemptionStep.formModelIfReachableOrNull).thenReturn(
            MeesExemptionReasonFormModel().apply {
                exemptionReason = meesExemptionReason
            },
        )

        val mockGasTask: GasSafetyTask = mock()
        whenever(mockGasTask.gasSafetyDetailsTask).thenReturn(gasSafetyTask)
        whenever(mockState.gasSafetyTask).thenReturn(mockGasTask)
        val mockElectricalSafetyTask: ElectricalSafetyTask = mock()
        whenever(mockElectricalSafetyTask.electricalSafetyDetailsTask).thenReturn(electricalSafetyDetailsTask)
        whenever(mockState.electricalSafetyTask).thenReturn(mockElectricalSafetyTask)
        whenever(mockState.epcTask).thenReturn(mockEpcTask)
    }

    private fun setupStateForComplianceDataWithNullValues() {
        val gasSafetyDetailsTask: GasSafetyDetailsTask = mock()
        val electricalSafetyDetailsTask: ElectricalSafetyDetailsTask = mock()
        val mockEpcTask: EpcTask = mock()
        val mockEpcDetailsTask: EpcDetailsTask = mock()
        whenever(mockEpcTask.epcDetailsTask).thenReturn(mockEpcDetailsTask)

        whenever(gasSafetyDetailsTask.gasUploadIds).thenReturn(emptyList())
        whenever(electricalSafetyDetailsTask.electricalUploadIds).thenReturn(emptyList())
        whenever(electricalSafetyDetailsTask.mapElectricalCertificateTypeToGlobalCertificateType()).thenReturn(null)

        val mockHasGasSupplyStep = mock<HasGasSupplyStep>()
        whenever(gasSafetyDetailsTask.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)
        whenever(mockHasGasSupplyStep.outcome).thenReturn(YesOrNo.YES)

        val mockHasGasCertStep = mock<HasGasCertStep>()
        whenever(gasSafetyDetailsTask.hasGasCertStep).thenReturn(mockHasGasCertStep)
        whenever(mockHasGasCertStep.outcome).thenReturn(null)

        val mockHasElectricalCertStep = mock<HasElectricalCertStep>()
        whenever(electricalSafetyDetailsTask.hasElectricalCertStep).thenReturn(mockHasElectricalCertStep)
        whenever(mockHasElectricalCertStep.outcome).thenReturn(null)

        val mockHasEpcStep = mock<HasEpcStep>()
        whenever(mockEpcDetailsTask.hasEpcStep).thenReturn(mockHasEpcStep)
        whenever(mockHasEpcStep.outcome).thenReturn(null)

        whenever(gasSafetyDetailsTask.getGasSafetyCertificateIssueDateIfReachable()).thenReturn(null)
        whenever(electricalSafetyDetailsTask.getElectricalCertificateExpiryDateIfReachable()).thenReturn(null)
        whenever(mockEpcDetailsTask.acceptedEpcIfStillAccepted).thenReturn(null)

        val mockGasTask: GasSafetyTask = mock()
        whenever(mockGasTask.gasSafetyDetailsTask).thenReturn(gasSafetyDetailsTask)
        whenever(mockState.gasSafetyTask).thenReturn(mockGasTask)
        val mockElectricalSafetyTask: ElectricalSafetyTask = mock()
        whenever(mockElectricalSafetyTask.electricalSafetyDetailsTask).thenReturn(electricalSafetyDetailsTask)
        whenever(mockState.electricalSafetyTask).thenReturn(mockElectricalSafetyTask)
        whenever(mockState.epcTask).thenReturn(mockEpcTask)

        val mockTenancyStep = mock<EpcInDateAtStartOfTenancyCheckStep>()
        val mockEpcExemptionStep = mock<EpcExemptionStep>()
        val mockMeesExemptionStep = mock<MeesExemptionStep>()
        whenever(mockEpcDetailsTask.epcInDateAtStartOfTenancyCheckStep).thenReturn(mockTenancyStep)
        whenever(mockTenancyStep.formModelIfReachableOrNull).thenReturn(null)
        whenever(mockEpcDetailsTask.epcExemptionStep).thenReturn(mockEpcExemptionStep)
        whenever(mockEpcExemptionStep.formModelIfReachableOrNull).thenReturn(null)
        whenever(mockEpcDetailsTask.meesExemptionStep).thenReturn(mockMeesExemptionStep)
        whenever(mockMeesExemptionStep.formModelIfReachableOrNull).thenReturn(null)
    }
}
