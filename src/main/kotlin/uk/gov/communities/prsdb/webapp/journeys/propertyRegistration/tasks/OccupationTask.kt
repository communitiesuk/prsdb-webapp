package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.OccupationState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BedroomsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FurnishedStatusStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OccupiedStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

@JourneyFrameworkComponent
class OccupationTask : Task<OccupationState>() {
    override fun makeSubJourney(state: OccupationState) =
        subJourney(state) {
            step(journey.occupied) {
                routeSegment(OccupiedStep.ROUTE_SEGMENT)
                nextStep { mode ->
                    when (mode) {
                        YesOrNo.YES -> journey.householdsAndTenantsTask.firstStep
                        YesOrNo.NO -> exitStep
                    }
                }
                savable()
            }
            task(journey.householdsAndTenantsTask) {
                parents { journey.occupied.hasOutcome(YesOrNo.YES) }
                nextStep { journey.bedrooms }
                savable()
            }
            step(journey.bedrooms) {
                routeSegment(BedroomsStep.ROUTE_SEGMENT)
                parents { journey.householdsAndTenantsTask.isComplete() }
                nextStep { journey.rentIncludesBillsTask.firstStep }
                savable()
            }
            task(journey.rentIncludesBillsTask) {
                parents { journey.bedrooms.hasOutcome(Complete.COMPLETE) }
                nextStep { journey.furnishedStatus }
            }
            step(journey.furnishedStatus) {
                routeSegment(FurnishedStatusStep.ROUTE_SEGMENT)
                parents { journey.rentIncludesBillsTask.isComplete() }
                nextStep { journey.rentFrequencyAndAmountTask.firstStep }
                savable()
            }
            task(journey.rentFrequencyAndAmountTask) {
                parents {
                    journey.furnishedStatus.hasOutcome(Complete.COMPLETE)
                }
                nextStep { exitStep }
                savable()
            }
            exitStep {
                savable()
                parents {
                    OrParents(
                        journey.rentFrequencyAndAmountTask.isComplete(),
                        journey.occupied.hasOutcome(YesOrNo.NO),
                    )
                }
            }
        }
}
