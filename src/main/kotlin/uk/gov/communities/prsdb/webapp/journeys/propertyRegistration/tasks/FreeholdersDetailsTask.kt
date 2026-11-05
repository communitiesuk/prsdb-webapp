package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.FreeholderContactType
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.SingleParent
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FreeholdersDetailsDependencies
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FreeholdersDetailsState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ConfirmRemoveFreeholderStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholderContactTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholderEmailStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholderNameStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdersListStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasAnyFreeholdersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideFreeholderDetailsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SaveFreeholderStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SetStateForFreeholderEditStep
import uk.gov.communities.prsdb.webapp.journeys.shared.AnyMembers
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel

@JourneyFrameworkComponent
class FreeholdersDetailsTask(
    journeyStateService: JourneyStateService,
    override val freeholderProvideDetailsStep: ProvideFreeholderDetailsStep,
    override val freeholderNameStep: FreeholderNameStep,
    override val freeholderContactTypeStep: FreeholderContactTypeStep,
    override val freeholderEmailStep: FreeholderEmailStep,
    override val freeholderAddressTask: FreeholderAddressTask,
    override val freeholdersListStep: FreeholdersListStep,
    override val hasAnyFreeholdersStep: HasAnyFreeholdersStep,
    override val saveFreeholderStep: SaveFreeholderStep,
    override val setStateForFreeholderEditStep: SetStateForFreeholderEditStep,
    override val confirmRemoveFreeholderStep: ConfirmRemoveFreeholderStep,
) : Task<FreeholdersDetailsState, FreeholdersDetailsDependencies>(journeyStateService),
    FreeholdersDetailsState {
    override val taskState get() = this

    override val isJointFreeholder: Boolean
        get() = dependencies.isJointFreeholder()

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

    override fun makeSubJourney(state: FreeholdersDetailsState) =
        subJourney(state) {
            step(journey.hasAnyFreeholdersStep) {
                nextStep { mode ->
                    when (mode) {
                        AnyMembers.NO_MEMBERS -> journey.freeholderProvideDetailsStep
                        AnyMembers.SOME_MEMBERS -> journey.freeholdersListStep
                    }
                }
            }
            step(journey.setStateForFreeholderEditStep) {
                routeSegment(SetStateForFreeholderEditStep.ROUTE_SEGMENT)
                parents { journey.hasAnyFreeholdersStep.hasOutcome(AnyMembers.SOME_MEMBERS) }
                nextStep { journey.freeholderProvideDetailsStep }
            }
            step(journey.confirmRemoveFreeholderStep) {
                routeSegment(ConfirmRemoveFreeholderStep.ROUTE_SEGMENT)
                parents { journey.hasAnyFreeholdersStep.hasOutcome(AnyMembers.SOME_MEMBERS) }
                backStep { journey.freeholdersListStep }
                nextStep { mode ->
                    when (mode) {
                        AnyMembers.SOME_MEMBERS -> journey.freeholdersListStep
                        AnyMembers.NO_MEMBERS -> exitStep
                    }
                }
                savable()
            }
            step(journey.freeholdersListStep) {
                routeSegment(FreeholdersListStep.ROUTE_SEGMENT)
                parents { journey.hasAnyFreeholdersStep.hasOutcome(AnyMembers.SOME_MEMBERS) }
                nextStep { exitStep }
                savable()
            }
            step(journey.freeholderProvideDetailsStep) {
                routeSegment(ProvideFreeholderDetailsStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.hasAnyFreeholdersStep.hasOutcome(AnyMembers.NO_MEMBERS),
                        SingleParent(journey.freeholdersListStep) {
                            !journey.freeholdersMap.isNullOrEmpty()
                        },
                    )
                }
                nextStep { mode ->
                    when (mode) {
                        YesOrNo.YES -> journey.freeholderNameStep
                        YesOrNo.NO -> journey.saveFreeholderStep
                    }
                }
                savable()
            }
            step(journey.freeholderNameStep) {
                routeSegment(FreeholderNameStep.ROUTE_SEGMENT)
                parents { journey.freeholderProvideDetailsStep.hasOutcome(YesOrNo.YES) }
                nextStep { journey.freeholderContactTypeStep }
                savable()
            }
            step(journey.freeholderContactTypeStep) {
                routeSegment(FreeholderContactTypeStep.ROUTE_SEGMENT)
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
                routeSegment(FreeholderEmailStep.ROUTE_SEGMENT)
                parents { journey.freeholderContactTypeStep.hasOutcome(FreeholderContactType.EMAIL) }
                nextStep { journey.saveFreeholderStep }
                savable()
            }
            task(journey.freeholderAddressTask, FreeholderAddressTask.ROUTE_SEGMENT) {
                parents { journey.freeholderContactTypeStep.hasOutcome(FreeholderContactType.POSTAL_ADDRESS) }
                nextStep { journey.saveFreeholderStep }
                savable()
                configureStep(journey.freeholderAddressTask.lookupAddressStep) {
                    withAdditionalContentProperty {
                        "fieldSetHeadingParam" to journey.freeholderNameStep.formModelOrNull?.name
                    }
                }
                configureStep(journey.freeholderAddressTask.selectAddressStep) {
                    withAdditionalContentProperty {
                        "fieldSetHeadingParam" to journey.freeholderNameStep.formModelOrNull?.name
                    }
                }
                configureStep(journey.freeholderAddressTask.manualAddressStep) {
                    withAdditionalContentProperty {
                        "fieldSetHeadingParam" to journey.freeholderNameStep.formModelOrNull?.name
                    }
                }
            }
            step(journey.saveFreeholderStep) {
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
                parents {
                    OrParents(
                        journey.freeholdersListStep.isComplete(),
                        journey.confirmRemoveFreeholderStep.hasOutcome(AnyMembers.NO_MEMBERS),
                    )
                }
            }
        }
}
