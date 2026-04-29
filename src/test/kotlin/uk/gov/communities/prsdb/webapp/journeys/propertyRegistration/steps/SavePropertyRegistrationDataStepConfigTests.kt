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
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.HasElectricalSafetyCertificate
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.JointLandlordsPropertyRegistrationStrategy
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcExemptionFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcInDateAtStartOfTenancyCheckFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LicensingTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.MeesExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OccupancyFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OwnershipTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyTypeFormModel
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.services.PropertyRegistrationService
import uk.gov.communities.prsdb.webapp.testHelpers.JourneyTestHelper.Companion.setMockUser
import kotlin.test.assertNotEquals

@ExtendWith(MockitoExtension::class)
class SavePropertyRegistrationDataStepConfigTests {
    @Mock
    private lateinit var mockPropertyRegistrationService: PropertyRegistrationService

    @Mock
    private lateinit var mockPropertyComplianceService: PropertyComplianceService

    @Mock
    private lateinit var mockEpcCertificateUrlProvider: EpcCertificateUrlProvider

    @Mock
    private lateinit var mockJointLandlordsStrategy: JointLandlordsPropertyRegistrationStrategy

    @Mock
    private lateinit var mockState: PropertyRegistrationJourneyState

    private lateinit var stepConfig: SavePropertyRegistrationDataStepConfig

    @BeforeEach
    fun setUp() {
        stepConfig =
            SavePropertyRegistrationDataStepConfig(
                propertyRegistrationService = mockPropertyRegistrationService,
                propertyComplianceService = mockPropertyComplianceService,
                epcCertificateUrlProvider = mockEpcCertificateUrlProvider,
                jointLandlordsStrategy = mockJointLandlordsStrategy,
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
            electricalCertType = HasElectricalSafetyCertificate.HAS_EICR,
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
            hasGasSupply = eq(true),
            gasSafetyCertIssueDate = eq(gasCertIssueDate.toJavaLocalDate()),
            gasSafetyFileUploadIds = eq(gasUploadIds),
            electricalSafetyFileUploadIds = eq(electricalUploadIds),
            electricalSafetyExpiryDate = eq(electricalSafetyExpiryDate.toJavaLocalDate()),
            electricalCertType = eq(CertificateType.Eicr),
            epcCertificateUrl = eq(epcUrl),
            epcExpiryDate = eq(acceptedEpc.expiryDate.toJavaLocalDate()),
            epcEnergyRating = eq(acceptedEpc.energyRating),
            tenancyStartedBeforeEpcExpiry = eq(true),
            epcExemptionReason = eq(epcExemptionReason),
            epcMeesExemptionReason = eq(meesExemptionReason),
        )
    }

    @Test
    fun `afterStepIsReached sets isAddressAlreadyRegistered when EntityExistsException`() {
        // Arrange
        setupStateForPropertyRegistration()
        setupStateForComplianceData()
        whenever(mockState.gasUploadIds).thenReturn(emptyList())
        whenever(mockState.electricalUploadIds).thenReturn(emptyList())
        whenever(mockState.getElectricalCertificateType()).thenReturn(null)
        whenever(
            mockPropertyRegistrationService.registerProperty(
                addressModel = any(),
                propertyType = any(),
                licenseType = any(),
                licenceNumber = any(),
                ownershipType = any(),
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
                hasGasSupply = anyOrNull(),
                gasSafetyCertIssueDate = anyOrNull(),
                gasSafetyFileUploadIds = any(),
                electricalSafetyFileUploadIds = any(),
                electricalSafetyExpiryDate = anyOrNull(),
                electricalCertType = anyOrNull(),
                epcCertificateUrl = anyOrNull(),
                epcExpiryDate = anyOrNull(),
                epcEnergyRating = anyOrNull(),
                tenancyStartedBeforeEpcExpiry = anyOrNull(),
                epcExemptionReason = anyOrNull(),
                epcMeesExemptionReason = anyOrNull(),
            ),
        ).thenThrow(EntityExistsException("Address already registered"))

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockState).isAddressAlreadyRegistered = true
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
            hasGasSupply = anyOrNull(),
            gasSafetyCertIssueDate = isNull(),
            gasSafetyFileUploadIds = eq(emptyList()),
            electricalSafetyFileUploadIds = eq(emptyList()),
            electricalSafetyExpiryDate = isNull(),
            electricalCertType = isNull(),
            epcCertificateUrl = isNull(),
            epcExpiryDate = isNull(),
            epcEnergyRating = isNull(),
            tenancyStartedBeforeEpcExpiry = isNull(),
            epcExemptionReason = isNull(),
            epcMeesExemptionReason = isNull(),
        )
    }

    @Test
    fun `resolveNextDestination deletes journey and returns default destination when address is not already registered`() {
        // Arrange
        val defaultDestination = Destination.ExternalUrl("redirect")
        whenever(mockState.isAddressAlreadyRegistered).thenReturn(false)

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
        whenever(mockState.isAddressAlreadyRegistered).thenReturn(true)
        whenever(mockState.alreadyRegisteredStep).thenReturn(mockAlreadyRegisteredStep)

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

        whenever(mockState.getBillsIncludedOrNull()).thenReturn(null)

        whenever(mockState.getAddress()).thenReturn(
            AddressDataModel(singleLineAddress = "1 Test St", uprn = 12345L, localCouncilId = 1),
        )

        val mockPropertyTypeStep = mock<PropertyTypeStep>()
        val propertyTypeFormModel = PropertyTypeFormModel().apply { propertyType = PropertyType.DETACHED_HOUSE }
        whenever(mockState.propertyTypeStep).thenReturn(mockPropertyTypeStep)
        whenever(mockPropertyTypeStep.formModel).thenReturn(propertyTypeFormModel)

        val mockLicensingTypeStep = mock<LicensingTypeStep>()
        val licensingTypeFormModel = LicensingTypeFormModel().apply { licensingType = LicensingType.SELECTIVE_LICENCE }
        whenever(mockState.licensingTypeStep).thenReturn(mockLicensingTypeStep)
        whenever(mockLicensingTypeStep.formModel).thenReturn(licensingTypeFormModel)

        whenever(mockState.getLicenceNumberOrNull()).thenReturn(null)

        val mockOwnershipTypeStep = mock<OwnershipTypeStep>()
        val ownershipTypeFormModel = OwnershipTypeFormModel().apply { ownershipType = OwnershipType.FREEHOLD }
        whenever(mockState.ownershipTypeStep).thenReturn(mockOwnershipTypeStep)
        whenever(mockOwnershipTypeStep.formModel).thenReturn(ownershipTypeFormModel)
    }

    private fun setupStateForComplianceData(
        gasUploadIds: List<Long> = emptyList(),
        gasCertIssueDate: LocalDate? = null,
        electricalUploadIds: List<Long> = emptyList(),
        electricalCertExpiryDate: LocalDate? = null,
        electricalCertType: HasElectricalSafetyCertificate? = null,
        acceptedEpc: EpcDataModel? = null,
        epcUrl: String? = null,
        tenancyStartedBeforeEpcExpiry: Boolean? = null,
        epcExemptionReason: EpcExemptionReason = EpcExemptionReason.PROTECTED_ARCHITECTURAL_OR_HISTORICAL_MERIT,
        meesExemptionReason: MeesExemptionReason = MeesExemptionReason.HIGH_COST,
    ) {
        whenever(mockState.gasUploadIds).thenReturn(gasUploadIds)
        whenever(mockState.electricalUploadIds).thenReturn(electricalUploadIds)
        whenever(mockState.getElectricalCertificateType()).thenReturn(electricalCertType)

        val mockHasGasSupplyStep = mock<HasGasSupplyStep>()
        whenever(mockState.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)
        whenever(mockHasGasSupplyStep.outcome).thenReturn(YesOrNo.YES)

        whenever(mockState.getGasSafetyCertificateIssueDateIfReachable()).thenReturn(gasCertIssueDate)
        whenever(mockState.getElectricalCertificateExpiryDateIfReachable()).thenReturn(electricalCertExpiryDate)

        if (acceptedEpc != null) {
            whenever(mockEpcCertificateUrlProvider.getEpcCertificateUrl(acceptedEpc.certificateNumber)).thenReturn(epcUrl)
        }

        whenever(mockState.acceptedEpcIfReachable).thenReturn(acceptedEpc)

        val mockTenancyStep = mock<EpcInDateAtStartOfTenancyCheckStep>()
        val mockEpcExemptionStep = mock<EpcExemptionStep>()
        val mockMeesExemptionStep = mock<MeesExemptionStep>()
        whenever(mockState.epcInDateAtStartOfTenancyCheckStep).thenReturn(mockTenancyStep)
        whenever(mockTenancyStep.formModelIfReachableOrNull).thenReturn(
            EpcInDateAtStartOfTenancyCheckFormModel().apply {
                tenancyStartedBeforeExpiry = tenancyStartedBeforeEpcExpiry
            },
        )
        whenever(mockState.epcExemptionStep).thenReturn(mockEpcExemptionStep)
        whenever(mockEpcExemptionStep.formModelIfReachableOrNull).thenReturn(
            EpcExemptionFormModel().apply {
                exemptionReason = epcExemptionReason
            },
        )
        whenever(mockState.meesExemptionStep).thenReturn(mockMeesExemptionStep)
        whenever(mockMeesExemptionStep.formModelIfReachableOrNull).thenReturn(
            MeesExemptionReasonFormModel().apply {
                exemptionReason = meesExemptionReason
            },
        )
    }

    private fun setupStateForComplianceDataWithNullValues() {
        whenever(mockState.gasUploadIds).thenReturn(emptyList())
        whenever(mockState.electricalUploadIds).thenReturn(emptyList())
        whenever(mockState.getElectricalCertificateType()).thenReturn(null)

        val mockHasGasSupplyStep = mock<HasGasSupplyStep>()
        whenever(mockState.hasGasSupplyStep).thenReturn(mockHasGasSupplyStep)
        whenever(mockHasGasSupplyStep.outcome).thenReturn(YesOrNo.YES)

        whenever(mockState.getGasSafetyCertificateIssueDateIfReachable()).thenReturn(null)
        whenever(mockState.getElectricalCertificateExpiryDateIfReachable()).thenReturn(null)
        whenever(mockState.acceptedEpcIfReachable).thenReturn(null)

        val mockTenancyStep = mock<EpcInDateAtStartOfTenancyCheckStep>()
        val mockEpcExemptionStep = mock<EpcExemptionStep>()
        val mockMeesExemptionStep = mock<MeesExemptionStep>()
        whenever(mockState.epcInDateAtStartOfTenancyCheckStep).thenReturn(mockTenancyStep)
        whenever(mockTenancyStep.formModelIfReachableOrNull).thenReturn(null)
        whenever(mockState.epcExemptionStep).thenReturn(mockEpcExemptionStep)
        whenever(mockEpcExemptionStep.formModelIfReachableOrNull).thenReturn(null)
        whenever(mockState.meesExemptionStep).thenReturn(mockMeesExemptionStep)
        whenever(mockMeesExemptionStep.formModelIfReachableOrNull).thenReturn(null)
    }
}
