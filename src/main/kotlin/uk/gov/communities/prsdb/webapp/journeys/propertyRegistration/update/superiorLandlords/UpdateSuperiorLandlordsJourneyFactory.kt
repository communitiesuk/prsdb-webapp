package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.superiorLandlords

import kotlinx.datetime.Instant
import org.springframework.beans.factory.ObjectFactory
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.PEOPLE_RESPONSIBLE_FRAGMENT
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
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.SuperiorLandlordsTask
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.useContinueButtons
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@PrsdbWebService
class UpdateSuperiorLandlordsJourneyFactory(
    private val stateFactory: ObjectFactory<UpdateSuperiorLandlordsJourney>,
    private val propertyOwnershipService: PropertyOwnershipService,
) {
    final fun createJourneySteps(propertyId: Long): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()
        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(propertyId)

        if (!propertyOwnership.ownershipType.requiresSuperiorLandlordInfo()) {
            throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Property ownership $propertyId does not require superior landlord info",
            )
        }

        if (!state.isStateInitialized) {
            state.propertyId = propertyId
            state.lastModifiedDate = propertyOwnership.getOwnershipStructureMostRecentlyUpdated().toString()
            state.isStateInitialized = true
        }

        if (state.propertyId != propertyId) {
            throw PrsdbWebException("Journey state propertyId ${state.propertyId} does not match provided propertyId $propertyId")
        }

        val peopleResponsibleRoute = "${PropertyDetailsController.getPropertyDetailsPath(propertyId)}#$PEOPLE_RESPONSIBLE_FRAGMENT"
        val superiorLandlordsListStep = state.superiorLandlordsTask.superiorLandlordsDetailsTask.superiorLandlordsListStep

        return journey(state) {
            unreachableStepUrl { peopleResponsibleRoute }
            step(journey.startStep) {
                routeSegment(StartSuperiorLandlordsUpdateStep.ROUTE_SEGMENT)
                initialStep()
                nextStep { journey.routingStep }
            }
            step(journey.routingStep) {
                parents { journey.startStep.isComplete() }
                nextDestination { mode ->
                    when (mode) {
                        SuperiorLandlordsUpdateRouteMode.HAS_SUPERIOR_LANDLORDS -> Destination(superiorLandlordsListStep)
                        SuperiorLandlordsUpdateRouteMode.NO_SUPERIOR_LANDLORDS -> Destination(journey.superiorLandlordsTask.firstStep)
                    }
                }
            }
            task(journey.superiorLandlordsTask) {
                parents { journey.routingStep.isComplete() }
                backUrl { peopleResponsibleRoute }
                nextDestination {
                    if (journey.superiorLandlordsTask.anyoneInOwnershipChainStep.outcome == YesOrNo.NO) {
                        Destination(journey.checkYourAnswersStep)
                    } else {
                        Destination(journey.completeSuperiorLandlordsUpdateStep)
                    }
                }
            }
            step(journey.checkYourAnswersStep) {
                routeSegment(UpdateSuperiorLandlordsCheckYourAnswersStep.ROUTE_SEGMENT)
                parents { journey.superiorLandlordsTask.anyoneInOwnershipChainStep.hasOutcome(YesOrNo.NO) }
                nextStep { journey.completeSuperiorLandlordsUpdateStep }
            }
            step(journey.completeSuperiorLandlordsUpdateStep) {
                parents {
                    OrParents(
                        journey.checkYourAnswersStep.isComplete(),
                        superiorLandlordsListStep.isComplete(),
                    )
                }
                nextUrl { peopleResponsibleRoute }
            }
            useContinueButtons(journey.superiorLandlordsTask)
            // There's no check your answers page when there are superior landlords, so the list page submits the update.
            configureStep(superiorLandlordsListStep) {
                withAdditionalContentProperties {
                    mapOf(
                        "submitButtonText" to "forms.buttons.confirmAndSubmitUpdate",
                        "submitButton" to "transactionSubmitButton",
                        "insetText" to "forms.update.superiorLandlords.notificationInset",
                        "showWarning" to true,
                    )
                }
            }
        }
    }

    fun initializeJourneyState(
        seed: Any?,
        currentLastModifiedDate: java.time.Instant,
    ): String = stateFactory.getObject().initialiseOrRestoreStateReinitialisingIfOutdated(seed, currentLastModifiedDate)
}

@JourneyFrameworkComponent
class UpdateSuperiorLandlordsJourney(
    // Seeds the superior landlords list and ownership chain answer from the database
    override val startStep: StartSuperiorLandlordsUpdateStep,
    override val routingStep: SuperiorLandlordsUpdateRoutingStep,
    // Ownership chain question, superior landlord list, details and removal steps shared with property registration
    override val superiorLandlordsTask: SuperiorLandlordsTask,
    override val checkYourAnswersStep: UpdateSuperiorLandlordsCheckYourAnswersStep,
    override val completeSuperiorLandlordsUpdateStep: CompleteSuperiorLandlordsUpdateStep,
    override val finishCyaStep: FinishCyaJourneyStep,
    journeyStateService: JourneyStateService,
    journeyName: String = "superior landlords",
    override val stateFactory: ObjectFactory<UpdateSuperiorLandlordsJourneyState>,
) : AbstractPropertyOwnershipUpdateJourneyState(journeyStateService, journeyName),
    UpdateSuperiorLandlordsJourneyState {
    override val cyaStep: JourneyStep.RequestableStep<*, *, *>
        get() = checkYourAnswersStep

    override var propertyId: Long by delegateProvider.requiredImmutableDelegate("propertyId")
    override var lastModifiedDate: String by delegateProvider.requiredImmutableDelegate(LAST_MODIFIED_DATE_KEY)
    override var superiorLandlordsInitialised: Boolean? by delegateProvider.nullableDelegate("superiorLandlordsInitialised")

    override var originalJourneyUpdated: Instant? by delegateProvider.nullableDelegate("originalJourneyUpdated")
    override var checkingAnswersFor: String? by delegateProvider.nullableDelegate("checkingAnswersFor")
    override var cyaJourneys: Map<String, String> = mapOf()
    override var cyaUrlPath: String? by delegateProvider.nullableDelegate("cyaRouteSegment")
}

interface UpdateSuperiorLandlordsJourneyState : CheckYourAnswersJourneyState {
    val startStep: StartSuperiorLandlordsUpdateStep
    val routingStep: SuperiorLandlordsUpdateRoutingStep
    val superiorLandlordsTask: SuperiorLandlordsTask
    val checkYourAnswersStep: UpdateSuperiorLandlordsCheckYourAnswersStep
    val completeSuperiorLandlordsUpdateStep: CompleteSuperiorLandlordsUpdateStep
    val propertyId: Long
    val lastModifiedDate: String
    var superiorLandlordsInitialised: Boolean?
}
