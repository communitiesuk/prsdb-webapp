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
                savable()
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
                savable()
            }
            step(journey.rentIncludesBills) {
                routeSegment("rent-includes-bills")
                parents { journey.bedrooms.hasOutcome(Complete.COMPLETE) }
                nextStep { mode ->
                    when (mode) {
                        YesOrNo.YES -> journey.billsIncluded
                        YesOrNo.NO -> journey.furnished
                    }
                }
                savable()
            }
            step(journey.billsIncluded) {
                routeSegment(RegisterPropertyStepId.BillsIncluded.urlPathSegment)
                parents { journey.rentIncludesBills.hasOutcome(YesOrNo.YES) }
                nextStep { journey.furnished }
            }
            step(journey.furnished) {
                routeSegment(RegisterPropertyStepId.PropertyFurnished.urlPathSegment)
                parents {
                    OrParents(
                        journey.billsIncluded.hasOutcome(Complete.COMPLETE),
                        journey.rentIncludesBills.hasOutcome(YesOrNo.NO),
                    )
                }
                // TODO PDJB-103 make is property furnished next step
                nextStep { journey.rentFrequency }
                savable()
            }
            // TODO PDJB-103 make is property furnished step have rent frequency as next step
            step(journey.rentFrequency) {
                routeSegment(RegisterPropertyStepId.RentFrequency.urlPathSegment)
                // TODO PDJB-103 make is property furnished step parent of this step
                parents {
                    journey.furnished.hasOutcome(Complete.COMPLETE)
                }
                nextStep { exitStep }
                savable()
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
