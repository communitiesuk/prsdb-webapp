package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.RentFrequencyAndAmountState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentAmountStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentFrequencyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete

@JourneyFrameworkComponent
class RentFrequencyAndAmountTask : Task<RentFrequencyAndAmountState>() {
    override fun makeSubJourney(state: RentFrequencyAndAmountState) =
        subJourney(state) {
            step(journey.rentFrequency) {
                routeSegment(RentFrequencyStep.ROUTE_SEGMENT)
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
                parents {
                    journey.rentAmount.hasOutcome(Complete.COMPLETE)
                }
            }
        }
}
