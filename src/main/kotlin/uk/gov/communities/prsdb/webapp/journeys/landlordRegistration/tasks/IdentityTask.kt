package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.IdentityState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.ConfirmIdentityStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.DateOfBirthStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.IdentityNotVerifiedStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.IdentityVerifiedMode
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.IdentityVerifyingStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.NameStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkable

@JourneyFrameworkComponent
class IdentityTask : Task<IdentityState>() {
    override fun makeSubJourney(state: IdentityState) =
        subJourney(state) {
            step(journey.identityVerifyingStep) {
                routeSegment(IdentityVerifyingStep.ROUTE_SEGMENT)
                nextStep { mode ->
                    when (mode) {
                        IdentityVerifiedMode.VERIFIED -> journey.confirmIdentityStep
                        IdentityVerifiedMode.NOT_VERIFIED -> journey.identityNotVerifiedStep
                    }
                }
            }
            step(journey.confirmIdentityStep) {
                routeSegment(ConfirmIdentityStep.ROUTE_SEGMENT)
                parents { journey.identityVerifyingStep.hasOutcome(IdentityVerifiedMode.VERIFIED) }
                nextStep { exitStep }
            }
            step(journey.identityNotVerifiedStep) {
                routeSegment(IdentityNotVerifiedStep.ROUTE_SEGMENT)
                parents { journey.identityVerifyingStep.hasOutcome(IdentityVerifiedMode.NOT_VERIFIED) }
                nextStep { journey.nameStep }
            }
            step(journey.nameStep) {
                routeSegment(NameStep.ROUTE_SEGMENT)
                parents { journey.identityNotVerifiedStep.isComplete() }
                nextStep { journey.dateOfBirthStep }
                checkable()
            }
            step(journey.dateOfBirthStep) {
                routeSegment(DateOfBirthStep.ROUTE_SEGMENT)
                parents { journey.nameStep.isComplete() }
                nextStep { exitStep }
                checkable()
            }
            exitStep {
                parents { OrParents(journey.confirmIdentityStep.isComplete(), journey.dateOfBirthStep.isComplete()) }
            }
        }
}
