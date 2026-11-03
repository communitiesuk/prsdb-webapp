package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FreeholdersDetailsDependencies
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FreeholdersState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdersBackRoutingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdersInfoStep
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel

@JourneyFrameworkComponent
class FreeholdersTask(
    journeyStateService: JourneyStateService,
    override val freeholdersInfoStep: FreeholdersInfoStep,
    override val freeholdersDetailsTask: FreeholdersDetailsTask,
    override val freeholdersBackRoutingStep: FreeholdersBackRoutingStep,
) : Task<FreeholdersState, FreeholdersDependencies>(journeyStateService),
    FreeholdersState {
    override val taskState get() = this

    override val isJointFreeholder: Boolean
        get() = dependencies.isJointFreeholder

    override var freeholdersMap: Map<Int, FreeholderDataModel>? by delegateProvider.nullableDelegate("freeholdersMap")
    override var nextFreeholderId: Int? by delegateProvider.nullableDelegate("nextFreeholderId")
    override var editingFreeholderId: Int? by delegateProvider.nullableDelegate("editingFreeholderId")

    override fun makeSubJourney(state: FreeholdersState) =
        subJourney(state) {
            step(journey.freeholdersInfoStep) {
                routeSegment(FreeholdersInfoStep.ROUTE_SEGMENT)
                nextStep { journey.freeholdersDetailsTask.firstStep }
                savable()
            }
            task(journey.freeholdersDetailsTask) {
                parents { journey.freeholdersInfoStep.isComplete() }
                backDestination { Destination(journey.freeholdersInfoStep) }
                nextStep { journey.freeholdersBackRoutingStep }
                withDependencies {
                    FreeholdersDetailsDependencies(
                        listState = journey,
                    )
                }
                savable()
            }
            step(journey.freeholdersBackRoutingStep) {
                routeSegment(FreeholdersBackRoutingStep.ROUTE_SEGMENT)
                parents { journey.freeholdersDetailsTask.isComplete() }
                nextStep { exitStep }
                savable()
            }
            exitStep {
                parents { journey.freeholdersBackRoutingStep.isComplete() }
            }
        }
}

interface FreeholdersDependencies {
    val isJointFreeholder: Boolean
}
