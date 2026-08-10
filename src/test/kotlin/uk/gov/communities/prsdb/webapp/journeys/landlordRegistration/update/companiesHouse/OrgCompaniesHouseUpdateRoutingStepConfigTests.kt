package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.companiesHouse

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCompanyStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCompanyStepConfig
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgIsRegisteredCompanyFormModel

class OrgCompaniesHouseUpdateRoutingStepConfigTests {
    @Nested
    inner class ModeTests {
        @Test
        fun `mode returns UNCHANGED when previous is registered and new answer is yes`() {
            val result = configuredConfig(true).mode(stateWith(YesOrNo.YES))

            assertEquals(OrgCompaniesHouseUpdateRouteMode.UNCHANGED, result)
        }

        @Test
        fun `mode returns UNCHANGED when previous is not registered and new answer is no`() {
            val result = configuredConfig(false).mode(stateWith(YesOrNo.NO))

            assertEquals(OrgCompaniesHouseUpdateRouteMode.UNCHANGED, result)
        }

        @Test
        fun `mode returns CHANGED_TO_COMPANY when previous is not registered and new answer is yes`() {
            val result = configuredConfig(false).mode(stateWith(YesOrNo.YES))

            assertEquals(OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_COMPANY, result)
        }

        @Test
        fun `mode returns CHANGED_TO_NON_COMPANY when previous is registered and new answer is no`() {
            val result = configuredConfig(true).mode(stateWith(YesOrNo.NO))

            assertEquals(OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_NON_COMPANY, result)
        }

        @Test
        fun `mode returns null without reading previous registration status when new answer is null`() {
            var previousIsRegisteredCompanyWasRead = false
            val config =
                OrgCompaniesHouseUpdateRoutingStepConfig().apply {
                    usingPreviousIsRegisteredCompany {
                        previousIsRegisteredCompanyWasRead = true
                        true
                    }
                }

            val result = config.mode(stateWith(null))

            assertNull(result)
            assertFalse(previousIsRegisteredCompanyWasRead)
        }
    }

    @Nested
    inner class PreviousRegistrationStatusTests {
        @Test
        fun `base journey lookup returns true when original answer is registered with companies house`() {
            val result = getPreviousIsRegisteredCompanyFromBaseJourney(true)

            assertTrue(result)
        }

        @Test
        fun `base journey lookup returns false when original answer is not registered with companies house`() {
            val result = getPreviousIsRegisteredCompanyFromBaseJourney(false)

            assertFalse(result)
        }

        @Test
        fun `base journey lookup returns false when original answer is unset`() {
            val result = getPreviousIsRegisteredCompanyFromBaseJourney(null)

            assertFalse(result)
        }
    }

    private fun configuredConfig(previousIsRegisteredCompany: Boolean) =
        OrgCompaniesHouseUpdateRoutingStepConfig().apply {
            usingPreviousIsRegisteredCompany { previousIsRegisteredCompany }
        }

    private fun stateWith(newAnswer: YesOrNo?): OrgCompaniesHouseUpdateState {
        val orgIsRegisteredCompanyStep = mock<OrgIsRegisteredCompanyStep> { on { outcome } doReturn newAnswer }
        return mock<OrgCompaniesHouseUpdateState> { on { this.orgIsRegisteredCompanyStep } doReturn orgIsRegisteredCompanyStep }
    }

    private fun getPreviousIsRegisteredCompanyFromBaseJourney(companiesHouse: Boolean?): Boolean {
        val baseState = mock<CheckYourAnswersJourneyState>()
        val childState = mock<CheckYourAnswersJourneyState> { on { getBaseJourneyState() } doReturn baseState }
        val formModel = mock<OrgIsRegisteredCompanyFormModel> { on { this.companiesHouse } doReturn companiesHouse }
        val stepConfig = mock<OrgIsRegisteredCompanyStepConfig> { on { getFormModelFromState(baseState) } doReturn formModel }

        return OrgCompaniesHouseUpdateRoutingStepConfig().getPreviousIsRegisteredCompanyFromBaseJourney(
            childState,
            OrgIsRegisteredCompanyStep(stepConfig),
        )
    }
}
