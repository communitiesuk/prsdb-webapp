package uk.gov.communities.prsdb.webapp.journeys.joinProperty

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.JoinPropertyController.Companion.JOIN_PROPERTY_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateDelegateProvider
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.ConfirmPropertyStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.FindPropertyPrnStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.FindPropertyStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.JoinPropertyAlreadyRegisteredStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.NoMatchingPropertiesStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.PendingRequestStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.PrnNotFoundStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.PropertyNotRegisteredStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.RequestRejectedStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.RequestSentStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.SelectPropertyStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps.StartPageStep
import java.security.Principal

@PrsdbWebService
class JoinPropertyJourneyFactory(
    private val stateFactory: ObjectFactory<JoinPropertyJourney>,
) {
    fun createJourneySteps(): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        return journey(state) {
            unreachableStepStep { journey.startPageStep }
            configure {
                withAdditionalContentProperty { "title" to "joinProperty.title" }
                withAdditionalContentProperty { "backUrl" to LANDLORD_DASHBOARD_URL }
            }

            // Main happy path
            step(journey.startPageStep) {
                routeSegment(StartPageStep.ROUTE_SEGMENT)
                initialStep()
                nextStep { journey.findPropertyStep }
            }
            step(journey.findPropertyStep) {
                routeSegment(FindPropertyStep.ROUTE_SEGMENT)
                parents { journey.startPageStep.isComplete() }
                // TODO: PDJB-274 - Add conditional routing based on search results
                nextStep { journey.selectPropertyStep }
            }
            step(journey.selectPropertyStep) {
                routeSegment(SelectPropertyStep.ROUTE_SEGMENT)
                parents { journey.findPropertyStep.isComplete() }
                // TODO: PDJB-275 - Add conditional routing to error pages
                nextStep { journey.requestSentStep }
            }
            step(journey.requestSentStep) {
                routeSegment(RequestSentStep.ROUTE_SEGMENT)
                parents { journey.selectPropertyStep.isComplete() }
                nextUrl { LANDLORD_DASHBOARD_URL }
            }

            // PRN alternative path (stub - not yet connected)
            step(journey.findPropertyPrnStep) {
                routeSegment(FindPropertyPrnStep.ROUTE_SEGMENT)
                parents { journey.startPageStep.isComplete() }
                // TODO: PDJB-277 - Connect from FindProperty page link
                nextStep { journey.confirmPropertyStep }
            }
            step(journey.confirmPropertyStep) {
                routeSegment(ConfirmPropertyStep.ROUTE_SEGMENT)
                parents { journey.findPropertyPrnStep.isComplete() }
                // TODO: PDJB-278 - Add conditional routing to error pages
                nextStep { journey.requestSentStep }
            }

            // Error pages (stubs - not yet connected to flow)
            step(journey.alreadyRegisteredStep) {
                routeSegment(JoinPropertyAlreadyRegisteredStep.ROUTE_SEGMENT)
                parents { journey.selectPropertyStep.isComplete() }
                // TODO: PDJB-280 - Connect when user is already registered
                nextUrl { "$JOIN_PROPERTY_ROUTE/${FindPropertyStep.ROUTE_SEGMENT}" }
            }
            step(journey.pendingRequestStep) {
                routeSegment(PendingRequestStep.ROUTE_SEGMENT)
                parents { journey.selectPropertyStep.isComplete() }
                // TODO: PDJB-281 - Connect when user has pending request
                nextUrl { LANDLORD_DASHBOARD_URL }
            }
            step(journey.requestRejectedStep) {
                routeSegment(RequestRejectedStep.ROUTE_SEGMENT)
                parents { journey.startPageStep.isComplete() }
                // TODO: PDJB-282 - Entry point from dashboard notification
                nextStep { journey.findPropertyStep }
            }
            step(journey.noMatchingPropertiesStep) {
                routeSegment(NoMatchingPropertiesStep.ROUTE_SEGMENT)
                parents { journey.findPropertyStep.isComplete() }
                // TODO: PDJB-276 - Connect when no properties match search
                nextStep { journey.findPropertyStep }
            }
            step(journey.propertyNotRegisteredStep) {
                routeSegment(PropertyNotRegisteredStep.ROUTE_SEGMENT)
                parents { journey.findPropertyStep.isComplete() }
                // TODO: PDJB-283 - Connect when property is not registered
                nextUrl { LANDLORD_DASHBOARD_URL }
            }
            step(journey.prnNotFoundStep) {
                routeSegment(PrnNotFoundStep.ROUTE_SEGMENT)
                parents { journey.findPropertyPrnStep.isComplete() }
                // TODO: PDJB-279 - Connect when PRN not found
                nextStep { journey.findPropertyPrnStep }
            }
        }
    }

    fun initializeJourneyState(user: Principal) = stateFactory.getObject().initializeState(user)
}

@JourneyFrameworkComponent
class JoinPropertyJourney(
    override val startPageStep: StartPageStep,
    override val findPropertyStep: FindPropertyStep,
    override val findPropertyPrnStep: FindPropertyPrnStep,
    override val selectPropertyStep: SelectPropertyStep,
    override val confirmPropertyStep: ConfirmPropertyStep,
    override val requestSentStep: RequestSentStep,
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
    val startPageStep: StartPageStep
    val findPropertyStep: FindPropertyStep
    val findPropertyPrnStep: FindPropertyPrnStep
    val selectPropertyStep: SelectPropertyStep
    val confirmPropertyStep: ConfirmPropertyStep
    val requestSentStep: RequestSentStep
    val alreadyRegisteredStep: JoinPropertyAlreadyRegisteredStep
    val pendingRequestStep: PendingRequestStep
    val requestRejectedStep: RequestRejectedStep
    val noMatchingPropertiesStep: NoMatchingPropertiesStep
    val propertyNotRegisteredStep: PropertyNotRegisteredStep
    val prnNotFoundStep: PrnNotFoundStep
}
