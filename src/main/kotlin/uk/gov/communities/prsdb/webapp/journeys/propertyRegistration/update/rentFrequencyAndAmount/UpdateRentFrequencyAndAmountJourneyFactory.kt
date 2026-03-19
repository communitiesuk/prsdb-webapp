package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.rentFrequencyAndAmount

import kotlinx.datetime.Instant
import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractPropertyOwnershipUpdateJourneyState
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateDelegateProvider
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.Unvisitable
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.RentFrequencyAndAmountState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FinishCyaJourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentAmountStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentFrequencyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.RentFrequencyAndAmountTask
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkAnswerStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkAnswerTask
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.security.Principal

@PrsdbWebService
class UpdateRentFrequencyAndAmountJourneyFactory(
    private val stateFactory: ObjectFactory<UpdateRentFrequencyAndAmountJourney>,
    private val propertyOwnershipService: PropertyOwnershipService,
) {
    final fun createJourneySteps(propertyId: Long): Map<String, StepLifecycleOrchestrator> {
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
            mainJourneyMap(state, propertyId)
        } else {
            checkYourAnswersJourneyMap(state, checkingAnswersFor, propertyId)
        }
    }

    private fun mainJourneyMap(
        state: UpdateRentFrequencyAndAmountJourney,
        propertyId: Long,
    ): Map<String, StepLifecycleOrchestrator> {
        val propertyDetailsRoute = PropertyDetailsController.getPropertyDetailsPath(propertyId)

        return journey(state) {
            unreachableStepUrl { propertyDetailsRoute }
            task(journey.rentFrequencyAndAmountTask) {
                initialStep()
                nextStep { journey.cyaStep }
                withAdditionalContentProperty {
                    "title" to "propertyDetails.update.title"
                }
            }
            step(journey.cyaStep) {
                routeSegment(UpdateRentFrequencyAndAmountCyaStep.ROUTE_SEGMENT)
                parents { journey.rentFrequencyAndAmountTask.isComplete() }
                nextUrl { propertyDetailsRoute }
            }
            configureStep(journey.rentFrequency) {
                withAdditionalContentProperty {
                    "heading" to "forms.update.rentFrequency.heading"
                }
            }
            configureStep(journey.rentAmount) {
                withAdditionalContentProperty {
                    "heading" to state.getUpdateRentAmountHeading()
                }
            }
        }
    }

    private fun checkYourAnswersJourneyMap(
        state: UpdateRentFrequencyAndAmountJourney,
        checkingAnswersFor: String,
        propertyId: Long,
    ): Map<String, StepLifecycleOrchestrator> {
        val propertyDetailsRoute = PropertyDetailsController.getPropertyDetailsPath(propertyId)

        return journey(state) {
            unreachableStepUrl { propertyDetailsRoute }
            when (checkingAnswersFor) {
                RentFrequencyStep.ROUTE_SEGMENT -> checkAnswerTask(journey.rentFrequencyAndAmountTask)
                RentAmountStep.ROUTE_SEGMENT -> {
                    checkAnswerStep(journey.rentAmount, RentAmountStep.ROUTE_SEGMENT)
                    step(journey.rentFrequency) {
                        routeSegment(RentFrequencyStep.ROUTE_SEGMENT)
                        parents { Unvisitable() }
                        nextDestination { Destination.Nowhere() }
                    }
                }
                else -> throw IllegalStateException("Unknown step being checked: $checkingAnswersFor")
            }
            step(journey.finishCyaStep) {
                parents { journey.rentFrequencyAndAmountTask.isComplete() }
                nextDestination { Destination.Nowhere() }
            }
            configureStep(journey.rentFrequency) {
                withAdditionalContentProperty {
                    "heading" to "forms.update.rentFrequency.heading"
                }
            }
            configureStep(journey.rentAmount) {
                withAdditionalContentProperty {
                    "heading" to state.getUpdateRentAmountHeading()
                }
            }
        }
    }

    fun initializeJourneyState(
        ownershipId: Long,
        user: Principal,
    ): String = stateFactory.getObject().initializeOrRestoreState(Pair(ownershipId, user))
}

@JourneyFrameworkComponent
class UpdateRentFrequencyAndAmountJourney(
    // RentFrequencyAndAmount task
    override val rentFrequencyAndAmountTask: RentFrequencyAndAmountTask,
    override val rentFrequency: RentFrequencyStep,
    override val rentAmount: RentAmountStep,
    // Check your answers step
    override val cyaStep: UpdateRentFrequencyAndAmountCyaStep,
    journeyStateService: JourneyStateService,
    journeyName: String = "rent frequency and amount",
    override val finishCyaStep: FinishCyaJourneyStep,
    override val stateFactory: ObjectFactory<UpdateRentFrequencyAndAmountJourneyState>,
) : AbstractPropertyOwnershipUpdateJourneyState(journeyStateService, journeyName),
    UpdateRentFrequencyAndAmountJourneyState {
    private val delegateProvider = JourneyStateDelegateProvider(journeyStateService)
    override var propertyId: Long by delegateProvider.requiredImmutableDelegate("propertyId")
    override var lastModifiedDate: String by delegateProvider.requiredImmutableDelegate("lastModifiedDate")
    override var cyaJourneys: Map<String, String> = mapOf()
    override var checkingAnswersFor: String? by delegateProvider.nullableDelegate("checkingAnswersFor")
    override var cyaRouteSegment: String? by delegateProvider.nullableDelegate("cyaRouteSegment")
    override var originalJourneyUpdated: Instant? by delegateProvider.nullableDelegate("originalJourneyUpdated")
}

interface UpdateRentFrequencyAndAmountJourneyState :
    RentFrequencyAndAmountState,
    CheckYourAnswersJourneyState {
    val rentFrequencyAndAmountTask: RentFrequencyAndAmountTask
    override val cyaStep: UpdateRentFrequencyAndAmountCyaStep
    val propertyId: Long
    val lastModifiedDate: String
}
