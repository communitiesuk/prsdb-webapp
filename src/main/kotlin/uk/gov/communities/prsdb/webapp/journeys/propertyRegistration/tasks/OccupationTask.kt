package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.example.steps.YesOrNo
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.OccupationState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete

@PrsdbWebComponent
@Scope("prototype")
class OccupationTask : Task<OccupationState>() {
    override fun makeSubJourney(state: OccupationState) =
        subJourney(state) {
            step("occupied", journey.occupied) {
                nextStep { mode ->
                    when (mode) {
                        YesOrNo.YES -> journey.households
                        YesOrNo.NO -> exitStep
                    }
                }
            }
            step("households", journey.households) {
                parents { journey.occupied.hasOutcome(YesOrNo.YES) }
                nextStep { journey.tenants }
            }
            step("tenants", journey.tenants) {
                parents { journey.households.hasOutcome(Complete.COMPLETE) }
                nextStep { journey.bedrooms }
            }
            step("bedrooms", journey.bedrooms) {
                parents { journey.tenants.hasOutcome(Complete.COMPLETE) }
                nextStep { journey.rentIncludesBills }
            }
            step("rent-includes-bills", journey.rentIncludesBills) {
                parents { journey.bedrooms.hasOutcome(Complete.COMPLETE) }
                nextStep { exitStep }
            }
            exitStep {
                parents {
                    OrParents(
                        journey.tenants.hasOutcome(Complete.COMPLETE),
                        journey.occupied.hasOutcome(YesOrNo.NO),
                    )
                }
            }
        }
}
