package uk.gov.communities.prsdb.webapp.journeys.lettingAgentInvitation

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentPropertyDetailsController
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.AndParents
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.lettingAgentInvitation.steps.ConfirmationStep
import uk.gov.communities.prsdb.webapp.journeys.lettingAgentInvitation.steps.EnterPasswordStep
import uk.gov.communities.prsdb.webapp.journeys.lettingAgentInvitation.steps.HasPasswordStep
import uk.gov.communities.prsdb.webapp.journeys.lettingAgentInvitation.steps.PasswordStatus
import uk.gov.communities.prsdb.webapp.journeys.lettingAgentInvitation.steps.SetPasswordStep
import uk.gov.communities.prsdb.webapp.journeys.lettingAgentInvitation.steps.StartStep
import uk.gov.communities.prsdb.webapp.journeys.lettingAgentInvitation.steps.StoreAccessStep
import java.util.UUID

@PrsdbWebService
class LettingAgentInvitationJourneyFactory(
    private val stateFactory: ObjectFactory<LettingAgentInvitationJourney>,
) {
    fun createJourneySteps(): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        return journey(state) {
            unreachableStepStep { journey.startStep }
            configure {
                withAdditionalContentProperty { "title" to "lettingAgentInvitation.title" }
            }
            step(journey.startStep) {
                routeSegment(StartStep.ROUTE_SEGMENT)
                initialStep()
                nextStep { journey.hasPasswordStep }
            }
            step(journey.hasPasswordStep) {
                parents { journey.startStep.isComplete() }
                nextStep { status ->
                    when (status) {
                        PasswordStatus.HAS_PASSWORD -> journey.enterPasswordStep
                        PasswordStatus.NO_PASSWORD -> journey.setPasswordStep
                    }
                }
            }
            step(journey.setPasswordStep) {
                routeSegment(SetPasswordStep.ROUTE_SEGMENT)
                parents { journey.hasPasswordStep.hasOutcome(PasswordStatus.NO_PASSWORD) }
                nextStep { journey.storeAccessStep }
            }
            step(journey.enterPasswordStep) {
                routeSegment(EnterPasswordStep.ROUTE_SEGMENT)
                parents { journey.hasPasswordStep.hasOutcome(PasswordStatus.HAS_PASSWORD) }
                nextStep { journey.storeAccessStep }
            }
            step(journey.storeAccessStep) {
                parents {
                    OrParents(
                        journey.setPasswordStep.isComplete(),
                        journey.enterPasswordStep.isComplete(),
                    )
                }
                nextDestination {
                    val propertyDetailsDestination =
                        Destination.ExternalUrl(
                            LettingAgentPropertyDetailsController.getLettingAgentPropertyDetailsPath(
                                UUID.fromString(journey.invitationToken),
                            ),
                        )
                    when (journey.hasPasswordStep.outcome) {
                        PasswordStatus.NO_PASSWORD -> Destination(journey.confirmationStep)
                        PasswordStatus.HAS_PASSWORD -> propertyDetailsDestination
                        null -> throw PrsdbWebException(
                            "hasExistingPassword outcome is missing, so the next destination cannot be determined",
                        )
                    }
                }
            }
            step(journey.confirmationStep) {
                routeSegment(ConfirmationStep.ROUTE_SEGMENT)
                parents {
                    AndParents(
                        journey.storeAccessStep.isComplete(),
                        journey.hasPasswordStep.hasOutcome(PasswordStatus.NO_PASSWORD),
                    )
                }
                backDestination { Destination.Nowhere() }
                nextDestination {
                    Destination.ExternalUrl(
                        LettingAgentPropertyDetailsController.getLettingAgentPropertyDetailsPath(
                            UUID.fromString(journey.invitationToken),
                        ),
                    )
                }
            }
        }
    }

    fun initializeJourneyState(token: UUID): String {
        val state = stateFactory.getObject()
        return state.initializeState(token)
    }
}

@JourneyFrameworkComponent("lettingAgentInvitationJourney")
class LettingAgentInvitationJourney(
    override val startStep: StartStep,
    override val hasPasswordStep: HasPasswordStep,
    override val setPasswordStep: SetPasswordStep,
    override val confirmationStep: ConfirmationStep,
    override val enterPasswordStep: EnterPasswordStep,
    override val storeAccessStep: StoreAccessStep,
    journeyStateService: JourneyStateService,
) : AbstractJourneyState(journeyStateService),
    LettingAgentInvitationJourneyState {
    override var invitationToken: String by delegateProvider.requiredImmutableDelegate("invitationToken")
    override var isStateInitialized: Boolean by delegateProvider.requiredDelegate("isStateInitialized", false)
    override var hasExistingPassword: Boolean? by delegateProvider.nullableDelegate("hasExistingPassword")
    override var hasSetNewPassword: Boolean? by delegateProvider.nullableDelegate("hasSetNewPassword")
    override var hasEnteredPassword: Boolean? by delegateProvider.nullableDelegate("hasEnteredPassword")

    override fun generateJourneyId(seed: Any?): String {
        val token = seed as? UUID
        return super<AbstractJourneyState>.generateJourneyId(
            token?.let { "Letting agent invitation journey for token $it at time ${System.currentTimeMillis()}" },
        )
    }
}

interface LettingAgentInvitationJourneyState : JourneyState {
    val startStep: StartStep
    val hasPasswordStep: HasPasswordStep
    val setPasswordStep: SetPasswordStep
    val confirmationStep: ConfirmationStep
    val enterPasswordStep: EnterPasswordStep
    val storeAccessStep: StoreAccessStep
    var invitationToken: String
    var isStateInitialized: Boolean
    var hasExistingPassword: Boolean?

    var hasSetNewPassword: Boolean?

    var hasEnteredPassword: Boolean?
}
