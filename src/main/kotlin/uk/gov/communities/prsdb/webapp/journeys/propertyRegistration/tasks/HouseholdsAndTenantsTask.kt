package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.HouseholdsAndTenantsState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HouseholdStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HouseholdStepMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideHouseholdDetailsLaterStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.TenantsStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete

@JourneyFrameworkComponent
class HouseholdsAndTenantsTask : Task<HouseholdsAndTenantsState>() {
    override fun makeSubJourney(state: HouseholdsAndTenantsState) =
        subJourney(state) {
            step(journey.households) {
                routeSegment(HouseholdStep.ROUTE_SEGMENT)
                nextStep { mode ->
                    when (mode) {
                        HouseholdStepMode.COMPLETE -> journey.tenants
                        HouseholdStepMode.PROVIDE_THIS_LATER -> journey.provideHouseholdDetailsLaterStep
                    }
                }
                savable()
            }
            step(journey.tenants) {
                routeSegment(TenantsStep.ROUTE_SEGMENT)
                parents { journey.households.hasOutcome(HouseholdStepMode.COMPLETE) }
                nextStep { exitStep }
                savable()
            }
            step(journey.provideHouseholdDetailsLaterStep) {
                routeSegment(ProvideHouseholdDetailsLaterStep.ROUTE_SEGMENT)
                parents { journey.households.hasOutcome(HouseholdStepMode.PROVIDE_THIS_LATER) }
                nextStep { exitStep }
                savable()
            }
            exitStep {
                parents {
                    OrParents(
                        journey.provideHouseholdDetailsLaterStep.isComplete(),
                        journey.tenants.hasOutcome(Complete.COMPLETE),
                    )
                }
            }
        }
}
