package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.LandlordRegistrationOrgLandlordState
import uk.gov.communities.prsdb.webapp.journeys.shared.AnyMembers

@JourneyFrameworkComponent
class HasAnyGovBodyMembersStepConfig : AbstractInternalStepConfig<AnyMembers, LandlordRegistrationOrgLandlordState>() {
    override fun mode(state: LandlordRegistrationOrgLandlordState) =
        if (state.governingBodyMembersMap.isNullOrEmpty()) AnyMembers.NO_MEMBERS else AnyMembers.SOME_MEMBERS
}

@JourneyFrameworkComponent
final class HasAnyGovBodyMembersStep(
    stepConfig: HasAnyGovBodyMembersStepConfig,
) : JourneyStep.InternalStep<AnyMembers, LandlordRegistrationOrgLandlordState>(stepConfig)
