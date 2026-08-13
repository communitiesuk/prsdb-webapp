package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.AnyMembers
import uk.gov.communities.prsdb.webapp.models.dataModels.GoverningBodyMemberDataModel

// Internal routing step that sits in the outer task/journey immediately after the reusable governing body members
// sub-journey. It reads the collected members (held by the outer task/journey via its own state) and reports whether
// any remain, so the outer journey can route forward when members exist or back to an earlier step once the list has
// been emptied. This replaces the back destination that used to be injected into the members task.
@JourneyFrameworkComponent
class GovBodyMembersBackRoutingStepConfig : AbstractInternalStepConfig<AnyMembers, JourneyState>() {
    private lateinit var membersList: () -> Map<Int, GoverningBodyMemberDataModel>?

    fun usingMembersList(membersList: () -> Map<Int, GoverningBodyMemberDataModel>?): GovBodyMembersBackRoutingStepConfig {
        this.membersList = membersList
        return this
    }

    override fun isSubClassInitialised() = ::membersList.isInitialized

    override fun mode(state: JourneyState): AnyMembers =
        if (membersList().isNullOrEmpty()) AnyMembers.NO_MEMBERS else AnyMembers.SOME_MEMBERS
}

@JourneyFrameworkComponent
class GovBodyMembersBackRoutingStep(
    stepConfig: GovBodyMembersBackRoutingStepConfig,
) : JourneyStep.InternalStep<AnyMembers, JourneyState>(stepConfig)
