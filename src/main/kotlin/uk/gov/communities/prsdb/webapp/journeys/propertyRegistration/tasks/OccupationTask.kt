package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.example.steps.YesOrNo
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.OccupationState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete

@JourneyFrameworkComponent
class OccupationTask : Task<OccupationState>() {
    override fun makeSubJourney(state: OccupationState) =
        subJourney(state) {
            step(journey.occupied) {
                routeSegment("occupancy")
                nextStep { mode ->
                    when (mode) {
                        YesOrNo.YES -> journey.households
                        YesOrNo.NO -> exitStep
                    }
                }
            }
            step(journey.households) {
                routeSegment("number-of-households")
                parents { journey.occupied.hasOutcome(YesOrNo.YES) }
                nextStep { journey.tenants }
                savable()
            }
            step(journey.tenants) {
                routeSegment("number-of-people")
                parents { journey.households.hasOutcome(Complete.COMPLETE) }
                nextStep { journey.bedrooms }
                savable()
            }
            step(journey.bedrooms) {
                routeSegment(RegisterPropertyStepId.NumberOfBedrooms.urlPathSegment)
                parents { journey.tenants.hasOutcome(Complete.COMPLETE) }
                nextStep { journey.rentIncludesBills }
            }
            step(journey.rentIncludesBills) {
                routeSegment("rent-includes-bills")
                parents { journey.bedrooms.hasOutcome(Complete.COMPLETE) }
                nextStep { journey.billsIncluded }
            }
            step("bills-included", journey.billsIncluded) {
                parents { journey.rentIncludesBills.hasOutcome(YesOrNo.YES) }
                nextStep { exitStep }
            }
            exitStep {
                savable()
                parents {
                    OrParents(
                        journey.tenants.hasOutcome(Complete.COMPLETE),
                        journey.occupied.hasOutcome(YesOrNo.NO),
                    )
                }
            }
        }
}
