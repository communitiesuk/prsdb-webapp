package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.DuplicableTask
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.HouseholdsAndTenantsState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HouseholdStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.TenantsStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete

@JourneyFrameworkComponent
class HouseholdsAndTenantsTask(
    journeyStateService: JourneyStateService,
    override val households: HouseholdStep,
    override val tenants: TenantsStep,
) : DuplicableTask<HouseholdsAndTenantsState>(journeyStateService),
    HouseholdsAndTenantsState {
    override val taskState get() = this

    override fun makeSubJourney(state: HouseholdsAndTenantsState) =
        subJourney(state) {
            step(journey.households) {
                routeSegment(HouseholdStep.ROUTE_SEGMENT)
                nextStep { journey.tenants }
                savable()
            }
            step(journey.tenants) {
                routeSegment(TenantsStep.ROUTE_SEGMENT)
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
