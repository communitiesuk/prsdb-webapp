package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.companiesHouse.OrgCompaniesHouseUpdateRouteMode
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.companiesHouse.OrgCompaniesHouseUpdateRoutingStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.companiesHouse.OrgCompaniesHouseUpdateState

class OrgCompaniesHouseInterruptionStepConfigTests {
    @Nested
    inner class ChooseTemplateTests {
        @Test
        fun `returns changing to company template when routing outcome is CHANGED_TO_COMPANY`() {
            val state = stateWithRoutingOutcome(OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_COMPANY)
            val config = OrgCompaniesHouseInterruptionStepConfig()

            assertEquals("forms/orgCompaniesHouseChangingToCompanyInterruptionForm", config.chooseTemplate(state))
        }

        @Test
        fun `returns changing to non-company template when routing outcome is CHANGED_TO_NON_COMPANY`() {
            val state = stateWithRoutingOutcome(OrgCompaniesHouseUpdateRouteMode.CHANGED_TO_NON_COMPANY)
            val config = OrgCompaniesHouseInterruptionStepConfig()

            assertEquals("forms/orgCompaniesHouseChangingToNonCompanyInterruptionForm", config.chooseTemplate(state))
        }
    }

    private fun stateWithRoutingOutcome(outcome: OrgCompaniesHouseUpdateRouteMode): OrgCompaniesHouseUpdateState {
        val routingStep =
            mock<OrgCompaniesHouseUpdateRoutingStep> {
                on { this.outcome } doReturn outcome
            }
        return mock {
            on { orgCompaniesHouseUpdateRoutingStep } doReturn routingStep
        }
    }
}
