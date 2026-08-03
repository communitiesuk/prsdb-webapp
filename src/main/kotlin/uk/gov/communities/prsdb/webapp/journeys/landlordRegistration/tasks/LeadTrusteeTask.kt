package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.DuplicableTask
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.LeadTrusteeState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeDobStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeEmailStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeNameStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteePhoneStep
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.TrusteeAddressTask

@JourneyFrameworkComponent
class LeadTrusteeTask(
    journeyStateService: JourneyStateService,
    override val leadTrusteeNameStep: LeadTrusteeNameStep,
    override val leadTrusteeDobStep: LeadTrusteeDobStep,
    override val leadTrusteeEmailStep: LeadTrusteeEmailStep,
    override val leadTrusteePhoneStep: LeadTrusteePhoneStep,
    override val trusteeAddressTask: TrusteeAddressTask,
) : DuplicableTask<LeadTrusteeState>(journeyStateService),
    LeadTrusteeState {
    override val taskState get() = this

    override fun makeSubJourney(state: LeadTrusteeState) =
        subJourney(state) {
            step(journey.leadTrusteeNameStep) {
                routeSegment(LeadTrusteeNameStep.ROUTE_SEGMENT)
                nextStep { journey.leadTrusteeDobStep }
            }
            step(journey.leadTrusteeDobStep) {
                routeSegment(LeadTrusteeDobStep.ROUTE_SEGMENT)
                parents { journey.leadTrusteeNameStep.isComplete() }
                nextStep { journey.leadTrusteeEmailStep }
            }
            step(journey.leadTrusteeEmailStep) {
                routeSegment(LeadTrusteeEmailStep.ROUTE_SEGMENT)
                parents { journey.leadTrusteeDobStep.isComplete() }
                nextStep { journey.leadTrusteePhoneStep }
            }
            step(journey.leadTrusteePhoneStep) {
                routeSegment(LeadTrusteePhoneStep.ROUTE_SEGMENT)
                parents { journey.leadTrusteeEmailStep.isComplete() }
                nextStep { journey.trusteeAddressTask.firstStep }
            }
            duplicableTask(journey.trusteeAddressTask, TrusteeAddressTask.ROUTE_SEGMENT) {
                parents { journey.leadTrusteePhoneStep.isComplete() }
                nextStep { exitStep }
            }
            exitStep {
                parents { journey.trusteeAddressTask.isComplete() }
            }
        }
}
