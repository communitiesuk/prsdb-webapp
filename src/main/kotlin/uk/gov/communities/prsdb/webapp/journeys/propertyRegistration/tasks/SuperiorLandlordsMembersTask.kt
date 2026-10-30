package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.PersonContactType
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.SuperiorLandlordsMembersDependencies
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.SuperiorLandlordsMembersState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasAnySuperiorLandlordsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PersonContactTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PersonEmailStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PersonNameStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PersonProvideDetailsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PersonRemoveConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SaveSuperiorLandlordStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SetStateForSuperiorLandlordEditStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SuperiorLandlordsListStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel

@JourneyFrameworkComponent
class SuperiorLandlordsMembersTask(
    journeyStateService: JourneyStateService,
    override val superiorLandlordProvideDetailsStep: PersonProvideDetailsStep,
    override val superiorLandlordNameStep: PersonNameStep,
    override val superiorLandlordContactTypeStep: PersonContactTypeStep,
    override val superiorLandlordEmailStep: PersonEmailStep,
    override val superiorLandlordAddressTask: SuperiorLandlordAddressTask,
    override val superiorLandlordsListStep: SuperiorLandlordsListStep,
    override val hasAnySuperiorLandlordsStep: HasAnySuperiorLandlordsStep,
    override val saveSuperiorLandlordStep: SaveSuperiorLandlordStep,
    override val setStateForSuperiorLandlordEditStep: SetStateForSuperiorLandlordEditStep,
    override val removeSuperiorLandlordStep: PersonRemoveConfirmationStep,
) : Task<SuperiorLandlordsMembersState, SuperiorLandlordsMembersDependencies>(journeyStateService),
    SuperiorLandlordsMembersState {
    override val taskState get() = this

    override var superiorLandlordsMap: Map<Int, SuperiorLandlordDataModel>?
        get() = dependencies.listState.superiorLandlordsMap
        set(value) {
            dependencies.listState.superiorLandlordsMap = value
        }
    override var nextSuperiorLandlordId: Int?
        get() = dependencies.listState.nextSuperiorLandlordId
        set(value) {
            dependencies.listState.nextSuperiorLandlordId = value
        }
    override var editingSuperiorLandlordId: Int?
        get() = dependencies.listState.editingSuperiorLandlordId
        set(value) {
            dependencies.listState.editingSuperiorLandlordId = value
        }
    override val allowRemovingLastMember: Boolean get() = dependencies.allowRemovingLastMember

    override fun makeSubJourney(state: SuperiorLandlordsMembersState) =
        subJourney(state) {
            step(journey.hasAnySuperiorLandlordsStep) {
                routeSegment(HasAnySuperiorLandlordsStep.ROUTE_SEGMENT)
                nextStep { journey.superiorLandlordProvideDetailsStep }
                savable()
            }
            step(journey.setStateForSuperiorLandlordEditStep) {
                routeSegment(SetStateForSuperiorLandlordEditStep.ROUTE_SEGMENT)
                parents { journey.hasAnySuperiorLandlordsStep.isComplete() }
                nextStep { journey.superiorLandlordProvideDetailsStep }
                savable()
            }
            step(journey.removeSuperiorLandlordStep) {
                routeSegment(PersonRemoveConfirmationStep.ROUTE_SEGMENT)
                parents { journey.hasAnySuperiorLandlordsStep.isComplete() }
                backStep { journey.superiorLandlordsListStep }
                nextStep { journey.superiorLandlordsListStep }
                savable()
            }
            step(journey.superiorLandlordsListStep) {
                routeSegment(SuperiorLandlordsListStep.ROUTE_SEGMENT)
                parents { journey.hasAnySuperiorLandlordsStep.isComplete() }
                nextStep { exitStep }
                savable()
            }
            step(journey.superiorLandlordProvideDetailsStep) {
                routeSegment(PersonProvideDetailsStep.ROUTE_SEGMENT)
                parents { journey.hasAnySuperiorLandlordsStep.isComplete() }
                nextStep { mode ->
                    when (mode) {
                        YesOrNo.YES -> journey.superiorLandlordNameStep
                        YesOrNo.NO -> journey.saveSuperiorLandlordStep
                    }
                }
                savable()
            }
            step(journey.superiorLandlordNameStep) {
                routeSegment(PersonNameStep.ROUTE_SEGMENT)
                parents { journey.superiorLandlordProvideDetailsStep.hasOutcome(YesOrNo.YES) }
                nextStep { journey.superiorLandlordContactTypeStep }
                savable()
            }
            step(journey.superiorLandlordContactTypeStep) {
                routeSegment(PersonContactTypeStep.ROUTE_SEGMENT)
                parents { journey.superiorLandlordNameStep.isComplete() }
                nextStep { mode ->
                    when (mode) {
                        PersonContactType.EMAIL -> journey.superiorLandlordEmailStep
                        PersonContactType.POSTAL_ADDRESS -> journey.superiorLandlordAddressTask.firstStep
                    }
                }
                savable()
            }
            step(journey.superiorLandlordEmailStep) {
                routeSegment(PersonEmailStep.ROUTE_SEGMENT)
                parents { journey.superiorLandlordContactTypeStep.hasOutcome(PersonContactType.EMAIL) }
                nextStep { journey.saveSuperiorLandlordStep }
                savable()
            }
            task(journey.superiorLandlordAddressTask) {
                parents { journey.superiorLandlordContactTypeStep.hasOutcome(PersonContactType.POSTAL_ADDRESS) }
                nextStep { journey.saveSuperiorLandlordStep }
                savable()
            }
            step(journey.saveSuperiorLandlordStep) {
                routeSegment(SaveSuperiorLandlordStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.superiorLandlordProvideDetailsStep.hasOutcome(YesOrNo.NO),
                        journey.superiorLandlordEmailStep.isComplete(),
                        journey.superiorLandlordAddressTask.isComplete(),
                    )
                }
                nextStep { journey.superiorLandlordsListStep }
                savable()
            }
            exitStep {
                parents { journey.superiorLandlordsListStep.isComplete() }
            }
        }
}
