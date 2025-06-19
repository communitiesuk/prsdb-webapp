package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor.captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.internal.matchers.apachecommons.ReflectionEquals
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.PROPERTIES_WITH_COMPLIANCE_ADDED_THIS_SESSION
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.database.repository.PropertyComplianceRepository
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPropertyComplianceData
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

    @InjectMocks
    private lateinit var propertyComplianceService: PropertyComplianceService

    @Test
    fun `createPropertyCompliance creates a compliance record`() {
        val expectedPropertyCompliance = MockPropertyComplianceData.createPropertyCompliance()

        whenever(mockPropertyOwnershipService.getPropertyOwnership(expectedPropertyCompliance.propertyOwnership.id))
            .thenReturn(expectedPropertyCompliance.propertyOwnership)

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
    }

    @Test
    fun `getComplianceForProperty retrieves the compliance record for the given property ownership ID`() {
        val expectedPropertyCompliance = MockPropertyComplianceData.createPropertyCompliance()
        whenever(mockPropertyComplianceRepository.findByPropertyOwnership_Id(expectedPropertyCompliance.propertyOwnership.id))
            .thenReturn(expectedPropertyCompliance)

        val returnedPropertyCompliance = propertyComplianceService.getComplianceForProperty(expectedPropertyCompliance.propertyOwnership.id)

        assertEquals(returnedPropertyCompliance, returnedPropertyCompliance)
    }

    @Test
    fun `getComplianceForProperty returns null when no compliance record exists for the given property ownership ID`() {
        val propertyOwnershipId = 123L
        whenever(mockPropertyComplianceRepository.findByPropertyOwnership_Id(propertyOwnershipId)).thenReturn(null)

        val returnedPropertyCompliance = propertyComplianceService.getComplianceForProperty(propertyOwnershipId)

        assertNull(returnedPropertyCompliance)
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
}
