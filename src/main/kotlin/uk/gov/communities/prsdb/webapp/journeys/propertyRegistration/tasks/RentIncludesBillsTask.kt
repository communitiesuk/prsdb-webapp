package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.RentIncludesBillsState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BillsIncludedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentIncludesBillsStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

@JourneyFrameworkComponent
class RentIncludesBillsTask : Task<RentIncludesBillsState>() {
    override fun makeSubJourney(state: RentIncludesBillsState) =
        subJourney(state) {
            step(journey.rentIncludesBills) {
                routeSegment(RentIncludesBillsStep.ROUTE_SEGMENT)
                nextStep { mode ->
                    when (mode) {
                        YesOrNo.YES -> journey.billsIncluded
                        YesOrNo.NO -> exitStep
                    }
                }
                savable()
            }
            step(journey.billsIncluded) {
                routeSegment(BillsIncludedStep.ROUTE_SEGMENT)
                parents { journey.rentIncludesBills.hasOutcome(YesOrNo.YES) }
                nextStep { exitStep }
                savable()
            }
            exitStep {
                parents {
                    OrParents(
                        journey.rentIncludesBills.hasOutcome(YesOrNo.NO),
                        journey.billsIncluded.hasOutcome(Complete.COMPLETE),
                    )
                }
            }
        }
}
