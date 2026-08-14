package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.organisationType

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.OrgType
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class OrgTypeTrustInterruptionStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, OrgTypeUpdateState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: OrgTypeUpdateState): Map<String, Any> {
        val isAddingTrust = state.orgTypeUpdateRoutingStep.outcome == OrgTypeUpdateRouteMode.ADDING_TRUST
        if (isAddingTrust) return emptyMap()

        val selectedOrgTypes =
            state.orgTypeStep.stepConfig
                .getFormModelFromState(state)
                .getSelectedOrgTypes()

        return mapOf("selectedOrgTypeLabelKeys" to selectedOrgTypes.map { orgTypeToLabelKey(it) })
    }

    private fun orgTypeToLabelKey(orgType: OrgType): String =
        when (orgType) {
            OrgType.COMPANY -> "registerAsALandlord.orgTypeTrustInterruption.orgType.company"
            OrgType.CHARITY -> "registerAsALandlord.orgTypeTrustInterruption.orgType.charity"
            OrgType.NONE -> "registerAsALandlord.orgTypeTrustInterruption.orgType.none"
            OrgType.TRUST -> throw PrsdbWebException("Trust is not a valid org type for the removing trust interruption page")
        }

    override fun chooseTemplate(state: OrgTypeUpdateState): String {
        val isAddingTrust = state.orgTypeUpdateRoutingStep.outcome == OrgTypeUpdateRouteMode.ADDING_TRUST
        return if (isAddingTrust) {
            "forms/orgTypeAddingTrustInterruptionForm"
        } else {
            "forms/orgTypeRemovingTrustInterruptionForm"
        }
    }

    override fun mode(state: OrgTypeUpdateState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class OrgTypeTrustInterruptionStep(
    stepConfig: OrgTypeTrustInterruptionStepConfig,
) : RequestableStep<Complete, NoInputFormModel, OrgTypeUpdateState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "organisation-type-trust-interruption"
    }
}
