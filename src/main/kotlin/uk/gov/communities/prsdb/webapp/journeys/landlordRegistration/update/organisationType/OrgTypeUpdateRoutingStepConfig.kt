package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.organisationType

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.InternalStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTypeMode

enum class OrgTypeUpdateRouteMode {
    TRUST_UNCHANGED,
    ADDING_TRUST,
    REMOVING_TRUST,
}

@JourneyFrameworkComponent
class OrgTypeUpdateRoutingStepConfig : AbstractInternalStepConfig<OrgTypeUpdateRouteMode, OrgTypeUpdateState>() {
    override fun mode(state: OrgTypeUpdateState): OrgTypeUpdateRouteMode? {
        val previousIsTrust = state.previousOrgTypeMode == OrgTypeMode.INCLUDES_TRUST
        val newOrgTypeMode = state.orgTypeStep.outcome ?: return null
        val newIsTrust = newOrgTypeMode == OrgTypeMode.INCLUDES_TRUST
        return when {
            previousIsTrust == newIsTrust -> OrgTypeUpdateRouteMode.TRUST_UNCHANGED
            newIsTrust -> OrgTypeUpdateRouteMode.ADDING_TRUST
            else -> OrgTypeUpdateRouteMode.REMOVING_TRUST
        }
    }
}

@JourneyFrameworkComponent
class OrgTypeUpdateRoutingStep(
    stepConfig: OrgTypeUpdateRoutingStepConfig,
) : InternalStep<OrgTypeUpdateRouteMode, OrgTypeUpdateState>(stepConfig)
