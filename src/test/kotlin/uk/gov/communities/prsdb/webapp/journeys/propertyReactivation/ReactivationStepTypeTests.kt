package uk.gov.communities.prsdb.webapp.journeys.propertyReactivation

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.test.util.ReflectionTestUtils
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPropertyComplianceData
import kotlin.test.assertEquals

class ReactivationStepTypeTests {
    @Test
    fun `requiredFor returns no step types for an unoccupied property, even if details were marked to provide later`() {
        val propertyOwnership = createPropertyOwnership(isOccupied = false, provideLater = ReactivationStepType.entries.toSet())

        assertEquals(emptyList(), ReactivationStepType.requiredFor(propertyOwnership))
    }

    @Test
    fun `requiredFor returns no step types for an occupied property with nothing marked to provide later`() {
        val propertyOwnership = createPropertyOwnership(isOccupied = true, provideLater = emptySet())

        assertEquals(emptyList(), ReactivationStepType.requiredFor(propertyOwnership))
    }

    @Test
    fun `requiredFor returns no step types for an occupied property with no compliance record or provide later flags`() {
        val propertyOwnership = MockLandlordData.createPropertyOwnership(isOccupied = true)

        assertEquals(emptyList(), ReactivationStepType.requiredFor(propertyOwnership))
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(ReactivationStepType::class)
    fun `requiredFor returns only the step type marked to provide later for an occupied property`(stepType: ReactivationStepType) {
        val propertyOwnership = createPropertyOwnership(isOccupied = true, provideLater = setOf(stepType))

        assertEquals(listOf(stepType), ReactivationStepType.requiredFor(propertyOwnership))
    }

    @Test
    fun `requiredFor returns every step type in journey order when all are marked to provide later`() {
        val propertyOwnership = createPropertyOwnership(isOccupied = true, provideLater = ReactivationStepType.entries.toSet())

        assertEquals(
            listOf(
                ReactivationStepType.LICENSING,
                ReactivationStepType.GAS_SAFETY,
                ReactivationStepType.ELECTRICAL_SAFETY,
                ReactivationStepType.EPC,
                ReactivationStepType.TENANCY_DETAILS,
            ),
            ReactivationStepType.requiredFor(propertyOwnership),
        )
    }

    private fun createPropertyOwnership(
        isOccupied: Boolean,
        provideLater: Set<ReactivationStepType>,
    ): PropertyOwnership {
        val propertyOwnership =
            MockLandlordData.createPropertyOwnership(
                isOccupied = isOccupied,
                licenseProvideLater = ReactivationStepType.LICENSING in provideLater,
                tenancyProvideLater = ReactivationStepType.TENANCY_DETAILS in provideLater,
            )
        val propertyCompliance = MockPropertyComplianceData.createPropertyCompliance(propertyOwnership = propertyOwnership)
        propertyCompliance.gasSafetyCertProvideLater = ReactivationStepType.GAS_SAFETY in provideLater
        propertyCompliance.electricalSafetyCertProvideLater = ReactivationStepType.ELECTRICAL_SAFETY in provideLater
        propertyCompliance.epcProvideLater = ReactivationStepType.EPC in provideLater
        ReflectionTestUtils.setField(propertyOwnership, "propertyCompliance", propertyCompliance)
        return propertyOwnership
    }
}
