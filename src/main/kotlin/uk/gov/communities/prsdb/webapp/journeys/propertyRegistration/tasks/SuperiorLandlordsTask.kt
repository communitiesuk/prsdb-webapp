package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.TaskWithoutDependencies
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.SuperiorLandlordsDetailsDependencies
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.SuperiorLandlordsState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.AnyoneInOwnershipChainStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SuperiorLandlordsBackRoutingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SuperiorLandlordsBackRoutingStepConfig
import uk.gov.communities.prsdb.webapp.journeys.shared.AnyMembers
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel

@JourneyFrameworkComponent
class SuperiorLandlordsTask(
    journeyStateService: JourneyStateService,
    override val anyoneInOwnershipChainStep: AnyoneInOwnershipChainStep,
    override val superiorLandlordsDetailsTask: SuperiorLandlordsDetailsTask,
    override val superiorLandlordsBackRoutingStep: SuperiorLandlordsBackRoutingStep,
) : TaskWithoutDependencies<SuperiorLandlordsState>(journeyStateService),
    SuperiorLandlordsState {
    override val taskState get() = this

    override var superiorLandlordsMap: Map<Int, SuperiorLandlordDataModel>? by delegateProvider.nullableDelegate("superiorLandlordsMap")
    override var nextSuperiorLandlordId: Int? by delegateProvider.nullableDelegate("nextSuperiorLandlordId")
    override var editingSuperiorLandlordId: Int? by delegateProvider.nullableDelegate("editingSuperiorLandlordId")

    override fun makeSubJourney(state: SuperiorLandlordsState) =
        subJourney(state) {
            step(journey.anyoneInOwnershipChainStep) {
                routeSegment(AnyoneInOwnershipChainStep.ROUTE_SEGMENT)
                nextStep { mode ->
                    when (mode) {
                        YesOrNo.YES -> journey.superiorLandlordsDetailsTask.firstStep
                        YesOrNo.NO -> exitStep
                    }
                }
                savable()
            }
            task(journey.superiorLandlordsDetailsTask) {
                parents { journey.anyoneInOwnershipChainStep.hasOutcome(YesOrNo.YES) }
                backDestination { Destination(journey.anyoneInOwnershipChainStep) }
                nextStep { journey.superiorLandlordsBackRoutingStep }
                withDependencies {
                    SuperiorLandlordsDetailsDependencies(
                        listState = journey,
                    )
                }
                savable()
            }
            step<AnyMembers, SuperiorLandlordsBackRoutingStepConfig>(journey.superiorLandlordsBackRoutingStep) {
                stepSpecificInitialisation { usingSuperiorLandlordsList { journey.superiorLandlordsMap } }
                parents { journey.superiorLandlordsDetailsTask.isComplete() }
                nextStep { mode ->
                    when (mode) {
                        AnyMembers.NO_MEMBERS -> journey.anyoneInOwnershipChainStep
                        AnyMembers.SOME_MEMBERS -> exitStep
                    }
                }
            }
            exitStep {
                parents {
                    OrParents(
                        journey.anyoneInOwnershipChainStep.hasOutcome(YesOrNo.NO),
                        journey.superiorLandlordsBackRoutingStep.hasOutcome(AnyMembers.SOME_MEMBERS),
                    )
                }
            }
        }
}
