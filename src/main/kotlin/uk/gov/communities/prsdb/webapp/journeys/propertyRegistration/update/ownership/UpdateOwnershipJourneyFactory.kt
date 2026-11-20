package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.ownership

import kotlinx.datetime.Instant
import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.PEOPLE_RESPONSIBLE_FRAGMENT
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractPropertyOwnershipUpdateJourneyState
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.AnyoneInOwnershipChainStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FinishCyaJourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FreeholdersListStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipKindStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnsPropertyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SuperiorLandlordsListStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.OwnershipStructureTask
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.useContinueButtons
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkAnswerTask
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkAnswerTaskFrom
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@PrsdbWebService
class UpdateOwnershipJourneyFactory(
    private val stateFactory: ObjectFactory<UpdateOwnershipJourney>,
    private val propertyOwnershipService: PropertyOwnershipService,
) {
    final fun createJourneySteps(propertyId: Long): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        if (!state.isStateInitialized) {
            val propertyOwnership = propertyOwnershipService.getPropertyOwnership(propertyId)
            state.propertyId = propertyId
            state.lastModifiedDate = propertyOwnership.getOwnershipStructureMostRecentlyUpdated().toString()
            state.isStateInitialized = true
        }

        if (state.propertyId != propertyId) {
            throw PrsdbWebException("Journey state propertyId ${state.propertyId} does not match provided propertyId $propertyId")
        }

        val checkingAnswersFor = state.checkingAnswersFor
        return if (checkingAnswersFor == null) {
            mainJourneyMap(state, propertyId)
        } else {
            checkYourAnswersJourneyMap(state, checkingAnswersFor)
        }
    }

    private fun mainJourneyMap(
        state: UpdateOwnershipJourney,
        propertyId: Long,
    ): Map<String, StepLifecycleOrchestrator> {
        val peopleResponsibleRoute = "${PropertyDetailsController.getPropertyDetailsPath(propertyId)}#$PEOPLE_RESPONSIBLE_FRAGMENT"

        return journey(state) {
            unreachableStepUrl { peopleResponsibleRoute }
            step(journey.initialiseOwnershipUpdateStep) {
                initialStep()
                routeSegment(InitialiseOwnershipUpdateStep.ROUTE_SEGMENT)
                nextStep { journey.ownershipStructureTask.firstStep }
            }
            task(journey.ownershipStructureTask) {
                parents { journey.initialiseOwnershipUpdateStep.isComplete() }
                backUrl { peopleResponsibleRoute }
                nextStep { journey.checkYourAnswersStep }
            }
            step(journey.checkYourAnswersStep) {
                routeSegment(UpdateOwnershipCheckYourAnswersStep.ROUTE_SEGMENT)
                parents { journey.ownershipStructureTask.isComplete() }
                nextStep { journey.completeOwnershipUpdateStep }
            }
            step(journey.completeOwnershipUpdateStep) {
                parents { journey.checkYourAnswersStep.isComplete() }
                nextUrl { peopleResponsibleRoute }
            }
            useContinueButtons(journey.ownershipStructureTask)
        }
    }

    private fun checkYourAnswersJourneyMap(
        state: UpdateOwnershipJourney,
        checkingAnswersFor: String,
    ): Map<String, StepLifecycleOrchestrator> =
        journey(state) {
            unreachableStepDestination { journey.returnToCyaPageDestination }
            configureFirst { backDestination { journey.returnToCyaPageDestination } }

            val ownershipStructureTask = journey.ownershipStructureTask
            when (checkingAnswersFor) {
                OwnsPropertyStep.ROUTE_SEGMENT -> {
                    checkAnswerTask(ownershipStructureTask)
                }

                OwnershipKindStep.ROUTE_SEGMENT -> {
                    checkAnswerTaskFrom(ownershipStructureTask, startStep = ownershipStructureTask.ownershipTypeTask.ownershipKindStep)
                }

                FreeholdTypeStep.ROUTE_SEGMENT -> {
                    checkAnswerTaskFrom(ownershipStructureTask, startStep = ownershipStructureTask.ownershipTypeTask.freeholdTypeStep)
                }

                AnyoneInOwnershipChainStep.ROUTE_SEGMENT,
                SuperiorLandlordsListStep.ROUTE_SEGMENT,
                -> {
                    val superiorLandlordsTask = ownershipStructureTask.superiorLandlordsTask
                    checkAnswerTask(superiorLandlordsTask)
                    configureStep(superiorLandlordsTask.superiorLandlordsDetailsTask.superiorLandlordsListStep) {
                        backDestination { journey.returnToCyaPageDestination }
                    }
                }

                FreeholdersListStep.ROUTE_SEGMENT -> {
                    val ownershipTypeTask = ownershipStructureTask.ownershipTypeTask
                    val freeholdersTask = ownershipStructureTask.freeholdersTask
                    // The freeholders steps need isJointFreeholder, which is derived from the ownership type steps' outcomes.
                    // Step outcomes are only available once a step is initialised, so the ownership type task must be mounted too.
                    checkAnswerTask(ownershipTypeTask)
                    checkAnswerTaskFrom(
                        freeholdersTask,
                        { ownershipStructureTask },
                        startStep = freeholdersTask.freeholdersDetailsTask.freeholdersListStep,
                    )
                }

                else -> {
                    throw IllegalStateException("Unknown step being checked: $checkingAnswersFor")
                }
            }
            step(journey.finishCyaStep) {
                initialStep()
                nextDestination { Destination.Nowhere() }
            }
            useContinueButtons(journey.ownershipStructureTask)
        }

    fun initializeJourneyState(
        seed: Any?,
        currentLastModifiedDate: java.time.Instant,
    ): String = stateFactory.getObject().initialiseOrRestoreStateReinitialisingIfOutdated(seed, currentLastModifiedDate)
}

@JourneyFrameworkComponent
class UpdateOwnershipJourney(
    // Seeds the freeholders and superior landlords lists from the database
    override val initialiseOwnershipUpdateStep: InitialiseOwnershipUpdateStep,
    // Ownership type, freeholders and superior landlords tasks shared with property registration
    override val ownershipStructureTask: OwnershipStructureTask,
    // Check-your-answers step for the update
    override val checkYourAnswersStep: UpdateOwnershipCheckYourAnswersStep,
    // Completion step for the update
    override val completeOwnershipUpdateStep: CompleteOwnershipUpdateStep,
    // Finish check-your-answers step
    override val finishCyaStep: FinishCyaJourneyStep,
    journeyStateService: JourneyStateService,
    journeyName: String = "ownership",
    override val stateFactory: ObjectFactory<UpdateOwnershipJourneyState>,
) : AbstractPropertyOwnershipUpdateJourneyState(journeyStateService, journeyName),
    UpdateOwnershipJourneyState {
    override val cyaStep: JourneyStep.RequestableStep<*, *, *>
        get() = checkYourAnswersStep

    override var propertyId: Long by delegateProvider.requiredDelegate("propertyId")

    override var originalJourneyUpdated: Instant? by delegateProvider.nullableDelegate("originalJourneyUpdated")
    override var checkingAnswersFor: String? by delegateProvider.nullableDelegate("checkingAnswersFor")
    override var cyaJourneys: Map<String, String> = mapOf()

    override var cyaUrlPath: String? by delegateProvider.nullableDelegate("cyaRouteSegment")

    override var lastModifiedDate: String by delegateProvider.requiredImmutableDelegate(LAST_MODIFIED_DATE_KEY)

    override var ownershipPartiesInitialised: Boolean? by delegateProvider.nullableDelegate("ownershipPartiesInitialised")
}

interface UpdateOwnershipJourneyState : CheckYourAnswersJourneyState {
    val initialiseOwnershipUpdateStep: InitialiseOwnershipUpdateStep
    val ownershipStructureTask: OwnershipStructureTask
    val checkYourAnswersStep: UpdateOwnershipCheckYourAnswersStep
    val completeOwnershipUpdateStep: CompleteOwnershipUpdateStep
    val propertyId: Long
    val lastModifiedDate: String
    var ownershipPartiesInitialised: Boolean?
}
