package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.organisationType

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.constants.enums.OrgType
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTypeStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTypeStepConfig
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgTypeFormModel
import java.util.Locale

class OrgTypeTrustInterruptionStepConfigTests {
    private val mockMessageSource =
        mock<MessageSource> {
            on {
                getMessage(
                    eq("registerAsALandlord.orgTypeTrustInterruption.orgType.company"),
                    eq(null),
                    eq(Locale.getDefault()),
                )
            } doReturn
                "company"
            on {
                getMessage(
                    eq("registerAsALandlord.orgTypeTrustInterruption.orgType.charity"),
                    eq(null),
                    eq(Locale.getDefault()),
                )
            } doReturn
                "charity"
            on { getMessage(eq("registerAsALandlord.orgTypeTrustInterruption.orgType.none"), eq(null), eq(Locale.getDefault())) } doReturn
                "none of these"
        }
    private val stepConfig = OrgTypeTrustInterruptionStepConfig(mockMessageSource)

    @Nested
    inner class GetStepSpecificContentTests {
        @Test
        fun `returns empty map when adding trust`() {
            val state = stateWith(OrgTypeUpdateRouteMode.ADDING_TRUST, listOf(OrgType.COMPANY, OrgType.TRUST))

            val content = stepConfig.getStepSpecificContent(state)

            assertTrue(content.isEmpty())
        }

        @Test
        fun `includes selectedOrgTypeLabels excluding trust when removing trust`() {
            val state = stateWith(OrgTypeUpdateRouteMode.REMOVING_TRUST, listOf(OrgType.COMPANY, OrgType.CHARITY))

            val content = stepConfig.getStepSpecificContent(state)

            assertEquals("company, charity", content["selectedOrgTypeLabels"])
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
