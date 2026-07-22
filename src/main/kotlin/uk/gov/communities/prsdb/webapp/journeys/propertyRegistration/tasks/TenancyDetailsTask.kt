package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.TenancyDetailsState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FurnishedStatusStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HouseholdMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideTenancyDetailsLaterStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete

@JourneyFrameworkComponent
class TenancyDetailsTask : Task<TenancyDetailsState>() {
    override fun makeSubJourney(state: TenancyDetailsState) =
        subJourney(state) {
            task(journey.householdsAndTenantsTask) {
                nextStep {
                    if (state.households.outcome == HouseholdMode.PROVIDE_THIS_LATER) {
                        exitStep
                    } else {
                        journey.rentIncludesBillsTask.firstStep
                    }
                }
                savable()
            }
            task(journey.rentIncludesBillsTask) {
                parents { journey.householdsAndTenantsTask.isComplete() }
                nextStep { journey.furnishedStatus }
                savable()
            }
            step(journey.furnishedStatus) {
                routeSegment(FurnishedStatusStep.ROUTE_SEGMENT)
                parents { journey.rentIncludesBillsTask.isComplete() }
                nextStep { journey.rentFrequencyAndAmountTask.firstStep }
                savable()
            }
            task(journey.rentFrequencyAndAmountTask) {
                parents { journey.furnishedStatus.hasOutcome(Complete.COMPLETE) }
                nextStep { exitStep }
                savable()
            }
            exitStep {
                savable()
                parents {
                    OrParents(
                        journey.rentFrequencyAndAmountTask.isComplete(),
                        journey.provideTenancyDetailsLaterStep.isComplete(),
                    )
                }
            }
        }
}
