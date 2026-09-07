package uk.gov.communities.prsdb.webapp.journeys.delegateToLettingAgent

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.DelegateToLettingAgentController
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.delegateToLettingAgent.stepConfig.AllowLettingAgentStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.PropertyOwnershipJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.initialiseFromPropertyOwnershipId

@PrsdbWebService
class DelegateToLettingAgentJourneyFactory(
    private val stateFactory: ObjectFactory<DelegateToLettingAgentJourney>,
) {
    fun createJourneySteps(propertyOwnershipId: Long): Map<String, StepLifecycleOrchestrator> {
        val state = getInitializedState(propertyOwnershipId)

        return journey(state) {
            unreachableStepUrl { PropertyDetailsController.getPropertyDetailsPath(propertyOwnershipId) }
            configure {
                withAdditionalContentProperty { "title" to "delegateToLettingAgent.title" }
            }
            step(journey.allowLettingAgentStep) {
                routeSegment(AllowLettingAgentStep.ROUTE_SEGMENT)
                initialStep()
                backUrl { PropertyDetailsController.getPropertyDetailsPath(propertyOwnershipId) }
                nextUrl {
                    "${
                        DelegateToLettingAgentController.getDelegateToLettingAgentBasePath(
                            propertyOwnershipId,
                        )
                    }/$CONFIRMATION_PATH_SEGMENT"
                }
            }
        }
    }

    fun initializeJourneyState(propertyOwnershipId: Long): String = stateFactory.getObject().initializeState(propertyOwnershipId)

    private fun getInitializedState(propertyOwnershipId: Long): DelegateToLettingAgentJourney =
        stateFactory.getObject().initialiseFromPropertyOwnershipId(propertyOwnershipId)
}

@JourneyFrameworkComponent
class DelegateToLettingAgentJourney(
    val allowLettingAgentStep: AllowLettingAgentStep,
    journeyStateService: JourneyStateService,
) : AbstractJourneyState(journeyStateService),
    DelegateToLettingAgentJourneyState {
    override var isStateInitialized: Boolean by delegateProvider.requiredDelegate("isStateInitialized", false)
    override var propertyOwnershipId: Long by delegateProvider.requiredImmutableDelegate("propertyOwnershipId")

    override fun generateJourneyId(seed: Any?): String {
        val propertyOwnershipId = seed as? Long
        return super<AbstractJourneyState>.generateJourneyId(
            propertyOwnershipId?.let { "Delegate to letting agent journey for property $it at time ${System.currentTimeMillis()}" },
        )
    }
}

interface DelegateToLettingAgentJourneyState : PropertyOwnershipJourneyState
