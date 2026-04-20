package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import jakarta.persistence.EntityExistsException
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.database.entity.RegistrationNumber
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.JointLandlordsPropertyRegistrationStrategy
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
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

@ExtendWith(MockitoExtension::class)
class SaveComplianceDataStepConfigTests {
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

    private lateinit var stepConfig: SaveComplianceDataStepConfig

    @BeforeEach
    fun setUp() {
        stepConfig =
            SaveComplianceDataStepConfig(
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
    fun `afterStepIsReached registers property and saves compliance data`() {
        // Arrange
        val registrationNumberValue = 12345L
        setupStateForPropertyRegistration()
        setupMockRegistrationService(registrationNumberValue)
        setupStateForComplianceData()

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockPropertyRegistrationService).registerProperty(
            addressModel = any(),
            propertyType = any(),
            customPropertyType = anyOrNull(),
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
            jointLandlordEmails = anyOrNull(),
            gasSafetyFileUploadIds = any(),
            electricalSafetyFileUploadIds = any(),
        )
        verify(mockPropertyComplianceService).saveRegistrationComplianceData(
            registrationNumberValue = registrationNumberValue,
        )
    }

    @Test
    fun `afterStepIsReached sets isAddressAlreadyRegistered and skips compliance save when EntityExistsException`() {
        // Arrange
        setupStateForPropertyRegistration()
        whenever(
            mockPropertyRegistrationService.registerProperty(
                addressModel = any(),
                propertyType = any(),
                customPropertyType = anyOrNull(),
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
                jointLandlordEmails = anyOrNull(),
                gasSafetyFileUploadIds = any(),
                electricalSafetyFileUploadIds = any(),
            ),
        ).thenThrow(EntityExistsException("Address already registered"))

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockState).isAddressAlreadyRegistered = true
        verify(mockPropertyComplianceService, never()).saveRegistrationComplianceData(
            registrationNumberValue = any(),
            gasSafetyCertIssueDate = anyOrNull(),
            eicrExpiryDate = anyOrNull(),
            epcCertificateUrl = anyOrNull(),
            epcExpiryDate = anyOrNull(),
            epcEnergyRating = anyOrNull(),
            tenancyStartedBeforeEpcExpiry = anyOrNull(),
            epcExemptionReason = anyOrNull(),
            epcMeesExemptionReason = anyOrNull(),
        )
    }

    @Test
    fun `afterStepIsReached saves compliance data with all fields from state`() {
        // Arrange
        val registrationNumberValue = 12345L
        val gasCertIssueDate = LocalDate(2024, 6, 15)
        val eicrExpiryDate = LocalDate(2029, 3, 20)
        val certificateNumber = "1234-5678-9012-3456-7890"
        val epcUrl = "https://epc.example.com/$certificateNumber"

        val epcDataModel =
            EpcDataModel(
                certificateNumber = certificateNumber,
                singleLineAddress = "1 Test St",
                energyRating = "B",
                expiryDate = LocalDate(2030, 1, 1),
            )

        val tenancyFormModel =
            EpcInDateAtStartOfTenancyCheckFormModel().apply {
                tenancyStartedBeforeExpiry = true
            }
        val epcExemptionFormModel =
            EpcExemptionFormModel().apply {
                exemptionReason = EpcExemptionReason.PROTECTED_ARCHITECTURAL_OR_HISTORICAL_MERIT
            }
        val meesExemptionFormModel =
            MeesExemptionReasonFormModel().apply {
                exemptionReason = MeesExemptionReason.HIGH_COST
            }

        setupStateForPropertyRegistration()
        setupMockRegistrationService(registrationNumberValue)

        whenever(mockState.getGasSafetyCertificateIssueDateIfReachable()).thenReturn(gasCertIssueDate)
        whenever(mockState.getElectricalCertificateExpiryDateIfReachable()).thenReturn(eicrExpiryDate)
        whenever(mockState.acceptedEpc).thenReturn(epcDataModel)
        whenever(mockEpcCertificateUrlProvider.getEpcCertificateUrl(certificateNumber)).thenReturn(epcUrl)

        val mockTenancyStep = mock<EpcInDateAtStartOfTenancyCheckStep>()
        val mockEpcExemptionStep = mock<EpcExemptionStep>()
        val mockMeesExemptionStep = mock<MeesExemptionStep>()
        whenever(mockState.epcInDateAtStartOfTenancyCheckStep).thenReturn(mockTenancyStep)
        whenever(mockTenancyStep.formModelIfReachableOrNull).thenReturn(tenancyFormModel)
        whenever(mockState.epcExemptionStep).thenReturn(mockEpcExemptionStep)
        whenever(mockEpcExemptionStep.formModelIfReachableOrNull).thenReturn(epcExemptionFormModel)
        whenever(mockState.meesExemptionStep).thenReturn(mockMeesExemptionStep)
        whenever(mockMeesExemptionStep.formModelIfReachableOrNull).thenReturn(meesExemptionFormModel)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockPropertyComplianceService).saveRegistrationComplianceData(
            registrationNumberValue = registrationNumberValue,
            gasSafetyCertIssueDate = java.time.LocalDate.of(2024, 6, 15),
            eicrExpiryDate = java.time.LocalDate.of(2029, 3, 20),
            epcCertificateUrl = epcUrl,
            epcExpiryDate = java.time.LocalDate.of(2030, 1, 1),
            epcEnergyRating = "B",
            tenancyStartedBeforeEpcExpiry = true,
            epcExemptionReason = EpcExemptionReason.PROTECTED_ARCHITECTURAL_OR_HISTORICAL_MERIT,
            epcMeesExemptionReason = MeesExemptionReason.HIGH_COST,
        )
    }

    @Test
    fun `afterStepIsReached passes null for unreachable compliance steps`() {
        // Arrange
        val registrationNumberValue = 12345L

        setupStateForPropertyRegistration()
        setupMockRegistrationService(registrationNumberValue)

        whenever(mockState.getGasSafetyCertificateIssueDateIfReachable()).thenReturn(null)
        whenever(mockState.getElectricalCertificateExpiryDateIfReachable()).thenReturn(null)
        whenever(mockState.acceptedEpc).thenReturn(null)

        val mockTenancyStep = mock<EpcInDateAtStartOfTenancyCheckStep>()
        val mockEpcExemptionStep = mock<EpcExemptionStep>()
        val mockMeesExemptionStep = mock<MeesExemptionStep>()
        whenever(mockState.epcInDateAtStartOfTenancyCheckStep).thenReturn(mockTenancyStep)
        whenever(mockTenancyStep.formModelIfReachableOrNull).thenReturn(null)
        whenever(mockState.epcExemptionStep).thenReturn(mockEpcExemptionStep)
        whenever(mockEpcExemptionStep.formModelIfReachableOrNull).thenReturn(null)
        whenever(mockState.meesExemptionStep).thenReturn(mockMeesExemptionStep)
        whenever(mockMeesExemptionStep.formModelIfReachableOrNull).thenReturn(null)

        // Act
        stepConfig.afterStepIsReached(mockState)

        // Assert
        verify(mockPropertyComplianceService).saveRegistrationComplianceData(
            registrationNumberValue = registrationNumberValue,
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
    fun `resolveNextDestination deletes journey and redirects to already registered step when address is already registered`() {
        // Arrange
        val defaultDestination = Destination.ExternalUrl("redirect")
        val mockAlreadyRegisteredStep = mock<AlreadyRegisteredStep>()
        whenever(mockState.isAddressAlreadyRegistered).thenReturn(true)
        whenever(mockState.alreadyRegisteredStep).thenReturn(mockAlreadyRegisteredStep)

        // Act
        val result = stepConfig.resolveNextDestination(mockState, defaultDestination)

        // Assert
        verify(mockState).deleteJourney()
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

        whenever(mockState.gasUploadIds).thenReturn(emptyList())
        whenever(mockState.electricalUploadIds).thenReturn(emptyList())
    }

    private fun setupMockRegistrationService(registrationNumberValue: Long) {
        val mockRegistrationNumber = mock<RegistrationNumber>()
        whenever(mockRegistrationNumber.number).thenReturn(registrationNumberValue)
        whenever(
            mockPropertyRegistrationService.registerProperty(
                addressModel = any(),
                propertyType = any(),
                customPropertyType = anyOrNull(),
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
                jointLandlordEmails = anyOrNull(),
                gasSafetyFileUploadIds = any(),
                electricalSafetyFileUploadIds = any(),
            ),
        ).thenReturn(mockRegistrationNumber)
    }

    private fun setupStateForComplianceData() {
        whenever(mockState.getGasSafetyCertificateIssueDateIfReachable()).thenReturn(null)
        whenever(mockState.getElectricalCertificateExpiryDateIfReachable()).thenReturn(null)
        whenever(mockState.acceptedEpc).thenReturn(null)

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
