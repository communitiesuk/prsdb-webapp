package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.propertyManager

import kotlinx.datetime.Instant
import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractPropertyOwnershipUpdateJourneyState
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FinishCyaJourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.PropertyManagerTask
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyManagerDataModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@PrsdbWebService
class UpdatePropertyManagerJourneyFactory(
    private val stateFactory: ObjectFactory<UpdatePropertyManagerJourney>,
    private val propertyOwnershipService: PropertyOwnershipService,
) {
    final fun createJourneySteps(propertyId: Long): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        if (!state.isStateInitialized) {
            state.propertyId = propertyId
            state.lastModifiedDate = propertyOwnershipService.getLastModifiedDateIncludingPropertyManager(propertyId).toString()
            state.isStateInitialized = true
        }

        if (state.propertyId != propertyId) {
            throw PrsdbWebException("Journey state propertyId ${state.propertyId} does not match provided propertyId $propertyId")
        }

        val propertyDetailsRoute = PropertyDetailsController.getPropertyDetailsPath(propertyId)

        return journey(state) {
            unreachableStepUrl { propertyDetailsRoute }
            step(journey.startStep) {
                routeSegment(StartPropertyManagerUpdateStep.ROUTE_SEGMENT)
                initialStep()
                nextStep { journey.routingStep }
            }
            step(journey.routingStep) {
                parents { journey.startStep.isComplete() }
                nextDestination { mode ->
                    when (mode) {
                        PropertyManagerUpdateRouteMode.HAS_PROPERTY_MANAGER -> {
                            Destination(journey.propertyManagerTask.checkPropertyManagerStep)
                        }

                        PropertyManagerUpdateRouteMode.NO_PROPERTY_MANAGER_ADDED -> {
                            Destination(journey.propertyManagerTask.firstStep)
                        }
                    }
                }
            }
            task(journey.propertyManagerTask) {
                parents { journey.routingStep.isComplete() }
                backUrl { propertyDetailsRoute }
                nextDestination {
                    if (journey.propertyManagerTask.propertyManagerStep.outcome == YesOrNo.NO) {
                        Destination(journey.checkYourAnswersStep)
                    } else {
                        Destination(journey.completePropertyManagerUpdateStep)
                    }
                }
                configureStep(journey.propertyManagerTask.checkPropertyManagerStep) {
                    withAdditionalContentProperties {
                        mapOf(
                            "insetText" to "forms.update.propertyManager.check.insetText",
                            "showWarning" to true,
                            "submitButton" to "transactionSubmitButton",
                            "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
                        )
                    }
                }
                configureStep(journey.propertyManagerTask.removePropertyManagerStep) {
                    withAdditionalContentProperty {
                        "insetText" to "forms.update.propertyManager.remove.insetText"
                    }
                }
            }
            step(journey.checkYourAnswersStep) {
                routeSegment(UpdatePropertyManagerCheckYourAnswersStep.ROUTE_SEGMENT)
                parents { journey.propertyManagerTask.propertyManagerStep.hasOutcome(YesOrNo.NO) }
                nextStep { journey.completePropertyManagerUpdateStep }
            }
            step(journey.completePropertyManagerUpdateStep) {
                parents {
                    OrParents(
                        journey.checkYourAnswersStep.isComplete(),
                        journey.propertyManagerTask.checkPropertyManagerStep.isComplete(),
                    )
                }
                nextUrl { propertyDetailsRoute }
            }
        }
    }

    fun initializeJourneyState(
        seed: Any?,
        currentLastModifiedDate: java.time.Instant,
    ): String = stateFactory.getObject().initialiseOrRestoreStateReinitialisingIfOutdated(seed, currentLastModifiedDate)
}

@JourneyFrameworkComponent
class UpdatePropertyManagerJourney(
    override val startStep: StartPropertyManagerUpdateStep,
    override val routingStep: PropertyManagerUpdateRoutingStep,
    override val propertyManagerTask: PropertyManagerTask,
    override val checkYourAnswersStep: UpdatePropertyManagerCheckYourAnswersStep,
    override val completePropertyManagerUpdateStep: CompletePropertyManagerUpdateStep,
    override val finishCyaStep: FinishCyaJourneyStep,
    journeyStateService: JourneyStateService,
    journeyName: String = "property manager",
    override val stateFactory: ObjectFactory<UpdatePropertyManagerJourneyState>,
) : AbstractPropertyOwnershipUpdateJourneyState(journeyStateService, journeyName),
    UpdatePropertyManagerJourneyState {
    override val cyaStep: JourneyStep.RequestableStep<*, *, *>
        get() = checkYourAnswersStep

    override var propertyId: Long by delegateProvider.requiredImmutableDelegate("propertyId")
    override var lastModifiedDate: String by delegateProvider.requiredImmutableDelegate(LAST_MODIFIED_DATE_KEY)
    override var propertyManagerInitialised: Boolean? by delegateProvider.nullableDelegate("propertyManagerInitialised")
    override var originalPropertyManagerName: String? by delegateProvider.nullableDelegate("originalPropertyManagerName")
    override var originalPropertyManagerEmail: String? by delegateProvider.nullableDelegate("originalPropertyManagerEmail")

    override var originalJourneyUpdated: Instant? by delegateProvider.nullableDelegate("originalJourneyUpdated")
    override var checkingAnswersFor: String? by delegateProvider.nullableDelegate("checkingAnswersFor")
    override var cyaJourneys: Map<String, String> = mapOf()
    override var cyaUrlPath: String? by delegateProvider.nullableDelegate("cyaRouteSegment")
}

interface UpdatePropertyManagerJourneyState : CheckYourAnswersJourneyState {
    val startStep: StartPropertyManagerUpdateStep
    val routingStep: PropertyManagerUpdateRoutingStep
    val propertyManagerTask: PropertyManagerTask
    val checkYourAnswersStep: UpdatePropertyManagerCheckYourAnswersStep
    val completePropertyManagerUpdateStep: CompletePropertyManagerUpdateStep
    val propertyId: Long
    val lastModifiedDate: String
    var propertyManagerInitialised: Boolean?
    var originalPropertyManagerName: String?
    var originalPropertyManagerEmail: String?

    val originalPropertyManager: PropertyManagerDataModel?
        get() =
            originalPropertyManagerName?.let { name ->
                originalPropertyManagerEmail?.let { email -> PropertyManagerDataModel(name, email) }
            }
}
