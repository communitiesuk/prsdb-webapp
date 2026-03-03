package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.HouseholdsAndTenantsState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete

@JourneyFrameworkComponent
class HouseholdsAndTenantsTask : Task<HouseholdsAndTenantsState>() {
    override fun makeSubJourney(state: HouseholdsAndTenantsState) =
        subJourney(state) {
            step(journey.households) {
                routeSegment("number-of-households")
                nextStep { journey.tenants }
                savable()
            }
            step(journey.tenants) {
                routeSegment("number-of-people")
                parents { journey.households.hasOutcome(Complete.COMPLETE) }
                nextStep { exitStep }
                savable()
            }
            exitStep {
                parents {
                    OrParents(
                        journey.tenants.hasOutcome(Complete.COMPLETE),
                    )
                }
            }
        }
}
