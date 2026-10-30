package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdType
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdTypeStep

class OwnershipStructureTaskTests {
    @ParameterizedTest(name = "freehold={0} -> isJointFreeholder={1}")
    @MethodSource("provideFreeholdTypeScenarios")
    fun `isJointFreeholder is true only for joint tenants or tenants in common`(
        freeholdType: FreeholdType?,
        expected: Boolean,
    ) {
        val freeholdTypeStep = mock<FreeholdTypeStep> { on { outcome } doReturn freeholdType }
        val ownershipTypeTask = mock<OwnershipTypeTask> { on { this.freeholdTypeStep } doReturn freeholdTypeStep }
        val task =
            OwnershipStructureTask(
                journeyStateService = mock(),
                ownershipTypeTask = ownershipTypeTask,
                ownershipStructureRoutingStep = mock(),
                freeholdersTask = mock(),
                superiorLandlordsTask = mock(),
            )

        assertEquals(expected, task.isJointFreeholder)
    }

    companion object {
        // freeholdType (null when the freehold type step is unreachable), expected isJointFreeholder
        @JvmStatic
        private fun provideFreeholdTypeScenarios() =
            arrayOf(
                arguments(FreeholdType.JOINT_TENANTS, true),
                arguments(FreeholdType.TENANTS_IN_COMMON, true),
                arguments(FreeholdType.SOLE_OWNERSHIP, false),
                arguments(FreeholdType.COMMONHOLD, false),
                arguments(null, false),
            )
    }
}
