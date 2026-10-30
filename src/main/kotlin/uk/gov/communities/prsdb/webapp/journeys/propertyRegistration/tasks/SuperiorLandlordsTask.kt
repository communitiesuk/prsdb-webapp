package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.TaskWithoutDependencies
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.SuperiorLandlordsMembersDependencies
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.SuperiorLandlordsState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.AnyoneInOwnershipChainStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SuperiorLandlordsBackRoutingStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel

@JourneyFrameworkComponent
class SuperiorLandlordsTask(
    journeyStateService: JourneyStateService,
    override val anyoneInOwnershipChainStep: AnyoneInOwnershipChainStep,
    override val superiorLandlordsMembersTask: SuperiorLandlordsMembersTask,
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
                        YesOrNo.YES -> journey.superiorLandlordsMembersTask.firstStep
                        YesOrNo.NO -> exitStep
                    }
                }
                savable()
            }
            task(journey.superiorLandlordsMembersTask) {
                parents { journey.anyoneInOwnershipChainStep.hasOutcome(YesOrNo.YES) }
                backDestination { Destination(journey.anyoneInOwnershipChainStep) }
                nextStep { journey.superiorLandlordsBackRoutingStep }
                withDependencies {
                    SuperiorLandlordsMembersDependencies(
                        listState = journey,
                    )
                }
                savable()
            }
            step(journey.superiorLandlordsBackRoutingStep) {
                routeSegment(SuperiorLandlordsBackRoutingStep.ROUTE_SEGMENT)
                parents { journey.superiorLandlordsMembersTask.isComplete() }
                nextStep { exitStep }
                savable()
            }
            exitStep {
                parents {
                    OrParents(
                        journey.anyoneInOwnershipChainStep.hasOutcome(YesOrNo.NO),
                        journey.superiorLandlordsBackRoutingStep.isComplete(),
                    )
                }
            }
        }

    companion object {
        const val ROUTE_SEGMENT = "superior-landlord"
    }
}
