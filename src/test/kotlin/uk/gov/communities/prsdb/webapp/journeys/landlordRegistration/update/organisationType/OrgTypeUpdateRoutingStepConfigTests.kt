package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.organisationType

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import uk.gov.communities.prsdb.webapp.constants.enums.OrgType
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTypeMode
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTypeStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTypeStepConfig
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgTypeFormModel

class OrgTypeUpdateRoutingStepConfigTests {
    @Nested
    inner class ModeTests {
        @Test
        fun `mode returns TRUST_UNCHANGED when previous is trust and new includes trust`() {
            val result = configuredConfig(true).mode(stateWith(OrgTypeMode.INCLUDES_TRUST))

            assertEquals(OrgTypeUpdateRouteMode.TRUST_UNCHANGED, result)
        }

        @Test
        fun `mode returns TRUST_UNCHANGED when previous is not trust and new excludes trust`() {
            val result = configuredConfig(false).mode(stateWith(OrgTypeMode.EXCLUDES_TRUST))

            assertEquals(OrgTypeUpdateRouteMode.TRUST_UNCHANGED, result)
        }

        @Test
        fun `mode returns ADDING_TRUST when previous is not trust and new includes trust`() {
            val result = configuredConfig(false).mode(stateWith(OrgTypeMode.INCLUDES_TRUST))

            assertEquals(OrgTypeUpdateRouteMode.ADDING_TRUST, result)
        }

        @Test
        fun `mode returns REMOVING_TRUST when previous is trust and new excludes trust`() {
            val result = configuredConfig(true).mode(stateWith(OrgTypeMode.EXCLUDES_TRUST))

            assertEquals(OrgTypeUpdateRouteMode.REMOVING_TRUST, result)
        }

        @Test
        fun `mode returns null without reading previous trust status when new org type mode is null`() {
            var previousIsTrustWasRead = false
            val config =
                OrgTypeUpdateRoutingStepConfig().apply {
                    usingPreviousIsTrust {
                        previousIsTrustWasRead = true
                        true
                    }
                }

            val result = config.mode(stateWith(null))

            assertNull(result)
            assertFalse(previousIsTrustWasRead)
        }
    }

    @Nested
    inner class PreviousTrustStatusTests {
        @Test
        fun `base journey lookup returns true when original organisation type includes trust`() {
            val result = getPreviousIsTrustFromBaseJourney(listOf(OrgType.COMPANY, OrgType.TRUST))

            assertTrue(result)
        }

        @Test
        fun `base journey lookup returns false when original organisation type excludes trust`() {
            val result = getPreviousIsTrustFromBaseJourney(listOf(OrgType.CHARITY))

            assertFalse(result)
        }
    }

    private fun configuredConfig(previousIsTrust: Boolean) =
        OrgTypeUpdateRoutingStepConfig().apply {
            usingPreviousIsTrust { previousIsTrust }
        }

    private fun stateWith(newOrgTypeMode: OrgTypeMode?): OrgTypeUpdateState {
        val orgTypeStep = mock<OrgTypeStep> { on { outcome } doReturn newOrgTypeMode }
        return mock<OrgTypeUpdateState> { on { this.orgTypeStep } doReturn orgTypeStep }
    }

    private fun getPreviousIsTrustFromBaseJourney(selectedOrgTypes: List<OrgType>): Boolean {
        val baseState = mock<CheckYourAnswersJourneyState>()
        val childState = mock<CheckYourAnswersJourneyState> { on { getBaseJourneyState() } doReturn baseState }
        val formModel = mock<OrgTypeFormModel> { on { getSelectedOrgTypes() } doReturn selectedOrgTypes }
        val stepConfig = mock<OrgTypeStepConfig> { on { getFormModelFromState(baseState) } doReturn formModel }

        return OrgTypeUpdateRoutingStepConfig().getPreviousIsTrustFromBaseJourney(
            childState,
            OrgTypeStep(stepConfig),
        )
    }
}
