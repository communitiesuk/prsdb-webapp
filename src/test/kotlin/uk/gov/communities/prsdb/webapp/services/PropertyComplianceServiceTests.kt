package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor.captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.internal.matchers.apachecommons.ReflectionEquals
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.database.repository.PropertyComplianceRepository
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPropertyComplianceData
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class PropertyComplianceServiceTests {
    @Mock
    private lateinit var mockPropertyComplianceRepository: PropertyComplianceRepository

    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

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
            epcEnergyRating = expectedPropertyCompliance.epcEnergyRating,
            epcExemptionReason = expectedPropertyCompliance.epcExemptionReason,
            epcMeesExemptionReason = expectedPropertyCompliance.epcMeesExemptionReason,
            hasFireSafetyDeclaration = expectedPropertyCompliance.hasFireSafetyDeclaration,
        )

        val propertyComplianceCaptor = captor<PropertyCompliance>()
        verify(mockPropertyComplianceRepository).save(propertyComplianceCaptor.capture())
        assertTrue(ReflectionEquals(expectedPropertyCompliance, "id").matches(propertyComplianceCaptor.value))
    }
}
