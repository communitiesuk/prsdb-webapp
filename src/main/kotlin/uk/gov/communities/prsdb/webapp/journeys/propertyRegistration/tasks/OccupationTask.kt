package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.OccupationState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BillsIncludedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OccupiedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentAmountStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentFrequencyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentIncludesBillsStep
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
                nextStep { journey.bedroomsTask.firstStep }
                savable()
            }
            task(journey.bedroomsTask) {
                parents { journey.householdsAndTenantsTask.isComplete() }
                nextStep { journey.rentIncludesBills }
                savable()
            }
            step(journey.rentIncludesBills) {
                routeSegment(RentIncludesBillsStep.ROUTE_SEGMENT)
                parents { journey.bedroomsTask.isComplete() }
                nextStep { mode ->
                    when (mode) {
                        YesOrNo.YES -> journey.billsIncluded
                        YesOrNo.NO -> journey.furnishedStatusTask.firstStep
                    }
                }
                savable()
            }
            step(journey.billsIncluded) {
                routeSegment(BillsIncludedStep.ROUTE_SEGMENT)
                parents { journey.rentIncludesBills.hasOutcome(YesOrNo.YES) }
                nextStep { journey.furnishedStatusTask.firstStep }
            }
            task(journey.furnishedStatusTask) {
                parents {
                    OrParents(
                        journey.billsIncluded.hasOutcome(Complete.COMPLETE),
                        journey.rentIncludesBills.hasOutcome(YesOrNo.NO),
                    )
                }
                nextStep { journey.rentFrequency }
                savable()
            }
            step(journey.rentFrequency) {
                routeSegment(RentFrequencyStep.ROUTE_SEGMENT)
                parents {
                    journey.furnishedStatusTask.isComplete()
                }
                nextStep { journey.rentAmount }
                savable()
            }
            step(journey.rentAmount) {
                routeSegment(RentAmountStep.ROUTE_SEGMENT)
                parents { journey.rentFrequency.hasOutcome(Complete.COMPLETE) }
                nextStep { exitStep }
                savable()
            }
            exitStep {
                savable()
                parents {
                    OrParents(
                        journey.rentAmount.hasOutcome(Complete.COMPLETE),
                        journey.occupied.hasOutcome(YesOrNo.NO),
                    )
                }
            }
        }
}
