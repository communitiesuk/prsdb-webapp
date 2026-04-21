package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityNotFoundException
import jakarta.servlet.http.HttpSession
import kotlinx.datetime.toKotlinLocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.FieldSource
import org.mockito.ArgumentCaptor.captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.internal.matchers.apachecommons.ReflectionEquals
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.PROPERTIES_WITH_COMPLIANCE_ADDED_THIS_SESSION
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.constants.enums.FurnishedStatus
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.RentFrequency
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.database.entity.VirusScanCallback
import uk.gov.communities.prsdb.webapp.database.repository.FileUploadRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyComplianceRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.database.repository.VirusScanCallbackRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.ComplianceStatusDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.EicrUpdateModel
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.EpcUpdateModel
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.GasSafetyCertUpdateModel
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.PropertyComplianceUpdateModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.ComplianceUpdateConfirmationEmail
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyComplianceBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPropertyComplianceData
import java.math.BigDecimal
import java.net.URI
import java.time.LocalDate
import kotlin.collections.listOf
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class PropertyComplianceServiceTests {
    @Mock
    private lateinit var mockPropertyComplianceRepository: PropertyComplianceRepository

    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockPropertyOwnershipRepository: PropertyOwnershipRepository

    @Mock
    private lateinit var mockSession: HttpSession

    @Mock
    private lateinit var mockVirusScanCallbackRepository: VirusScanCallbackRepository

    @Mock
    private lateinit var emailNotificationService: EmailNotificationService<ComplianceUpdateConfirmationEmail>

    @Mock
    private lateinit var absoluteUrlProvider: AbsoluteUrlProvider

    @Mock
    private lateinit var fileUploadRepository: FileUploadRepository

    @InjectMocks
    private lateinit var propertyComplianceService: PropertyComplianceService

    @Test
    fun `createPropertyCompliance creates and returns a compliance record`() {
        val expectedPropertyCompliance = MockPropertyComplianceData.createPropertyCompliance()

        whenever(mockPropertyOwnershipService.getPropertyOwnership(expectedPropertyCompliance.propertyOwnership.id))
            .thenReturn(expectedPropertyCompliance.propertyOwnership)
        whenever(mockPropertyComplianceRepository.save(any())).thenReturn(expectedPropertyCompliance)

        whenever(mockVirusScanCallbackRepository.findAllByFileUpload_Id(any())).thenReturn(
            expectedPropertyCompliance.gasSafetyFileUpload?.let {
                val listOf = listOf(VirusScanCallback(it, ""))
                listOf
            },
            expectedPropertyCompliance.eicrFileUpload?.let { listOf(VirusScanCallback(it, "")) },
        )

        val returnedPropertyCompliance =
            propertyComplianceService.createPropertyCompliance(
                propertyOwnershipId = expectedPropertyCompliance.propertyOwnership.id,
                gasSafetyCertUploadId = expectedPropertyCompliance.gasSafetyFileUpload?.id,
                gasSafetyCertIssueDate = expectedPropertyCompliance.gasSafetyCertIssueDate,
                gasSafetyCertEngineerNum = expectedPropertyCompliance.gasSafetyCertEngineerNum,
                gasSafetyCertExemptionReason = expectedPropertyCompliance.gasSafetyCertExemptionReason,
                gasSafetyCertExemptionOtherReason = expectedPropertyCompliance.gasSafetyCertExemptionOtherReason,
                eicrUploadId = expectedPropertyCompliance.eicrFileUpload?.id,
                eicrIssueDate = expectedPropertyCompliance.eicrIssueDate,
                eicrExemptionReason = expectedPropertyCompliance.eicrExemptionReason,
                eicrExemptionOtherReason = expectedPropertyCompliance.eicrExemptionOtherReason,
                epcUrl = expectedPropertyCompliance.epcUrl,
                epcExpiryDate = expectedPropertyCompliance.epcExpiryDate,
                tenancyStartedBeforeEpcExpiry = expectedPropertyCompliance.tenancyStartedBeforeEpcExpiry,
                epcEnergyRating = expectedPropertyCompliance.epcEnergyRating,
                epcExemptionReason = expectedPropertyCompliance.epcExemptionReason,
                epcMeesExemptionReason = expectedPropertyCompliance.epcMeesExemptionReason,
            )

        val propertyComplianceCaptor = captor<PropertyCompliance>()
        verify(mockPropertyComplianceRepository).save(propertyComplianceCaptor.capture())
        val capturedPropertyCompliance = propertyComplianceCaptor.value
        assertTrue(ReflectionEquals(expectedPropertyCompliance, "id").matches(capturedPropertyCompliance))
        assertEquals(expectedPropertyCompliance, returnedPropertyCompliance)
    }

    @Test
    fun `createPropertyCompliance with upload ID lists creates a compliance record with file uploads`() {
        // Arrange
        val propertyOwnershipId = 1L
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        val gasUpload1 = FileUpload(FileUploadStatus.QUARANTINED, "gas-1", "pdf", "etag1", "v1")
        val gasUpload2 = FileUpload(FileUploadStatus.QUARANTINED, "gas-2", "pdf", "etag2", "v2")
        val electricalUpload1 = FileUpload(FileUploadStatus.QUARANTINED, "eicr-1", "pdf", "etag3", "v3")

        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyOwnershipId)).thenReturn(propertyOwnership)
        whenever(mockPropertyComplianceRepository.save(any())).thenAnswer { it.arguments[0] }
        whenever(fileUploadRepository.getReferenceById(10L)).thenReturn(gasUpload1)
        whenever(fileUploadRepository.getReferenceById(20L)).thenReturn(gasUpload2)
        whenever(fileUploadRepository.getReferenceById(30L)).thenReturn(electricalUpload1)

        // Act
        propertyComplianceService.createPropertyCompliance(
            propertyOwnershipId = propertyOwnershipId,
            gasSafetyCertUploadIds = listOf(10L, 20L),
            electricalSafetyUploadIds = listOf(30L),
        )

        // Assert
        val propertyComplianceCaptor = captor<PropertyCompliance>()
        verify(mockPropertyComplianceRepository, org.mockito.kotlin.times(2)).save(propertyComplianceCaptor.capture())
        val capturedPropertyCompliance = propertyComplianceCaptor.allValues.last()
        assertEquals(propertyOwnership, capturedPropertyCompliance.propertyOwnership)
        assertEquals(listOf(gasUpload1, gasUpload2), capturedPropertyCompliance.gasSafetyFileUploads)
        assertEquals(listOf(electricalUpload1), capturedPropertyCompliance.electricalSafetyFileUploads)
    }

    @Test
    fun `getComplianceForPropertyOrNull retrieves the compliance record for the given property ownership ID`() {
        val expectedPropertyCompliance = MockPropertyComplianceData.createPropertyCompliance()
        whenever(mockPropertyComplianceRepository.findByPropertyOwnership_Id(expectedPropertyCompliance.propertyOwnership.id))
            .thenReturn(expectedPropertyCompliance)

        val returnedPropertyCompliance =
            propertyComplianceService.getComplianceForPropertyOrNull(expectedPropertyCompliance.propertyOwnership.id)

        assertEquals(returnedPropertyCompliance, returnedPropertyCompliance)
    }

    @Test
    fun `getComplianceForPropertyOrNull returns null when no compliance record exists for the given property ownership ID`() {
        val propertyOwnershipId = 123L
        whenever(mockPropertyComplianceRepository.findByPropertyOwnership_Id(propertyOwnershipId)).thenReturn(null)

        val returnedPropertyCompliance = propertyComplianceService.getComplianceForPropertyOrNull(propertyOwnershipId)

        assertNull(returnedPropertyCompliance)
    }

    @Test
    fun `getComplianceForProperty retrieves the compliance record for the given property ownership ID`() {
        val expectedPropertyCompliance = MockPropertyComplianceData.createPropertyCompliance()
        whenever(mockPropertyComplianceRepository.findByPropertyOwnership_Id(expectedPropertyCompliance.propertyOwnership.id))
            .thenReturn(expectedPropertyCompliance)

        val returnedPropertyCompliance =
            propertyComplianceService.getComplianceForProperty(expectedPropertyCompliance.propertyOwnership.id)

        assertEquals(expectedPropertyCompliance, returnedPropertyCompliance)
    }

    @Test
    fun `getComplianceForProperty throws an exception when no compliance record exists for the given property ownership ID`() {
        val propertyOwnershipId = 123L
        whenever(mockPropertyComplianceRepository.findByPropertyOwnership_Id(propertyOwnershipId)).thenReturn(null)

        assertThrows<EntityNotFoundException> { propertyComplianceService.getComplianceForProperty(propertyOwnershipId) }
    }

    @Test
    fun `getNumberOfNonCompliantPropertiesForLandlord returns a count of the landlord's non-compliant occupied properties`() {
        // Arrange
        val landlordBaseUserId = "baseUserId"
        val nonCompliantProperties =
            listOf(
                PropertyComplianceBuilder.createWithMissingCerts(propertyIsOccupied = true),
                PropertyComplianceBuilder.createWithExpiredCerts(propertyIsOccupied = true),
                PropertyComplianceBuilder.createWithInDateCertsAndLowEpcRating(propertyIsOccupied = true),
            )
        val compliantProperties =
            listOf(
                PropertyComplianceBuilder.createWithInDateCerts(propertyIsOccupied = true),
                PropertyComplianceBuilder.createWithCertExemptions(propertyIsOccupied = true),
            )
        val compliances = nonCompliantProperties + compliantProperties

        whenever(
            mockPropertyComplianceRepository.findAllByPropertyOwnership_PrimaryLandlord_BaseUser_Id(landlordBaseUserId),
        ).thenReturn(compliances)

        // Act
        val returnedCount = propertyComplianceService.getNumberOfNonCompliantPropertiesForLandlord(landlordBaseUserId)

        // Assert
        assertEquals(nonCompliantProperties.size, returnedCount)
    }

    @Test
    fun `getNumberOfNonCompliantPropertiesForLandlord only includes non-compliant unoccupied properties if they are expired`() {
        // Arrange
        val landlordBaseUserId = "baseUserId"
        val nonCompliantProperties =
            listOf(
                PropertyComplianceBuilder.createWithMissingCerts(propertyIsOccupied = false),
                PropertyComplianceBuilder.createWithExpiredCerts(propertyIsOccupied = false),
                PropertyComplianceBuilder.createWithInDateCertsAndLowEpcRating(propertyIsOccupied = false),
            )
        val compliantProperties =
            listOf(
                PropertyComplianceBuilder.createWithInDateCerts(propertyIsOccupied = false),
                PropertyComplianceBuilder.createWithCertExemptions(propertyIsOccupied = false),
            )
        val compliances = nonCompliantProperties + compliantProperties

        whenever(
            mockPropertyComplianceRepository.findAllByPropertyOwnership_PrimaryLandlord_BaseUser_Id(landlordBaseUserId),
        ).thenReturn(compliances)

        // Act
        val returnedCount = propertyComplianceService.getNumberOfNonCompliantPropertiesForLandlord(landlordBaseUserId)

        // Assert
        assertEquals(1, returnedCount)
    }

    @Test
    fun `getNonCompliantPropertiesForLandlord returns the landlord's non-compliant occupied properties`() {
        // Arrange
        val landlordBaseUserId = "baseUserId"
        val nonCompliantProperties =
            listOf(
                PropertyComplianceBuilder.createWithMissingCerts(propertyIsOccupied = true),
                PropertyComplianceBuilder.createWithExpiredCerts(propertyIsOccupied = true),
                PropertyComplianceBuilder.createWithInDateCertsAndLowEpcRating(propertyIsOccupied = true),
            )
        val compliantProperties =
            listOf(
                PropertyComplianceBuilder.createWithInDateCerts(propertyIsOccupied = true),
                PropertyComplianceBuilder.createWithCertExemptions(propertyIsOccupied = true),
            )
        val compliances = nonCompliantProperties + compliantProperties

        whenever(
            mockPropertyComplianceRepository.findAllByPropertyOwnership_PrimaryLandlord_BaseUser_Id(landlordBaseUserId),
        ).thenReturn(compliances)

        val expectedNonCompliantProperties =
            nonCompliantProperties.map { compliance ->
                ComplianceStatusDataModel.fromPropertyCompliance(compliance)
            }

        // Act
        val returnedNonCompliantProperties = propertyComplianceService.getNonCompliantPropertiesForLandlord(landlordBaseUserId)

        // Assert
        assertEquals(expectedNonCompliantProperties, returnedNonCompliantProperties)
    }

    @Test
    fun `getNonCompliantPropertiesForLandlord returns the only expired non-compliant unoccupied properties`() {
        // Arrange
        val landlordBaseUserId = "baseUserId"
        val nonCompliantProperties =
            listOf(
                PropertyComplianceBuilder.createWithMissingCerts(propertyIsOccupied = false),
                PropertyComplianceBuilder.createWithExpiredCerts(propertyIsOccupied = false),
                PropertyComplianceBuilder.createWithInDateCertsAndLowEpcRating(propertyIsOccupied = false),
            )
        val compliantProperties =
            listOf(
                PropertyComplianceBuilder.createWithInDateCerts(propertyIsOccupied = false),
                PropertyComplianceBuilder.createWithCertExemptions(propertyIsOccupied = false),
            )
        val compliances = nonCompliantProperties + compliantProperties

        whenever(
            mockPropertyComplianceRepository.findAllByPropertyOwnership_PrimaryLandlord_BaseUser_Id(landlordBaseUserId),
        ).thenReturn(compliances)

        val expectedNonCompliantProperties =
            listOf(ComplianceStatusDataModel.fromPropertyCompliance(nonCompliantProperties[1]))

        // Act
        val returnedNonCompliantProperties = propertyComplianceService.getNonCompliantPropertiesForLandlord(landlordBaseUserId)

        // Assert
        assertEquals(expectedNonCompliantProperties, returnedNonCompliantProperties)
    }

    @Test
    fun `updatePropertyCompliance changes the certificates associated with the given update model's non-null values`() {
        // Arrange
        val propertyCompliance =
            MockPropertyComplianceData.createPropertyCompliance(
                gasSafetyCertUpload = FileUpload(FileUploadStatus.SCANNED, "s3Key", "jpg", "eTag", "versionId"),
                gasSafetyCertIssueDate = LocalDate.now(),
                gasSafetyCertEngineerNum = "1234567",
                gasSafetyCertExemptionReason = null,
                gasSafetyCertExemptionOtherReason = null,
            )

        val updateModel =
            PropertyComplianceUpdateModel(
                gasSafetyCertUpdate =
                    GasSafetyCertUpdateModel(
                        exemptionReason = GasSafetyExemptionReason.OTHER,
                        exemptionOtherReason = "Other reason",
                    ),
            )

        whenever(mockPropertyComplianceRepository.findByPropertyOwnership_Id(propertyCompliance.propertyOwnership.id))
            .thenReturn(propertyCompliance)
        whenever(absoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI("https://example.com/dashboard"))

        // Act
        propertyComplianceService.updatePropertyCompliance(propertyCompliance.propertyOwnership.id, updateModel) {}

        // Assert
        assertEquals(updateModel.gasSafetyCertUpdate?.fileUploadId, propertyCompliance.gasSafetyFileUpload?.id)
        assertEquals(updateModel.gasSafetyCertUpdate?.issueDate, propertyCompliance.gasSafetyCertIssueDate)
        assertEquals(updateModel.gasSafetyCertUpdate?.engineerNum, propertyCompliance.gasSafetyCertEngineerNum)
        assertEquals(updateModel.gasSafetyCertUpdate?.exemptionReason, propertyCompliance.gasSafetyCertExemptionReason)
        assertEquals(
            updateModel.gasSafetyCertUpdate?.exemptionOtherReason,
            propertyCompliance.gasSafetyCertExemptionOtherReason,
        )
    }

    companion object {
        private fun createOccupiedPropertyOwnership() =
            MockLandlordData.createPropertyOwnership(
                currentNumHouseholds = 1,
                currentNumTenants = 1,
                numberOfBedrooms = 2,
                furnishedStatus = FurnishedStatus.FURNISHED,
                rentFrequency = RentFrequency.MONTHLY,
                rentAmount = BigDecimal("1000"),
            )

        val newGasSafetyCertUpdate =
            GasSafetyCertUpdateModel(
                fileUploadId = 1L,
                issueDate = LocalDate.now(),
                engineerNum = "1234567",
                exemptionReason = null,
                exemptionOtherReason = null,
            )

        val newGasSafetyExemptionUpdate =
            GasSafetyCertUpdateModel(
                fileUploadId = null,
                issueDate = null,
                engineerNum = null,
                exemptionReason = GasSafetyExemptionReason.NO_GAS_SUPPLY,
                exemptionOtherReason = null,
            )

        val newExpiredGasSafetyUpdate =
            GasSafetyCertUpdateModel(
                fileUploadId = null,
                issueDate = LocalDate.now(),
                engineerNum = null,
                exemptionReason = null,
                exemptionOtherReason = null,
            )

        val newEicrUpdate =
            EicrUpdateModel(
                fileUploadId = 1L,
                issueDate = LocalDate.now(),
                exemptionReason = null,
                exemptionOtherReason = null,
            )

        val newEicrExemptionUpdate =
            EicrUpdateModel(
                fileUploadId = null,
                issueDate = null,
                exemptionReason = EicrExemptionReason.LONG_LEASE,
                exemptionOtherReason = null,
            )

        val newExpiredEicrUpdate =
            EicrUpdateModel(
                fileUploadId = null,
                issueDate = LocalDate.now(),
                exemptionReason = null,
                exemptionOtherReason = null,
            )

        val newValidEpcUpdate =
            EpcUpdateModel(
                epcDataModel =
                    MockEpcData.createEpcDataModel(
                        expiryDate = LocalDate.now().toKotlinLocalDate(),
                        energyRating = "A",
                    ),
                url = "http://example.com/epc",
                tenancyStartedBeforeExpiry = null,
                exemptionReason = null,
                meesExemptionReason = null,
            )

        val newLowRatedEpcUpdate =
            EpcUpdateModel(
                epcDataModel =
                    MockEpcData.createEpcDataModel(
                        expiryDate = LocalDate.now().toKotlinLocalDate(),
                        energyRating = "G",
                    ),
                url = "http://example.com/epc",
                tenancyStartedBeforeExpiry = null,
                exemptionReason = null,
                meesExemptionReason = null,
            )

        val newExpiredBeforeTenancyEpcUpdate =
            EpcUpdateModel(
                epcDataModel =
                    MockEpcData.createEpcDataModel(
                        expiryDate = LocalDate.now().toKotlinLocalDate(),
                        energyRating = "A",
                    ),
                url = "http://example.com/epc",
                tenancyStartedBeforeExpiry = false,
                exemptionReason = null,
                meesExemptionReason = null,
            )

        val newExpiredDuringTenancyEpcUpdate =
            EpcUpdateModel(
                epcDataModel =
                    MockEpcData.createEpcDataModel(
                        expiryDate = LocalDate.now().toKotlinLocalDate(),
                        energyRating = "A",
                    ),
                url = "http://example.com/epc",
                tenancyStartedBeforeExpiry = true,
                exemptionReason = null,
                meesExemptionReason = null,
            )

        val newMeesExemptEpcUpdate =
            EpcUpdateModel(
                epcDataModel =
                    MockEpcData.createEpcDataModel(
                        expiryDate = LocalDate.now().toKotlinLocalDate(),
                        energyRating = "G",
                    ),
                url = "http://example.com/epc",
                tenancyStartedBeforeExpiry = null,
                exemptionReason = null,
                meesExemptionReason = MeesExemptionReason.NEW_LANDLORD,
            )

        val newEpcExemptUpdate =
            EpcUpdateModel(
                epcDataModel = null,
                url = null,
                tenancyStartedBeforeExpiry = null,
                exemptionReason = EpcExemptionReason.ANNUAL_ENERGY_CONSUMPTION_LESS_THAN_25_PERCENT,
                meesExemptionReason = null,
            )

        val newEpcRemovedUpdate =
            EpcUpdateModel(
                epcDataModel = null,
                url = null,
                tenancyStartedBeforeExpiry = null,
                exemptionReason = null,
                meesExemptionReason = null,
            )

        @JvmStatic
        val updatesAndCorrespondingEmails =
            listOf(
                Arguments.of(
                    PropertyComplianceUpdateModel(gasSafetyCertUpdate = newGasSafetyCertUpdate),
                    ComplianceUpdateConfirmationEmail.UpdateType.VALID_GAS_SAFETY_INFORMATION,
                    null,
                ),
                Arguments.of(
                    PropertyComplianceUpdateModel(gasSafetyCertUpdate = newGasSafetyExemptionUpdate),
                    ComplianceUpdateConfirmationEmail.UpdateType.VALID_GAS_SAFETY_INFORMATION,
                    null,
                ),
                Arguments.of(
                    PropertyComplianceUpdateModel(gasSafetyCertUpdate = newExpiredGasSafetyUpdate),
                    ComplianceUpdateConfirmationEmail.UpdateType.EXPIRED_GAS_SAFETY_INFORMATION,
                    null,
                ),
                Arguments.of(
                    PropertyComplianceUpdateModel(eicrUpdate = newEicrUpdate),
                    ComplianceUpdateConfirmationEmail.UpdateType.VALID_ELECTRICAL_INFORMATION,
                    null,
                ),
                Arguments.of(
                    PropertyComplianceUpdateModel(eicrUpdate = newEicrExemptionUpdate),
                    ComplianceUpdateConfirmationEmail.UpdateType.VALID_ELECTRICAL_INFORMATION,
                    null,
                ),
                Arguments.of(
                    PropertyComplianceUpdateModel(eicrUpdate = newExpiredEicrUpdate),
                    ComplianceUpdateConfirmationEmail.UpdateType.EXPIRED_ELECTRICAL_INFORMATION,
                    null,
                ),
                Arguments.of(
                    PropertyComplianceUpdateModel(epcUpdate = newValidEpcUpdate),
                    ComplianceUpdateConfirmationEmail.UpdateType.VALID_EPC_INFORMATION,
                    null,
                ),
                Arguments.of(
                    PropertyComplianceUpdateModel(epcUpdate = newLowRatedEpcUpdate),
                    ComplianceUpdateConfirmationEmail.UpdateType.LOW_RATED_EPC_INFORMATION,
                    null,
                ),
                Arguments.of(
                    PropertyComplianceUpdateModel(epcUpdate = newLowRatedEpcUpdate),
                    ComplianceUpdateConfirmationEmail.UpdateType.REMOVED_MEES_EPC_INFORMATION,
                    MeesExemptionReason.NEW_LANDLORD,
                ),
                Arguments.of(
                    PropertyComplianceUpdateModel(epcUpdate = newExpiredBeforeTenancyEpcUpdate),
                    ComplianceUpdateConfirmationEmail.UpdateType.EXPIRED_EPC_INFORMATION,
                    null,
                ),
                Arguments.of(
                    PropertyComplianceUpdateModel(epcUpdate = newExpiredDuringTenancyEpcUpdate),
                    ComplianceUpdateConfirmationEmail.UpdateType.VALID_EPC_INFORMATION,
                    null,
                ),
                Arguments.of(
                    PropertyComplianceUpdateModel(epcUpdate = newMeesExemptEpcUpdate),
                    ComplianceUpdateConfirmationEmail.UpdateType.VALID_EPC_INFORMATION,
                    null,
                ),
                Arguments.of(
                    PropertyComplianceUpdateModel(epcUpdate = newEpcExemptUpdate),
                    ComplianceUpdateConfirmationEmail.UpdateType.VALID_EPC_INFORMATION,
                    null,
                ),
                Arguments.of(
                    PropertyComplianceUpdateModel(epcUpdate = newEpcRemovedUpdate),
                    ComplianceUpdateConfirmationEmail.UpdateType.NO_EPC_INFORMATION,
                    null,
                ),
            )
    }

    @ParameterizedTest
    @FieldSource("updatesAndCorrespondingEmails")
    fun `updatePropertyCompliance sends an email with the correct template id determined by the update made`(
        update: PropertyComplianceUpdateModel,
        expectedTemplate: ComplianceUpdateConfirmationEmail.UpdateType,
        originalMeesExemptionReason: MeesExemptionReason? = null,
    ) {
        // Arrange
        val propertyCompliance =
            MockPropertyComplianceData.createPropertyCompliance(epcMeesExemptionReason = originalMeesExemptionReason)
        val dashboardUrl = URI("https://example.com/dashboard")

        whenever(mockPropertyComplianceRepository.findByPropertyOwnership_Id(propertyCompliance.propertyOwnership.id))
            .thenReturn(propertyCompliance)
        (update.gasSafetyCertUpdate?.fileUploadId ?: update.eicrUpdate?.fileUploadId)?.let {
            whenever(mockVirusScanCallbackRepository.findAllByFileUpload_Id(any()))
                .thenReturn(listOf(VirusScanCallback(FileUpload(), "")))
        }
        whenever(absoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(dashboardUrl)

        // Act
        propertyComplianceService.updatePropertyCompliance(propertyCompliance.propertyOwnership.id, update) {}

        // Assert
        val expectedEmailModel =
            ComplianceUpdateConfirmationEmail(
                propertyAddress = propertyCompliance.propertyOwnership.address.singleLineAddress,
                registrationNumber =
                    RegistrationNumberDataModel.fromRegistrationNumber(
                        propertyCompliance.propertyOwnership.registrationNumber,
                    ),
                dashboardUrl = dashboardUrl,
                complianceUpdateType = expectedTemplate,
            )

        verify(emailNotificationService).sendEmail(
            propertyCompliance.propertyOwnership.primaryLandlord.email,
            expectedEmailModel,
        )
    }

    @Test
    fun `updatePropertyCompliance does not change the certificates associated with the given update model's null values`() {
        // Arrange
        val propertyCompliance =
            MockPropertyComplianceData.createPropertyCompliance(
                gasSafetyCertUpload = FileUpload(FileUploadStatus.SCANNED, "s3Key", "jpg", "eTag", "versionId"),
                gasSafetyCertIssueDate = LocalDate.now(),
                gasSafetyCertEngineerNum = "1234567",
                gasSafetyCertExemptionReason = null,
                gasSafetyCertExemptionOtherReason = null,
            )

        val originalEicrS3Key = propertyCompliance.eicrS3Key
        val originalEicrIssueDate = propertyCompliance.eicrIssueDate
        val originalEicrExemptionReason = propertyCompliance.eicrExemptionReason
        val originalEicrExemptionOtherReason = propertyCompliance.eicrExemptionOtherReason

        val updateModel =
            PropertyComplianceUpdateModel(
                gasSafetyCertUpdate =
                    GasSafetyCertUpdateModel(
                        exemptionReason = GasSafetyExemptionReason.OTHER,
                        exemptionOtherReason = "Other reason",
                    ),
            )

        whenever(mockPropertyComplianceRepository.findByPropertyOwnership_Id(propertyCompliance.propertyOwnership.id))
            .thenReturn(propertyCompliance)

        whenever(absoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI("https://example.com/dashboard"))

        // Act
        propertyComplianceService.updatePropertyCompliance(propertyCompliance.propertyOwnership.id, updateModel) {}

        // Assert
        assertEquals(originalEicrS3Key, propertyCompliance.eicrS3Key)
        assertEquals(originalEicrIssueDate, propertyCompliance.eicrIssueDate)
        assertEquals(originalEicrExemptionReason, propertyCompliance.eicrExemptionReason)
        assertEquals(originalEicrExemptionOtherReason, propertyCompliance.eicrExemptionOtherReason)
    }

    @Test
    fun `updatePropertyCompliance preserves unrelated EICR and EPC values when only gas safety is updated`() {
        // Arrange
        val propertyCompliance =
            MockPropertyComplianceData.createPropertyCompliance(
                gasSafetyCertUpload = FileUpload(FileUploadStatus.SCANNED, "s3Key", "jpg", "eTag", "versionId"),
                gasSafetyCertIssueDate = LocalDate.now(),
                gasSafetyCertEngineerNum = "1234567",
                gasSafetyCertExemptionReason = null,
                gasSafetyCertExemptionOtherReason = null,
            )

        val originalEicrS3Key = propertyCompliance.eicrS3Key
        val originalEicrIssueDate = propertyCompliance.eicrIssueDate
        val originalEicrExemptionReason = propertyCompliance.eicrExemptionReason
        val originalEicrExemptionOtherReason = propertyCompliance.eicrExemptionOtherReason
        val originalEpcUrl = propertyCompliance.epcUrl
        val originalEpcExpiryDate = propertyCompliance.epcExpiryDate
        val originalTenancyStartedBeforeEpcExpiry = propertyCompliance.tenancyStartedBeforeEpcExpiry
        val originalEpcEnergyRating = propertyCompliance.epcEnergyRating
        val originalEpcExemptionReason = propertyCompliance.epcExemptionReason
        val originalEpcMeesExemptionReason = propertyCompliance.epcMeesExemptionReason

        val updateModel =
            PropertyComplianceUpdateModel(
                gasSafetyCertUpdate =
                    GasSafetyCertUpdateModel(
                        exemptionReason = GasSafetyExemptionReason.OTHER,
                        exemptionOtherReason = "Other reason",
                    ),
            )

        whenever(mockPropertyComplianceRepository.findByPropertyOwnership_Id(propertyCompliance.propertyOwnership.id))
            .thenReturn(propertyCompliance)

        whenever(absoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(URI("https://example.com/dashboard"))

        // Act
        propertyComplianceService.updatePropertyCompliance(propertyCompliance.propertyOwnership.id, updateModel) {}

        // Assert - non-targeted areas are unchanged
        assertEquals(originalEicrS3Key, propertyCompliance.eicrS3Key)
        assertEquals(originalEicrIssueDate, propertyCompliance.eicrIssueDate)
        assertEquals(originalEicrExemptionReason, propertyCompliance.eicrExemptionReason)
        assertEquals(originalEicrExemptionOtherReason, propertyCompliance.eicrExemptionOtherReason)
        assertEquals(originalEpcUrl, propertyCompliance.epcUrl)
        assertEquals(originalEpcExpiryDate, propertyCompliance.epcExpiryDate)
        assertEquals(originalTenancyStartedBeforeEpcExpiry, propertyCompliance.tenancyStartedBeforeEpcExpiry)
        assertEquals(originalEpcEnergyRating, propertyCompliance.epcEnergyRating)
        assertEquals(originalEpcExemptionReason, propertyCompliance.epcExemptionReason)
        assertEquals(originalEpcMeesExemptionReason, propertyCompliance.epcMeesExemptionReason)
    }

    @Test
    fun `when checkUpdateIsValid throws an exception, no update occurs`() {
        // Arrange
        val propertyCompliance =
            MockPropertyComplianceData.createPropertyCompliance(
                gasSafetyCertUpload = FileUpload(FileUploadStatus.SCANNED, "s3Key", "jpg", "eTag", "versionId"),
                gasSafetyCertIssueDate = LocalDate.now(),
                gasSafetyCertEngineerNum = "1234567",
                gasSafetyCertExemptionReason = null,
                gasSafetyCertExemptionOtherReason = null,
            )

        // Capture original gas safety cert values
        val originalGasSafetyCertS3Key = propertyCompliance.gasSafetyCertS3Key
        val originalGasSafetyCertIssueDate = propertyCompliance.gasSafetyCertIssueDate
        val originalGasSafetyCertEngineerNum = propertyCompliance.gasSafetyCertEngineerNum
        val originalGasSafetyCertExemptionReason = propertyCompliance.gasSafetyCertExemptionReason
        val originalGasSafetyCertExemptionOtherReason = propertyCompliance.gasSafetyCertExemptionOtherReason

        val updateModel =
            PropertyComplianceUpdateModel(
                gasSafetyCertUpdate =
                    GasSafetyCertUpdateModel(
                        exemptionReason = GasSafetyExemptionReason.OTHER,
                        exemptionOtherReason = "Other reason",
                    ),
            )

        // Act
        try {
            propertyComplianceService.updatePropertyCompliance(propertyCompliance.propertyOwnership.id, updateModel) {
                throw Exception("Validation failed")
            }
        } catch (_: Exception) {
            // Expected exception, do nothing
        }

        // Assert
        assertEquals(originalGasSafetyCertS3Key, propertyCompliance.gasSafetyCertS3Key)
        assertEquals(originalGasSafetyCertIssueDate, propertyCompliance.gasSafetyCertIssueDate)
        assertEquals(originalGasSafetyCertEngineerNum, propertyCompliance.gasSafetyCertEngineerNum)
        assertEquals(originalGasSafetyCertExemptionReason, propertyCompliance.gasSafetyCertExemptionReason)
        assertEquals(originalGasSafetyCertExemptionOtherReason, propertyCompliance.gasSafetyCertExemptionOtherReason)
    }

    @Test
    fun `addToPropertiesWithComplianceAddedThisSession adds the given property ownership ID to the session set`() {
        val propertyOwnershipId = 123L
        val initialSet = setOf(456L, 789L)
        whenever(mockSession.getAttribute(PROPERTIES_WITH_COMPLIANCE_ADDED_THIS_SESSION)).thenReturn(initialSet)

        propertyComplianceService.addToPropertiesWithComplianceAddedThisSession(propertyOwnershipId)

        val expectedUpdatedSet = initialSet + propertyOwnershipId
        verify(mockSession).setAttribute(PROPERTIES_WITH_COMPLIANCE_ADDED_THIS_SESSION, expectedUpdatedSet)
    }

    @Test
    fun `addToPropertiesWithComplianceAddedThisSession does nothing when the given property ownership ID is already in the session set`() {
        val propertyOwnershipId = 123L
        val initialSet = setOf(propertyOwnershipId, 789L)
        whenever(mockSession.getAttribute(PROPERTIES_WITH_COMPLIANCE_ADDED_THIS_SESSION)).thenReturn(initialSet)

        propertyComplianceService.addToPropertiesWithComplianceAddedThisSession(propertyOwnershipId)

        verify(mockSession).setAttribute(PROPERTIES_WITH_COMPLIANCE_ADDED_THIS_SESSION, initialSet)
    }

    @Test
    fun `wasPropertyComplianceAddedThisSession returns true if the property ownership ID is in the session set`() {
        val propertyOwnershipId = 123L
        val sessionSet = setOf(456L, 789L, propertyOwnershipId)
        whenever(mockSession.getAttribute(PROPERTIES_WITH_COMPLIANCE_ADDED_THIS_SESSION)).thenReturn(sessionSet)

        assertTrue(propertyComplianceService.wasPropertyComplianceAddedThisSession(propertyOwnershipId))
    }

    @Test
    fun `wasPropertyComplianceAddedThisSession returns false if the property ownership ID is not in the session set`() {
        val propertyOwnershipId = 123L
        val sessionSet = listOf(456L, 789L)
        whenever(mockSession.getAttribute(PROPERTIES_WITH_COMPLIANCE_ADDED_THIS_SESSION)).thenReturn(sessionSet)

        assertFalse(propertyComplianceService.wasPropertyComplianceAddedThisSession(propertyOwnershipId))
    }

    @Nested
    inner class SaveRegistrationComplianceData {
        private val mockPropertyOwnership = MockLandlordData.createPropertyOwnership()
        private val registrationNumberValue = 12345L

        @Test
        fun `creates new compliance record when none exists`() {
            whenever(mockPropertyOwnershipRepository.findByRegistrationNumber_Number(registrationNumberValue))
                .thenReturn(mockPropertyOwnership)
            whenever(mockPropertyComplianceRepository.findByPropertyOwnership_Id(mockPropertyOwnership.id))
                .thenReturn(null)
            whenever(mockPropertyComplianceRepository.save(any<PropertyCompliance>()))
                .thenAnswer { it.arguments[0] }

            propertyComplianceService.saveRegistrationComplianceData(
                registrationNumberValue = registrationNumberValue,
                epcCertificateUrl = "https://epc.example.com/cert/1234",
                epcExpiryDate = LocalDate.of(2030, 1, 1),
                epcEnergyRating = "B",
            )

            val captor = captor<PropertyCompliance>()
            verify(mockPropertyComplianceRepository).save(captor.capture())
            assertEquals("https://epc.example.com/cert/1234", captor.value.epcUrl)
            assertEquals(LocalDate.of(2030, 1, 1), captor.value.epcExpiryDate)
            assertEquals("B", captor.value.epcEnergyRating)
        }

        @Test
        fun `sets all compliance fields correctly`() {
            val existingCompliance = PropertyCompliance()

            whenever(mockPropertyOwnershipRepository.findByRegistrationNumber_Number(registrationNumberValue))
                .thenReturn(mockPropertyOwnership)
            whenever(mockPropertyComplianceRepository.findByPropertyOwnership_Id(mockPropertyOwnership.id))
                .thenReturn(existingCompliance)
            whenever(mockPropertyComplianceRepository.save(any<PropertyCompliance>()))
                .thenAnswer { it.arguments[0] }

            val gasCertIssueDate = LocalDate.of(2024, 6, 15)
            val eicrExpiryDate = LocalDate.of(2029, 3, 20)
            val epcUrl = "https://epc.example.com/cert/1234"
            val epcExpiryDate = LocalDate.of(2030, 1, 1)
            val epcEnergyRating = "C"
            val epcExemptionReason = EpcExemptionReason.PROTECTED_ARCHITECTURAL_OR_HISTORICAL_MERIT
            val meesExemptionReason = MeesExemptionReason.HIGH_COST

            propertyComplianceService.saveRegistrationComplianceData(
                registrationNumberValue = registrationNumberValue,
                gasSafetyCertIssueDate = gasCertIssueDate,
                eicrExpiryDate = eicrExpiryDate,
                epcCertificateUrl = epcUrl,
                epcExpiryDate = epcExpiryDate,
                epcEnergyRating = epcEnergyRating,
                tenancyStartedBeforeEpcExpiry = true,
                epcExemptionReason = epcExemptionReason,
                epcMeesExemptionReason = meesExemptionReason,
            )

            val captor = captor<PropertyCompliance>()
            verify(mockPropertyComplianceRepository).save(captor.capture())
            val saved = captor.value
            assertEquals(gasCertIssueDate, saved.gasSafetyCertIssueDate)
            assertEquals(eicrExpiryDate, saved.eicrExpiryDate)
            assertEquals(epcUrl, saved.epcUrl)
            assertEquals(epcExpiryDate, saved.epcExpiryDate)
            assertEquals(epcEnergyRating, saved.epcEnergyRating)
            assertEquals(true, saved.tenancyStartedBeforeEpcExpiry)
            assertEquals(epcExemptionReason, saved.epcExemptionReason)
            assertEquals(meesExemptionReason, saved.epcMeesExemptionReason)
        }
    }
}
