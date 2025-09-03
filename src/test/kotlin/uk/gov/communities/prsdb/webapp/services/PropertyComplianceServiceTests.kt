package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityNotFoundException
import jakarta.servlet.http.HttpSession
import kotlinx.datetime.toKotlinLocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
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
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.PROPERTIES_WITH_COMPLIANCE_ADDED_THIS_SESSION
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.database.entity.CertificateUpload
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.database.repository.CertificateUploadRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyComplianceRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.ComplianceStatusDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.EicrUpdateModel
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.EpcUpdateModel
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.GasSafetyCertUpdateModel
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.PropertyComplianceUpdateModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.ComplianceUpdateConfirmationEmail
import uk.gov.communities.prsdb.webapp.testHelpers.builders.PropertyComplianceBuilder
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPropertyComplianceData
import java.net.URI
import java.time.LocalDate
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class PropertyComplianceServiceTests {
    @Mock
    private lateinit var mockPropertyComplianceRepository: PropertyComplianceRepository

    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockSession: HttpSession

    @Mock
    private lateinit var mockCertificateUploadRepository: CertificateUploadRepository

    @Mock
    private lateinit var emailNotificationService: EmailNotificationService<ComplianceUpdateConfirmationEmail>

    @Mock
    private lateinit var absoluteUrlProvider: AbsoluteUrlProvider

    @InjectMocks
    private lateinit var propertyComplianceService: PropertyComplianceService

    @Test
    fun `createPropertyCompliance creates and returns a compliance record`() {
        val expectedPropertyCompliance = MockPropertyComplianceData.createPropertyCompliance()

        whenever(mockPropertyOwnershipService.getPropertyOwnership(expectedPropertyCompliance.propertyOwnership.id))
            .thenReturn(expectedPropertyCompliance.propertyOwnership)
        whenever(mockPropertyComplianceRepository.save(any())).thenReturn(expectedPropertyCompliance)

        whenever(mockCertificateUploadRepository.findByFileUpload_Id(any())).thenReturn(
            expectedPropertyCompliance.gasSafetyFileUpload?.let { CertificateUpload(it, mock(), mock()) },
            expectedPropertyCompliance.eicrFileUpload?.let { CertificateUpload(it, mock(), mock()) },
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
    fun `getNumberOfNonCompliantPropertiesForLandlord returns a count of the landlord's non-compliant properties`() {
        // Arrange
        val landlordBaseUserId = "baseUserId"
        val nonCompliantProperties =
            listOf(PropertyComplianceBuilder.createWithMissingCerts(), PropertyComplianceBuilder.createWithExpiredCerts())
        val compliances = nonCompliantProperties + PropertyComplianceBuilder.createWithInDateCerts()

        whenever(
            mockPropertyComplianceRepository.findAllByPropertyOwnership_PrimaryLandlord_BaseUser_Id(landlordBaseUserId),
        ).thenReturn(compliances)

        // Act
        val returnedCount = propertyComplianceService.getNumberOfNonCompliantPropertiesForLandlord(landlordBaseUserId)

        // Assert
        assertEquals(nonCompliantProperties.size, returnedCount)
    }

    @Test
    fun `getNonCompliantPropertiesForLandlord returns the landlord's non-compliant properties`() {
        // Arrange
        val landlordBaseUserId = "baseUserId"
        val nonCompliantProperty = PropertyComplianceBuilder.createWithMissingCerts()
        val compliances = listOf(PropertyComplianceBuilder.createWithInDateCerts(), nonCompliantProperty)

        whenever(
            mockPropertyComplianceRepository.findAllByPropertyOwnership_PrimaryLandlord_BaseUser_Id(landlordBaseUserId),
        ).thenReturn(compliances)

        // Act
        val returnedNonCompliantProperties = propertyComplianceService.getNonCompliantPropertiesForLandlord(landlordBaseUserId)

        // Assert
        val expectedNonCompliantProperties = listOf(ComplianceStatusDataModel.fromPropertyCompliance(nonCompliantProperty))
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
            whenever(mockCertificateUploadRepository.findByFileUpload_Id(any()))
                .thenReturn(mock<CertificateUpload>())
        }
        whenever(absoluteUrlProvider.buildLandlordDashboardUri()).thenReturn(dashboardUrl)

        // Act
        propertyComplianceService.updatePropertyCompliance(propertyCompliance.propertyOwnership.id, update) {}

        // Assert
        val expectedEmailModel =
            ComplianceUpdateConfirmationEmail(
                propertyAddress = propertyCompliance.propertyOwnership.property.address.singleLineAddress,
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

        // Act
        propertyComplianceService.updatePropertyCompliance(propertyCompliance.propertyOwnership.id, updateModel) {}

        // Assert
        assertEquals(originalEicrS3Key, propertyCompliance.eicrS3Key)
        assertEquals(originalEicrIssueDate, propertyCompliance.eicrIssueDate)
        assertEquals(originalEicrExemptionReason, propertyCompliance.eicrExemptionReason)
        assertEquals(originalEicrExemptionOtherReason, propertyCompliance.eicrExemptionOtherReason)
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

    @Test
    fun `deletePropertyCompliance deletes the given PropertyCompliance`() {
        val propertyCompliance = MockPropertyComplianceData.createPropertyCompliance()

        propertyComplianceService.deletePropertyCompliance(propertyCompliance)

        verify(mockPropertyComplianceRepository).delete(propertyCompliance)
    }

    @Test
    fun `deletePropertyCompliancesByOwnershipIds deletes PropertyCompliances with the given PropertyOwnershipIds`() {
        val propertyOwnershipIds =
            listOf(
                1L,
                2L,
            )

        propertyComplianceService.deletePropertyCompliancesByOwnershipIds(propertyOwnershipIds)

        verify(mockPropertyComplianceRepository).deleteByPropertyOwnership_IdIn(propertyOwnershipIds)
    }

    @Test
    fun `deletePropertyComplianceByOwnershipId deletes the PropertyCompliance with the given PropertyOwnershipId`() {
        val propertyOwnershipId = 1L

        propertyComplianceService.deletePropertyComplianceByOwnershipId(propertyOwnershipId)

        verify(mockPropertyComplianceRepository).deleteByPropertyOwnership_Id(propertyOwnershipId)
    }
}
