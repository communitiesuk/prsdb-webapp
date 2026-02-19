package uk.gov.communities.prsdb.webapp.journeys.joinProperty

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.JoinPropertyController.Companion.JOIN_PROPERTY_CONFIRMATION_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateDelegateProvider
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.ConfirmPropertyStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.FindPropertyByPrnStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.FindPropertyStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.JoinPropertyAlreadyRegisteredStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.NoMatchingPropertiesStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.PendingRequestStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.PrnNotFoundStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.PropertyNotRegisteredStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.RequestRejectedStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.SelectPropertyStep
import java.security.Principal

@PrsdbWebService
class JoinPropertyJourneyFactory(
    private val stateFactory: ObjectFactory<JoinPropertyJourney>,
) {
    fun createJourneySteps(): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        return journey(state) {
            configure {
                withAdditionalContentProperty { "title" to "joinProperty.title" }
            }

            // All steps in linear path for viewability during development
            // Address search path
            step(journey.findPropertyStep) {
                routeSegment(FindPropertyStep.ROUTE_SEGMENT)
                initialStep()
                // TODO: PDJB-274 - Add conditional routing based on search results
                nextStep { journey.noMatchingPropertiesStep }
            }
            step(journey.noMatchingPropertiesStep) {
                routeSegment(NoMatchingPropertiesStep.ROUTE_SEGMENT)
                parents { journey.findPropertyStep.isComplete() }
                // TODO: PDJB-276 - Connect when no properties match search
                nextStep { journey.propertyNotRegisteredStep }
            }
            step(journey.propertyNotRegisteredStep) {
                routeSegment(PropertyNotRegisteredStep.ROUTE_SEGMENT)
                parents { journey.noMatchingPropertiesStep.isComplete() }
                // TODO: PDJB-283 - Connect when property is not registered
                nextStep { journey.selectPropertyStep }
            }
            step(journey.selectPropertyStep) {
                routeSegment(SelectPropertyStep.ROUTE_SEGMENT)
                parents { journey.propertyNotRegisteredStep.isComplete() }
                // TODO: PDJB-275 - Add conditional routing to error pages
                nextStep { journey.alreadyRegisteredStep }
            }
            step(journey.alreadyRegisteredStep) {
                routeSegment(JoinPropertyAlreadyRegisteredStep.ROUTE_SEGMENT)
                parents { journey.selectPropertyStep.isComplete() }
                // TODO: PDJB-280 - Connect when user is already registered
                nextStep { journey.pendingRequestStep }
            }
            step(journey.pendingRequestStep) {
                routeSegment(PendingRequestStep.ROUTE_SEGMENT)
                parents { journey.alreadyRegisteredStep.isComplete() }
                // TODO: PDJB-281 - Connect when user has pending request
                nextStep { journey.requestRejectedStep }
            }
            step(journey.requestRejectedStep) {
                routeSegment(RequestRejectedStep.ROUTE_SEGMENT)
                parents { journey.pendingRequestStep.isComplete() }
                // TODO: PDJB-282 - Entry point from dashboard notification
                nextStep { journey.findPropertyByPrnStep }
            }

            // PRN search path
            step(journey.findPropertyByPrnStep) {
                routeSegment(FindPropertyByPrnStep.ROUTE_SEGMENT)
                parents { journey.requestRejectedStep.isComplete() }
                // TODO: PDJB-277 - Connect from FindProperty page link
                nextStep { journey.prnNotFoundStep }
            }
            step(journey.prnNotFoundStep) {
                routeSegment(PrnNotFoundStep.ROUTE_SEGMENT)
                parents { journey.findPropertyByPrnStep.isComplete() }
                // TODO: PDJB-279 - Connect when PRN not found
                nextStep { journey.confirmPropertyStep }
            }
            step(journey.confirmPropertyStep) {
                routeSegment(ConfirmPropertyStep.ROUTE_SEGMENT)
                parents { journey.prnNotFoundStep.isComplete() }
                // TODO: PDJB-278 - Add conditional routing to error pages
                nextUrl { JOIN_PROPERTY_CONFIRMATION_ROUTE }
            }
        }
    }

    fun initializeJourneyState(user: Principal) = stateFactory.getObject().initializeState(user)
}

@JourneyFrameworkComponent
class JoinPropertyJourney(
    override val findPropertyStep: FindPropertyStep,
    override val findPropertyByPrnStep: FindPropertyByPrnStep,
    override val selectPropertyStep: SelectPropertyStep,
    override val confirmPropertyStep: ConfirmPropertyStep,
    override val alreadyRegisteredStep: JoinPropertyAlreadyRegisteredStep,
    override val pendingRequestStep: PendingRequestStep,
    override val requestRejectedStep: RequestRejectedStep,
    override val noMatchingPropertiesStep: NoMatchingPropertiesStep,
    override val propertyNotRegisteredStep: PropertyNotRegisteredStep,
    override val prnNotFoundStep: PrnNotFoundStep,
    journeyStateService: JourneyStateService,
    delegateProvider: JourneyStateDelegateProvider,
) : AbstractJourneyState(journeyStateService),
    JoinPropertyJourneyState {
    override fun generateJourneyId(seed: Any?): String {
        val user = seed as? Principal
        return super<AbstractJourneyState>.generateJourneyId(user?.let { generateSeedForUser(user) })
    }

    companion object {
        private fun generateSeedForUser(user: Principal): String =
            "Join property journey for ${user.name} at time ${System.currentTimeMillis()}"
    }
}

interface JoinPropertyJourneyState : JourneyState {
    val findPropertyStep: FindPropertyStep
    val findPropertyByPrnStep: FindPropertyByPrnStep
    val selectPropertyStep: SelectPropertyStep
    val confirmPropertyStep: ConfirmPropertyStep
    val alreadyRegisteredStep: JoinPropertyAlreadyRegisteredStep
    val pendingRequestStep: PendingRequestStep
    val requestRejectedStep: RequestRejectedStep
    val noMatchingPropertiesStep: NoMatchingPropertiesStep
    val propertyNotRegisteredStep: PropertyNotRegisteredStep
    val prnNotFoundStep: PrnNotFoundStep
}
