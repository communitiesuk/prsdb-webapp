package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.TaskWithoutDependencies
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.WhoProvidesDetailsState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LettingAgentEmailStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.WhoProvidesRentalDetailsMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.WhoProvidesRentalDetailsStep

@JourneyFrameworkComponent
class WhoProvidesDetailsTask(
    journeyStateService: JourneyStateService,
    override val whoProvidesRentalDetailsStep: WhoProvidesRentalDetailsStep,
    override val lettingAgentEmailStep: LettingAgentEmailStep,
) : TaskWithoutDependencies<WhoProvidesDetailsState>(journeyStateService),
    WhoProvidesDetailsState {
    override val taskState get() = this

    override fun makeSubJourney(state: WhoProvidesDetailsState) =
        subJourney(state) {
            step(journey.whoProvidesRentalDetailsStep) {
                routeSegment(WhoProvidesRentalDetailsStep.ROUTE_SEGMENT)
                nextStep { mode ->
                    when (mode) {
                        WhoProvidesRentalDetailsMode.LANDLORD_PROVIDES -> exitStep
                        WhoProvidesRentalDetailsMode.LETTING_AGENT_PROVIDES -> journey.lettingAgentEmailStep
                    }
                }
                savable()
            }
            step(journey.lettingAgentEmailStep) {
                routeSegment(LettingAgentEmailStep.ROUTE_SEGMENT)
                parents { journey.whoProvidesRentalDetailsStep.hasOutcome(WhoProvidesRentalDetailsMode.LETTING_AGENT_PROVIDES) }
                nextStep { exitStep }
                savable()
            }
            exitStep {
                parents {
                    OrParents(
                        journey.whoProvidesRentalDetailsStep.hasOutcome(WhoProvidesRentalDetailsMode.LANDLORD_PROVIDES),
                        journey.lettingAgentEmailStep.isComplete(),
                    )
                }
            }
        }
}
