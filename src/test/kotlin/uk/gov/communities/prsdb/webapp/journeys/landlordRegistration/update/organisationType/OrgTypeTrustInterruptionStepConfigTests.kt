package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.organisationType

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import uk.gov.communities.prsdb.webapp.constants.enums.OrgType
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTypeStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTypeStepConfig
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgTypeFormModel

class OrgTypeTrustInterruptionStepConfigTests {
    private val stepConfig = OrgTypeTrustInterruptionStepConfig()

    @Nested
    inner class GetStepSpecificContentTests {
        @Test
        fun `returns empty map when adding trust`() {
            val state = stateWith(OrgTypeUpdateRouteMode.ADDING_TRUST, listOf(OrgType.COMPANY, OrgType.TRUST))

            val content = stepConfig.getStepSpecificContent(state)

            assertTrue(content.isEmpty())
        }

        @Test
        @Suppress("UNCHECKED_CAST")
        fun `includes selectedOrgTypeLabelKeys excluding trust when removing trust`() {
            val state = stateWith(OrgTypeUpdateRouteMode.REMOVING_TRUST, listOf(OrgType.COMPANY, OrgType.CHARITY))

            val content = stepConfig.getStepSpecificContent(state)

            val labelKeys = content["selectedOrgTypeLabelKeys"] as List<String>
            assertEquals(
                listOf(
                    "registerAsALandlord.orgTypeTrustInterruption.orgType.company",
                    "registerAsALandlord.orgTypeTrustInterruption.orgType.charity",
                ),
                labelKeys,
            )
        }

        @Test
        fun `throws when TRUST is in selectedOrgTypes during REMOVING_TRUST`() {
            val state = stateWith(OrgTypeUpdateRouteMode.REMOVING_TRUST, listOf(OrgType.COMPANY, OrgType.TRUST))

            assertThrows<PrsdbWebException> { stepConfig.getStepSpecificContent(state) }
        }
    }

    @Nested
    inner class ChooseTemplateTests {
        @Test
        fun `returns adding trust template when routing outcome is ADDING_TRUST`() {
            val state = stateWith(OrgTypeUpdateRouteMode.ADDING_TRUST, listOf(OrgType.TRUST))

            assertEquals("forms/orgTypeAddingTrustInterruptionForm", stepConfig.chooseTemplate(state))
        }

        @Test
        fun `returns removing trust template when routing outcome is REMOVING_TRUST`() {
            val state = stateWith(OrgTypeUpdateRouteMode.REMOVING_TRUST, listOf(OrgType.COMPANY))

            assertEquals("forms/orgTypeRemovingTrustInterruptionForm", stepConfig.chooseTemplate(state))
        }
    }

    private fun stateWith(
        routeMode: OrgTypeUpdateRouteMode,
        selectedOrgTypes: List<OrgType>,
    ): OrgTypeUpdateState {
        val formModel = mock<OrgTypeFormModel> { on { getSelectedOrgTypes() } doReturn selectedOrgTypes }
        val orgTypeStepConfig = mock<OrgTypeStepConfig> { on { getFormModelFromState(org.mockito.kotlin.any()) } doReturn formModel }
        val orgTypeStep = mock<OrgTypeStep> { on { this.stepConfig } doReturn orgTypeStepConfig }
        val routingStep = mock<OrgTypeUpdateRoutingStep> { on { outcome } doReturn routeMode }
        return mock<OrgTypeUpdateState> {
            on { this.orgTypeStep } doReturn orgTypeStep
            on { this.orgTypeUpdateRoutingStep } doReturn routingStep
        }
    }
}
