package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.organisationType

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTypeMode
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTypeStep

class OrgTypeUpdateRoutingStepConfigTests {
    @Nested
    inner class ModeTests {
        @Test
        fun `mode returns TRUST_UNCHANGED when previous is trust and new includes trust`() {
            val result =
                OrgTypeUpdateRoutingStepConfig().mode(
                    stateWith(previousOrgTypeMode = OrgTypeMode.INCLUDES_TRUST, newOrgTypeMode = OrgTypeMode.INCLUDES_TRUST),
                )

            assertEquals(OrgTypeUpdateRouteMode.TRUST_UNCHANGED, result)
        }

        @Test
        fun `mode returns TRUST_UNCHANGED when previous is not trust and new excludes trust`() {
            val result =
                OrgTypeUpdateRoutingStepConfig().mode(
                    stateWith(previousOrgTypeMode = OrgTypeMode.EXCLUDES_TRUST, newOrgTypeMode = OrgTypeMode.EXCLUDES_TRUST),
                )

            assertEquals(OrgTypeUpdateRouteMode.TRUST_UNCHANGED, result)
        }

        @Test
        fun `mode returns ADDING_TRUST when previous is not trust and new includes trust`() {
            val result =
                OrgTypeUpdateRoutingStepConfig().mode(
                    stateWith(previousOrgTypeMode = OrgTypeMode.EXCLUDES_TRUST, newOrgTypeMode = OrgTypeMode.INCLUDES_TRUST),
                )

            assertEquals(OrgTypeUpdateRouteMode.ADDING_TRUST, result)
        }

        @Test
        fun `mode returns REMOVING_TRUST when previous is trust and new excludes trust`() {
            val result =
                OrgTypeUpdateRoutingStepConfig().mode(
                    stateWith(previousOrgTypeMode = OrgTypeMode.INCLUDES_TRUST, newOrgTypeMode = OrgTypeMode.EXCLUDES_TRUST),
                )

            assertEquals(OrgTypeUpdateRouteMode.REMOVING_TRUST, result)
        }

        @Test
        fun `mode returns null when new org type mode is null`() {
            val result =
                OrgTypeUpdateRoutingStepConfig().mode(
                    stateWith(previousOrgTypeMode = OrgTypeMode.INCLUDES_TRUST, newOrgTypeMode = null),
                )

            assertNull(result)
        }
    }

    private fun stateWith(
        previousOrgTypeMode: OrgTypeMode,
        newOrgTypeMode: OrgTypeMode?,
    ): OrgTypeUpdateState {
        val mockOrgTypeStep = mock<OrgTypeStep> { on { outcome } doReturn newOrgTypeMode }
        return mock<OrgTypeUpdateState> {
            on { this.previousOrgTypeMode } doReturn previousOrgTypeMode
            on { orgTypeStep } doReturn mockOrgTypeStep
        }
    }
}
