package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.SubjourneyComplete
import uk.gov.communities.prsdb.webapp.journeys.UnrecoverableJourneyStateException
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.LandlordRegistrationState

@JourneyFrameworkComponent
class LandlordTypeChangeRedirectStepConfig :
    AbstractInternalStepConfig<LandlordTypeChangeDestination, LandlordRegistrationState>() {
    override fun mode(state: LandlordRegistrationState): LandlordTypeChangeDestination {
        val (selectedTask, taskDestination) =
            when (state.landlordTypeStep.outcome) {
                LandlordTypeMode.ORGANISATION ->
                    state.orgLandlordRegistrationTask to LandlordTypeChangeDestination.ORGANISATION_TASK
                LandlordTypeMode.INDIVIDUAL ->
                    state.individualLandlordLocationTask to LandlordTypeChangeDestination.INDIVIDUAL_TASK
                null -> throw UnrecoverableJourneyStateException(
                    state.journeyId,
                    "Cannot determine landlord type change destination: no landlord type has been selected",
                )
            }
        val landlordTypeUnchanged =
            state.getStepData(LandlordTypeStep.ROUTE_SEGMENT) ==
                state.getBaseJourneyState().getStepData(LandlordTypeStep.ROUTE_SEGMENT)
        val selectedTaskComplete = selectedTask.exitStep.outcome == SubjourneyComplete.COMPLETE
        return if (landlordTypeUnchanged && selectedTaskComplete) {
            LandlordTypeChangeDestination.CHECK_ANSWERS
        } else {
            taskDestination
        }
    }
}

@JourneyFrameworkComponent
final class LandlordTypeChangeRedirectStep(
    stepConfig: LandlordTypeChangeRedirectStepConfig,
) : JourneyStep.InternalStep<LandlordTypeChangeDestination, LandlordRegistrationState>(stepConfig)

enum class LandlordTypeChangeDestination {
    CHECK_ANSWERS,
    INDIVIDUAL_TASK,
    ORGANISATION_TASK,
}
