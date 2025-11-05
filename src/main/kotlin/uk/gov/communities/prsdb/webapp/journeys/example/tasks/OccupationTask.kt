package uk.gov.communities.prsdb.webapp.journeys.example.tasks

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Parentage
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.subJourney
import uk.gov.communities.prsdb.webapp.journeys.builders.StepInitialiser
import uk.gov.communities.prsdb.webapp.journeys.example.OccupiedJourneyState
import uk.gov.communities.prsdb.webapp.journeys.example.steps.Complete
import uk.gov.communities.prsdb.webapp.journeys.example.steps.YesOrNo
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome

@PrsdbWebComponent
@Scope("prototype")
class OccupationTask : Task<Complete, OccupiedJourneyState>() {
    override fun makeSubJourney(
        state: OccupiedJourneyState,
        entryPoint: Parentage,
    ): List<StepInitialiser<*, OccupiedJourneyState, *>> =
        subJourney(state) {
            step("occupied", journey.occupied) {
                parents { entryPoint }
                nextStep { mode ->
                    when (mode) {
                        YesOrNo.YES -> journey.households
                        YesOrNo.NO -> notionalExitStep
                    }
                }
            }
            step("households", journey.households) {
                parents { journey.occupied.hasOutcome(YesOrNo.YES) }
                nextStep { journey.tenants }
            }
            step("tenants", journey.tenants) {
                parents { journey.households.hasOutcome(Complete.COMPLETE) }
                nextStep { notionalExitStep }
            }
        }

    override fun taskCompletionParentage(state: OccupiedJourneyState): Parentage =
        OrParents(
            state.tenants.hasOutcome(Complete.COMPLETE),
            state.occupied.hasOutcome(YesOrNo.NO),
        )
}
