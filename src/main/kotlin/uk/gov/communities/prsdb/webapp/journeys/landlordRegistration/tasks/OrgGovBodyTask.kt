package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.TaskWithoutDependencies
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.OrgGovBodyMembersDependencies
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.OrgGovBodyState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyDetailsStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgGovBodyMustProvideInfoStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgGovBodyDetailsMode

@JourneyFrameworkComponent
class OrgGovBodyTask(
    journeyStateService: JourneyStateService,
    override val orgGovBodyDetailsStep: OrgGovBodyDetailsStep,
    override val orgGovBodyMustProvideInfoStep: OrgGovBodyMustProvideInfoStep,
    override val orgGovBodyMembersTask: OrgGovBodyMembersTask,
) : TaskWithoutDependencies<OrgGovBodyState>(journeyStateService),
    OrgGovBodyState {
    override val taskState get() = this

    override fun makeSubJourney(state: OrgGovBodyState) =
        subJourney(state) {
            step(journey.orgGovBodyDetailsStep) {
                routeSegment(OrgGovBodyDetailsStep.ROUTE_SEGMENT)
                nextDestination { mode ->
                    when (mode) {
                        OrgGovBodyDetailsMode.HAS_DETAILS -> Destination(journey.orgGovBodyMembersTask.firstStep)
                        OrgGovBodyDetailsMode.NO_DETAILS -> Destination(journey.orgGovBodyMustProvideInfoStep)
                    }
                }
            }
            step(journey.orgGovBodyMustProvideInfoStep) {
                routeSegment(OrgGovBodyMustProvideInfoStep.ROUTE_SEGMENT)
                parents { journey.orgGovBodyDetailsStep.hasOutcome(OrgGovBodyDetailsMode.NO_DETAILS) }
                noNextDestination()
            }
            task(journey.orgGovBodyMembersTask) {
                parents { journey.orgGovBodyDetailsStep.hasOutcome(OrgGovBodyDetailsMode.HAS_DETAILS) }
                nextStep { exitStep }
                withDependencies {
                    OrgGovBodyMembersDependencies(
                        whoToProvideEmptyBackDestination = { Destination(journey.orgGovBodyDetailsStep) },
                        removeLastMemberDestination = { Destination(journey.orgGovBodyDetailsStep) },
                    )
                }
            }
            exitStep {
                parents { journey.orgGovBodyMembersTask.isComplete() }
            }
        }
}
