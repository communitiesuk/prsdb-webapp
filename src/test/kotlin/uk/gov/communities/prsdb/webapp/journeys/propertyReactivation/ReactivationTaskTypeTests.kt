package uk.gov.communities.prsdb.webapp.journeys.propertyReactivation

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.test.util.ReflectionTestUtils
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPropertyComplianceData
import kotlin.test.assertEquals

class ReactivationTaskTypeTests {
    @Test
    fun `requiredFor returns no task types for an unoccupied property, even if details were marked to provide later`() {
        val propertyOwnership = createPropertyOwnership(isOccupied = false, provideLater = ReactivationTaskType.entries.toSet())

        assertEquals(emptyList(), ReactivationTaskType.requiredFor(propertyOwnership))
    }

    @Test
    fun `requiredFor returns no task types for an occupied property with nothing marked to provide later`() {
        val propertyOwnership = createPropertyOwnership(isOccupied = true, provideLater = emptySet())

        assertEquals(emptyList(), ReactivationTaskType.requiredFor(propertyOwnership))
    }

    @Test
    fun `requiredFor returns no task types for an occupied property with no compliance record or provide later flags`() {
        val propertyOwnership = MockLandlordData.createPropertyOwnership(isOccupied = true)

        assertEquals(emptyList(), ReactivationTaskType.requiredFor(propertyOwnership))
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(ReactivationTaskType::class)
    fun `requiredFor returns only the task type marked to provide later for an occupied property`(taskType: ReactivationTaskType) {
        val propertyOwnership = createPropertyOwnership(isOccupied = true, provideLater = setOf(taskType))

        assertEquals(listOf(taskType), ReactivationTaskType.requiredFor(propertyOwnership))
    }

    @Test
    fun `requiredFor returns every task type in journey order when all are marked to provide later`() {
        val propertyOwnership = createPropertyOwnership(isOccupied = true, provideLater = ReactivationTaskType.entries.toSet())

        assertEquals(
            listOf(
                ReactivationTaskType.LICENSING,
                ReactivationTaskType.GAS_SAFETY,
                ReactivationTaskType.ELECTRICAL_SAFETY,
                ReactivationTaskType.EPC,
                ReactivationTaskType.TENANCY_DETAILS,
            ),
            ReactivationTaskType.requiredFor(propertyOwnership),
        )
    }

    private fun createPropertyOwnership(
        isOccupied: Boolean,
        provideLater: Set<ReactivationTaskType>,
    ): PropertyOwnership {
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                isOccupied = isOccupied,
                licenseProvideLater = ReactivationTaskType.LICENSING in provideLater,
                tenancyProvideLater = ReactivationTaskType.TENANCY_DETAILS in provideLater,
            )
        val propertyCompliance = MockPropertyComplianceData.createPropertyCompliance(propertyOwnership = propertyOwnership)
        propertyCompliance.gasSafetyCertProvideLater = ReactivationTaskType.GAS_SAFETY in provideLater
        propertyCompliance.electricalSafetyCertProvideLater = ReactivationTaskType.ELECTRICAL_SAFETY in provideLater
        propertyCompliance.epcProvideLater = ReactivationTaskType.EPC in provideLater
        ReflectionTestUtils.setField(propertyOwnership, "propertyCompliance", propertyCompliance)
        return propertyOwnership
    }
}
