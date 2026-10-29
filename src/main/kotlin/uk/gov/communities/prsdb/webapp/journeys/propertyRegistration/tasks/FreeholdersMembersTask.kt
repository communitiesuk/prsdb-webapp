package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.FreeholderContactType
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FreeholdersMembersDependencies
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FreeholdersMembersState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdersListStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasAnyFreeholdersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PersonContactTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PersonEmailStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PersonNameStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PersonProvideDetailsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PersonRemoveConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SaveFreeholderStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SetStateForFreeholderEditStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel

@JourneyFrameworkComponent
class FreeholdersMembersTask(
    journeyStateService: JourneyStateService,
    override val freeholderProvideDetailsStep: PersonProvideDetailsStep,
    override val freeholderNameStep: PersonNameStep,
    override val freeholderContactTypeStep: PersonContactTypeStep,
    override val freeholderEmailStep: PersonEmailStep,
    override val freeholderAddressTask: FreeholderAddressTask,
    override val freeholdersListStep: FreeholdersListStep,
    override val hasAnyFreeholdersStep: HasAnyFreeholdersStep,
    override val saveFreeholderStep: SaveFreeholderStep,
    override val setStateForFreeholderEditStep: SetStateForFreeholderEditStep,
    override val removeFreeholderStep: PersonRemoveConfirmationStep,
) : Task<FreeholdersMembersState, FreeholdersMembersDependencies>(journeyStateService),
    FreeholdersMembersState {
    override val taskState get() = this

    override var freeholdersMap: Map<Int, FreeholderDataModel>?
        get() = dependencies.listState.freeholdersMap
        set(value) {
            dependencies.listState.freeholdersMap = value
        }
    override var nextFreeholderId: Int?
        get() = dependencies.listState.nextFreeholderId
        set(value) {
            dependencies.listState.nextFreeholderId = value
        }
    override var editingFreeholderId: Int?
        get() = dependencies.listState.editingFreeholderId
        set(value) {
            dependencies.listState.editingFreeholderId = value
        }
    override val allowRemovingLastMember: Boolean get() = dependencies.allowRemovingLastMember

    override fun makeSubJourney(state: FreeholdersMembersState) =
        subJourney(state) {
            step(journey.hasAnyFreeholdersStep) {
                routeSegment(HasAnyFreeholdersStep.ROUTE_SEGMENT)
                nextStep { journey.freeholderProvideDetailsStep }
                savable()
            }
            step(journey.setStateForFreeholderEditStep) {
                routeSegment(SetStateForFreeholderEditStep.ROUTE_SEGMENT)
                parents { journey.hasAnyFreeholdersStep.isComplete() }
                nextStep { journey.freeholderProvideDetailsStep }
                savable()
            }
            step(journey.removeFreeholderStep) {
                routeSegment(PersonRemoveConfirmationStep.ROUTE_SEGMENT)
                parents { journey.hasAnyFreeholdersStep.isComplete() }
                backStep { journey.freeholdersListStep }
                nextStep { journey.freeholdersListStep }
                savable()
            }
            step(journey.freeholdersListStep) {
                routeSegment(FreeholdersListStep.ROUTE_SEGMENT)
                parents { journey.hasAnyFreeholdersStep.isComplete() }
                nextStep { exitStep }
                savable()
            }
            step(journey.freeholderProvideDetailsStep) {
                routeSegment(PersonProvideDetailsStep.ROUTE_SEGMENT)
                parents { journey.hasAnyFreeholdersStep.isComplete() }
                nextStep { mode ->
                    when (mode) {
                        YesOrNo.YES -> journey.freeholderNameStep
                        YesOrNo.NO -> journey.saveFreeholderStep
                    }
                }
                savable()
            }
            step(journey.freeholderNameStep) {
                routeSegment(PersonNameStep.ROUTE_SEGMENT)
                parents { journey.freeholderProvideDetailsStep.hasOutcome(YesOrNo.YES) }
                nextStep { journey.freeholderContactTypeStep }
                savable()
            }
            step(journey.freeholderContactTypeStep) {
                routeSegment(PersonContactTypeStep.ROUTE_SEGMENT)
                parents { journey.freeholderNameStep.isComplete() }
                nextStep { mode ->
                    when (mode) {
                        FreeholderContactType.EMAIL -> journey.freeholderEmailStep
                        FreeholderContactType.POSTAL_ADDRESS -> journey.freeholderAddressTask.firstStep
                    }
                }
                savable()
            }
            step(journey.freeholderEmailStep) {
                routeSegment(PersonEmailStep.ROUTE_SEGMENT)
                parents { journey.freeholderContactTypeStep.hasOutcome(FreeholderContactType.EMAIL) }
                nextStep { journey.saveFreeholderStep }
                savable()
            }
            task(journey.freeholderAddressTask) {
                parents { journey.freeholderContactTypeStep.hasOutcome(FreeholderContactType.POSTAL_ADDRESS) }
                nextStep { journey.saveFreeholderStep }
                savable()
            }
            step(journey.saveFreeholderStep) {
                routeSegment(SaveFreeholderStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.freeholderProvideDetailsStep.hasOutcome(YesOrNo.NO),
                        journey.freeholderEmailStep.isComplete(),
                        journey.freeholderAddressTask.isComplete(),
                    )
                }
                nextStep { journey.freeholdersListStep }
                savable()
            }
            exitStep {
                parents { journey.freeholdersListStep.isComplete() }
            }
        }
}
