package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.FreeholderContactType
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FreeholdersState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ConfirmRemoveFreeholderStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholderContactTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholderEmailStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholderNameStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdersInfoStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdersListStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideFreeholderDetailsStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

@JourneyFrameworkComponent
class FreeholdersTask(
    journeyStateService: JourneyStateService,
    override val freeholdersInfoStep: FreeholdersInfoStep,
    override val freeholderProvideDetailsStep: ProvideFreeholderDetailsStep,
    override val freeholderNameStep: FreeholderNameStep,
    override val freeholderContactTypeStep: FreeholderContactTypeStep,
    override val freeholderEmailStep: FreeholderEmailStep,
    override val freeholderAddressTask: FreeholderAddressTask,
    override val freeholdersListStep: FreeholdersListStep,
    override val confirmRemoveFreeholderStep: ConfirmRemoveFreeholderStep,
) : Task<FreeholdersState, FreeholdersDependencies>(journeyStateService),
    FreeholdersState {
    override val taskState get() = this

    override val isJointFreeholder: Boolean
        get() = dependencies.isJointFreeholder

    override fun makeSubJourney(state: FreeholdersState) =
        subJourney(state) {
            step(journey.freeholdersInfoStep) {
                routeSegment(FreeholdersInfoStep.ROUTE_SEGMENT)
                nextStep { journey.freeholderProvideDetailsStep }
                savable()
            }
            step(journey.freeholderProvideDetailsStep) {
                routeSegment(ProvideFreeholderDetailsStep.ROUTE_SEGMENT)
                parents { journey.freeholdersInfoStep.isComplete() }
                nextStep { mode ->
                    when (mode) {
                        YesOrNo.YES -> journey.freeholderNameStep
                        YesOrNo.NO -> exitStep
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
                nextStep { journey.freeholdersListStep }
                savable()
            }
            task(journey.freeholderAddressTask) {
                parents { journey.freeholderContactTypeStep.hasOutcome(FreeholderContactType.POSTAL_ADDRESS) }
                nextStep { journey.freeholdersListStep }
                savable()
            }
            step(journey.freeholdersListStep) {
                routeSegment(FreeholdersListStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.freeholderEmailStep.isComplete(),
                        journey.freeholderAddressTask.isComplete(),
                    )
                }
                nextStep { journey.confirmRemoveFreeholderStep }
                savable()
            }
            step(journey.confirmRemoveFreeholderStep) {
                routeSegment(ConfirmRemoveFreeholderStep.ROUTE_SEGMENT)
                parents { journey.freeholdersListStep.isComplete() }
                nextStep { exitStep }
                savable()
            }
            exitStep {
                parents {
                    OrParents(
                        journey.freeholderProvideDetailsStep.hasOutcome(YesOrNo.NO),
                        journey.confirmRemoveFreeholderStep.isComplete(),
                    )
                }
            }
        }
}

interface FreeholdersDependencies {
    val isJointFreeholder: Boolean
}
