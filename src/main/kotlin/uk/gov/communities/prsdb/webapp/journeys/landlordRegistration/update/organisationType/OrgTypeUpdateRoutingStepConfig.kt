package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.organisationType

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.OrgType
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.InternalStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTypeMode
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTypeStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService

enum class OrgTypeUpdateRouteMode {
    TRUST_UNCHANGED,
    ADDING_TRUST,
    REMOVING_TRUST,
}

@JourneyFrameworkComponent
class OrgTypeUpdateRoutingStepConfig : AbstractInternalStepConfig<OrgTypeUpdateRouteMode, OrgTypeUpdateState>() {
    private lateinit var previousIsTrust: () -> Boolean

    fun usingPreviousIsTrust(previousIsTrust: () -> Boolean): OrgTypeUpdateRoutingStepConfig {
        this.previousIsTrust = previousIsTrust
        return this
    }

    fun getPreviousIsTrustFromDatabase(userToLandlordService: UserToLandlordService): Boolean =
        userToLandlordService.getCurrentOrganisationLandlordForUser().isTrust

    fun getPreviousIsTrustFromBaseJourney(
        state: CheckYourAnswersJourneyState,
        orgTypeStep: OrgTypeStep,
    ): Boolean =
        OrgType.TRUST in
            orgTypeStep.stepConfig
                .getFormModelFromState(state.getBaseJourneyState())
                .getSelectedOrgTypes()

    override fun isSubClassInitialised() = ::previousIsTrust.isInitialized

    override fun mode(state: OrgTypeUpdateState): OrgTypeUpdateRouteMode? {
        val newIsTrust = state.orgTypeStep.outcome?.let { it == OrgTypeMode.INCLUDES_TRUST } ?: return null
        return when {
            previousIsTrust() == newIsTrust -> OrgTypeUpdateRouteMode.TRUST_UNCHANGED
            newIsTrust -> OrgTypeUpdateRouteMode.ADDING_TRUST
            else -> OrgTypeUpdateRouteMode.REMOVING_TRUST
        }
    }
}

@JourneyFrameworkComponent
class OrgTypeUpdateRoutingStep(
    stepConfig: OrgTypeUpdateRoutingStepConfig,
) : InternalStep<OrgTypeUpdateRouteMode, OrgTypeUpdateState>(stepConfig)
