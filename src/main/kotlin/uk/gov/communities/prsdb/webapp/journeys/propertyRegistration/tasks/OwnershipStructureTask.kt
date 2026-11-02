package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AndParents
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.TaskWithoutDependencies
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.OwnershipStructureState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipStructureRouteMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipStructureRoutingStep

@JourneyFrameworkComponent
class OwnershipStructureTask(
    journeyStateService: JourneyStateService,
    override val ownershipTypeTask: OwnershipTypeTask,
    override val ownershipStructureRoutingStep: OwnershipStructureRoutingStep,
    override val freeholdersTask: FreeholdersTask,
    override val superiorLandlordsTask: SuperiorLandlordsTask,
) : TaskWithoutDependencies<OwnershipStructureState>(journeyStateService),
    OwnershipStructureState {
    override val taskState get() = this

    override fun makeSubJourney(state: OwnershipStructureState) =
        subJourney(state) {
            task(journey.ownershipTypeTask) {
                nextStep { journey.ownershipStructureRoutingStep }
                savable()
            }
            step(journey.ownershipStructureRoutingStep) {
                parents { journey.ownershipTypeTask.isComplete() }
                nextStep { mode ->
                    when (mode) {
                        OwnershipStructureRouteMode.SKIP_ALL -> exitStep

                        OwnershipStructureRouteMode.ASK_FREEHOLDERS,
                        OwnershipStructureRouteMode.ASK_FREEHOLDERS_AND_SUPERIOR_LANDLORDS,
                        -> journey.freeholdersTask.firstStep

                        OwnershipStructureRouteMode.ASK_SUPERIOR_LANDLORDS -> journey.superiorLandlordsTask.firstStep
                    }
                }
            }
            task(journey.freeholdersTask) {
                parents {
                    OrParents(
                        journey.ownershipStructureRoutingStep.hasOutcome(OwnershipStructureRouteMode.ASK_FREEHOLDERS),
                        journey.ownershipStructureRoutingStep.hasOutcome(
                            OwnershipStructureRouteMode.ASK_FREEHOLDERS_AND_SUPERIOR_LANDLORDS,
                        ),
                    )
                }
                nextStep {
                    when (journey.ownershipStructureRoutingStep.outcome) {
                        OwnershipStructureRouteMode.ASK_FREEHOLDERS_AND_SUPERIOR_LANDLORDS -> journey.superiorLandlordsTask.firstStep
                        else -> exitStep
                    }
                }
                savable()
            }
            task(journey.superiorLandlordsTask) {
                parents {
                    OrParents(
                        journey.ownershipStructureRoutingStep.hasOutcome(OwnershipStructureRouteMode.ASK_SUPERIOR_LANDLORDS),
                        AndParents(
                            journey.freeholdersTask.isComplete(),
                            journey.ownershipStructureRoutingStep.hasOutcome(
                                OwnershipStructureRouteMode.ASK_FREEHOLDERS_AND_SUPERIOR_LANDLORDS,
                            ),
                        ),
                    )
                }
                backUrl {
                    when (journey.ownershipStructureRoutingStep.outcome) {
                        OwnershipStructureRouteMode.ASK_FREEHOLDERS_AND_SUPERIOR_LANDLORDS -> journey.freeholdersTask.exitStep.backUrl
                        else -> journey.ownershipStructureRoutingStep.backUrl
                    }
                }
                nextStep { exitStep }
                savable()
            }
            exitStep {
                parents {
                    OrParents(
                        journey.ownershipStructureRoutingStep.hasOutcome(OwnershipStructureRouteMode.SKIP_ALL),
                        AndParents(
                            journey.freeholdersTask.isComplete(),
                            journey.ownershipStructureRoutingStep.hasOutcome(OwnershipStructureRouteMode.ASK_FREEHOLDERS),
                        ),
                        journey.superiorLandlordsTask.isComplete(),
                    )
                }
                backUrl {
                    when (journey.ownershipStructureRoutingStep.outcome) {
                        OwnershipStructureRouteMode.SKIP_ALL -> journey.ownershipStructureRoutingStep.backUrl

                        OwnershipStructureRouteMode.ASK_FREEHOLDERS -> journey.freeholdersTask.exitStep.backUrl

                        OwnershipStructureRouteMode.ASK_SUPERIOR_LANDLORDS,
                        OwnershipStructureRouteMode.ASK_FREEHOLDERS_AND_SUPERIOR_LANDLORDS,
                        -> journey.superiorLandlordsTask.exitStep.backUrl

                        null -> null
                    }
                }
            }
        }
}
