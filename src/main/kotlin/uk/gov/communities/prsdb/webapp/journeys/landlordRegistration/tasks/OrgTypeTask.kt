package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.DuplicableTask
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.OrgTypeState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTypeMode
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTypeStep

@JourneyFrameworkComponent
class OrgTypeTask(
    journeyStateService: JourneyStateService,
    override val orgTypeStep: OrgTypeStep,
    override val leadTrusteeTask: LeadTrusteeTask,
) : DuplicableTask<OrgTypeState>(journeyStateService),
    OrgTypeState {
    override val taskState get() = this

    override fun makeSubJourney(state: OrgTypeState) =
        subJourney(state) {
            step(journey.orgTypeStep) {
                routeSegment(OrgTypeStep.ROUTE_SEGMENT)
                nextStep { mode ->
                    when (mode) {
                        OrgTypeMode.INCLUDES_TRUST -> journey.leadTrusteeTask.firstStep
                        OrgTypeMode.EXCLUDES_TRUST -> exitStep
                    }
                }
            }
            duplicableTask(journey.leadTrusteeTask) {
                parents { journey.orgTypeStep.hasOutcome(OrgTypeMode.INCLUDES_TRUST) }
                nextStep { exitStep }
            }
            exitStep {
                parents {
                    OrParents(
                        journey.orgTypeStep.hasOutcome(OrgTypeMode.EXCLUDES_TRUST),
                        journey.leadTrusteeTask.isComplete(),
                    )
                }
            }
        }
}
