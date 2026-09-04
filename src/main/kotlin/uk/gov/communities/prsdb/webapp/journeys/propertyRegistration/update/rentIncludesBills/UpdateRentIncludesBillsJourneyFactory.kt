package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.rentIncludesBills

import kotlinx.datetime.Instant
import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractPropertyOwnershipUpdateJourneyState
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BillsIncludedStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FinishCyaJourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentIncludesBillsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.RentIncludesBillsTask
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkAnswerStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkAnswerTask
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@PrsdbWebService
class UpdateRentIncludesBillsJourneyFactory(
    private val stateFactory: ObjectFactory<UpdateRentIncludesBillsJourney>,
    private val propertyOwnershipService: PropertyOwnershipService,
) {
    final fun createJourneySteps(
        propertyId: Long,
        returnUrl: String,
    ): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        if (!state.isStateInitialized) {
            state.propertyId = propertyId
            state.lastModifiedDate = propertyOwnershipService.getPropertyOwnership(propertyId).getMostRecentlyUpdated().toString()
            state.isStateInitialized = true
        }

        if (state.propertyId != propertyId) {
            throw PrsdbWebException("Journey state propertyId ${state.propertyId} does not match provided propertyId $propertyId")
        }

        val checkingAnswersFor = state.checkingAnswersFor
        return if (checkingAnswersFor == null) {
            mainJourneyMap(state, returnUrl)
        } else {
            checkYourAnswersJourneyMap(state, checkingAnswersFor, returnUrl)
        }
    }

    private fun mainJourneyMap(
        state: UpdateRentIncludesBillsJourney,
        returnUrl: String,
    ): Map<String, StepLifecycleOrchestrator> =
        journey(state) {
            unreachableStepUrl { returnUrl }
            task(journey.rentIncludesBillsTask) {
                initialStep()
                backUrl { returnUrl }
                nextStep { journey.cyaStep }
                withAdditionalContentProperty {
                    "title" to "propertyDetails.update.title"
                }
            }
            step(journey.cyaStep) {
                routeSegment(UpdateRentIncludesBillsCyaStep.ROUTE_SEGMENT)
                parents { journey.rentIncludesBillsTask.isComplete() }
                nextUrl { returnUrl }
            }
            configureStep(journey.rentIncludesBillsTask.rentIncludesBills) {
                withAdditionalContentProperty {
                    "fieldSetHeading" to "forms.update.rentIncludesBills.fieldSetHeading"
                }
            }
            configureStep(journey.rentIncludesBillsTask.billsIncluded) {
                withAdditionalContentProperty {
                    "fieldSetHeading" to "forms.update.billsIncluded.fieldSetHeading"
                }
            }
        }

    private fun checkYourAnswersJourneyMap(
        state: UpdateRentIncludesBillsJourney,
        checkingAnswersFor: String,
        returnUrl: String,
    ): Map<String, StepLifecycleOrchestrator> =
        journey(state) {
            unreachableStepUrl { returnUrl }

            configureFirst { backDestination { journey.returnToCyaPageDestination } }
            when (checkingAnswersFor) {
                RentIncludesBillsStep.ROUTE_SEGMENT -> {
                    checkAnswerTask(journey.rentIncludesBillsTask)
                }

                BillsIncludedStep.ROUTE_SEGMENT -> {
                    fromTask(journey.rentIncludesBillsTask) {
                        checkAnswerStep(task.billsIncluded, BillsIncludedStep.ROUTE_SEGMENT)
                    }
                }

                else -> {
                    throw IllegalStateException("Unknown step being checked: $checkingAnswersFor")
                }
            }
            step(journey.finishCyaStep) {
                initialStep()
                nextDestination { Destination.Nowhere() }
            }
            configureStep(journey.rentIncludesBillsTask.rentIncludesBills) {
                withAdditionalContentProperty {
                    "fieldSetHeading" to "forms.update.rentIncludesBills.fieldSetHeading"
                }
            }
            configureStep(journey.rentIncludesBillsTask.billsIncluded) {
                withAdditionalContentProperty {
                    "fieldSetHeading" to "forms.update.billsIncluded.fieldSetHeading"
                }
            }
            configure {
                withAdditionalContentProperty {
                    "title" to "propertyDetails.update.title"
                }
            }
        }

    fun initializeJourneyState(seed: Any): String = stateFactory.getObject().initializeOrRestoreState(seed)
}

@JourneyFrameworkComponent
class UpdateRentIncludesBillsJourney(
    // RentIncludesBills task
    override val rentIncludesBillsTask: RentIncludesBillsTask,
    // Check your answers step
    override val cyaStep: UpdateRentIncludesBillsCyaStep,
    journeyStateService: JourneyStateService,
    journeyName: String = "rent includes bills",
    override val finishCyaStep: FinishCyaJourneyStep,
    override val stateFactory: ObjectFactory<UpdateRentIncludesBillsJourneyState>,
) : AbstractPropertyOwnershipUpdateJourneyState(journeyStateService, journeyName),
    UpdateRentIncludesBillsJourneyState {
    override var propertyId: Long by delegateProvider.requiredImmutableDelegate("propertyId")
    override var lastModifiedDate: String by delegateProvider.requiredImmutableDelegate("lastModifiedDate")
    override var originalJourneyUpdated: Instant? by delegateProvider.nullableDelegate("originalJourneyUpdated")
    override var cyaJourneys: Map<String, String> = mapOf()
    override var checkingAnswersFor: String? by delegateProvider.nullableDelegate("checkingAnswersFor")

    override var cyaUrlPath: String? by delegateProvider.nullableDelegate("cyaRouteSegment")
}

interface UpdateRentIncludesBillsJourneyState : CheckYourAnswersJourneyState {
    val rentIncludesBillsTask: RentIncludesBillsTask
    override val cyaStep: UpdateRentIncludesBillsCyaStep
    val propertyId: Long
    val lastModifiedDate: String
}
