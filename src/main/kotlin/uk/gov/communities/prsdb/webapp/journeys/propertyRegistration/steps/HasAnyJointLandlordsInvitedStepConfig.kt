package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.AnyLandlordsInvited
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.InviteJointLandlordPropertyRegistrationState

@JourneyFrameworkComponent
class HasAnyJointLandlordsInvitedStepConfig :
    AbstractInternalStepConfig<AnyLandlordsInvited, InviteJointLandlordPropertyRegistrationState>() {
    override fun mode(state: InviteJointLandlordPropertyRegistrationState) =
        if (state.invitedJointLandlords.isNotEmpty()) AnyLandlordsInvited.SOME_LANDLORDS else AnyLandlordsInvited.NO_LANDLORDS
}

@JourneyFrameworkComponent
final class HasAnyJointLandlordsInvitedStep(
    stepConfig: HasAnyJointLandlordsInvitedStepConfig,
) : JourneyStep.InternalStep<AnyLandlordsInvited, InviteJointLandlordPropertyRegistrationState>(stepConfig)
