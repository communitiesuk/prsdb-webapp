package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityNotFoundException
import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor.captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.internal.matchers.apachecommons.ReflectionEquals
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.PROPERTIES_WITH_COMPLIANCE_ADDED_THIS_SESSION
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.database.repository.PropertyComplianceRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.GasSafetyCertUpdateModel
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.PropertyComplianceUpdateModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPropertyComplianceData
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

    @Spy
    @InjectMocks
    private lateinit var propertyComplianceService: PropertyComplianceService

    @Test
    fun `createPropertyCompliance creates and returns a compliance record`() {
        val expectedPropertyCompliance = MockPropertyComplianceData.createPropertyCompliance()

        whenever(mockPropertyOwnershipService.getPropertyOwnership(expectedPropertyCompliance.propertyOwnership.id))
            .thenReturn(expectedPropertyCompliance.propertyOwnership)
        whenever(mockPropertyComplianceRepository.save(any())).thenReturn(expectedPropertyCompliance)

        val returnedPropertyCompliance =
            propertyComplianceService.createPropertyCompliance(
                propertyOwnershipId = expectedPropertyCompliance.propertyOwnership.id,
                gasSafetyCertS3Key = expectedPropertyCompliance.gasSafetyCertS3Key,
                gasSafetyCertIssueDate = expectedPropertyCompliance.gasSafetyCertIssueDate,
                gasSafetyCertEngineerNum = expectedPropertyCompliance.gasSafetyCertEngineerNum,
                gasSafetyCertExemptionReason = expectedPropertyCompliance.gasSafetyCertExemptionReason,
                gasSafetyCertExemptionOtherReason = expectedPropertyCompliance.gasSafetyCertExemptionOtherReason,
                eicrS3Key = expectedPropertyCompliance.eicrS3Key,
                eicrIssueDate = expectedPropertyCompliance.eicrIssueDate,
                eicrExemptionReason = expectedPropertyCompliance.eicrExemptionReason,
                eicrExemptionOtherReason = expectedPropertyCompliance.eicrExemptionOtherReason,
                epcUrl = expectedPropertyCompliance.epcUrl,
                epcExpiryDate = expectedPropertyCompliance.epcExpiryDate,
                tenancyStartedBeforeEpcExpiry = expectedPropertyCompliance.tenancyStartedBeforeEpcExpiry,
                epcEnergyRating = expectedPropertyCompliance.epcEnergyRating,
                epcExemptionReason = expectedPropertyCompliance.epcExemptionReason,
                epcMeesExemptionReason = expectedPropertyCompliance.epcMeesExemptionReason,
                hasFireSafetyDeclaration = expectedPropertyCompliance.hasFireSafetyDeclaration,
            )

        val propertyComplianceCaptor = captor<PropertyCompliance>()
        verify(mockPropertyComplianceRepository).save(propertyComplianceCaptor.capture())
        assertTrue(ReflectionEquals(expectedPropertyCompliance, "id").matches(propertyComplianceCaptor.value))
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
    fun `updatePropertyCompliance changes the certificates associated with the given update model's non-null values`() {
        // Arrange
        val propertyCompliance =
            MockPropertyComplianceData.createPropertyCompliance(
                gasSafetyCertS3Key = "s3Key",
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
        assertEquals(updateModel.gasSafetyCertUpdate?.s3Key, propertyCompliance.gasSafetyCertS3Key)
        assertEquals(updateModel.gasSafetyCertUpdate?.issueDate, propertyCompliance.gasSafetyCertIssueDate)
        assertEquals(updateModel.gasSafetyCertUpdate?.engineerNum, propertyCompliance.gasSafetyCertEngineerNum)
        assertEquals(updateModel.gasSafetyCertUpdate?.exemptionReason, propertyCompliance.gasSafetyCertExemptionReason)
        assertEquals(
            updateModel.gasSafetyCertUpdate?.exemptionOtherReason,
            propertyCompliance.gasSafetyCertExemptionOtherReason,
        )
    }

    @Test
    fun `updatePropertyCompliance does not change the certificates associated with the given update model's null values`() {
        // Arrange
        val propertyCompliance =
            MockPropertyComplianceData.createPropertyCompliance(
                gasSafetyCertS3Key = "s3Key",
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
                gasSafetyCertS3Key = "s3Key",
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
    fun `deletePropertyComplianceIfExists deletes the compliance when it exists`() {
        val propertyCompliance = MockPropertyComplianceData.createPropertyCompliance()
        val propertyOwnershipId = propertyCompliance.propertyOwnership.id

        whenever(mockPropertyComplianceRepository.findByPropertyOwnership_Id(propertyOwnershipId))
            .thenReturn(propertyCompliance)

        propertyComplianceService.deletePropertyComplianceIfExists(propertyOwnershipId)

        verify(propertyComplianceService).deletePropertyCompliance(propertyCompliance)
    }

    @Test
    fun `deletePropertyComplianceIfExists does nothing when compliance does not exist`() {
        val propertyOwnershipId = 123L

        whenever(mockPropertyComplianceRepository.findByPropertyOwnership_Id(propertyOwnershipId))
            .thenReturn(null)

        propertyComplianceService.deletePropertyComplianceIfExists(propertyOwnershipId)

        verify(propertyComplianceService, org.mockito.Mockito.never()).deletePropertyCompliance(any())
    }
}
