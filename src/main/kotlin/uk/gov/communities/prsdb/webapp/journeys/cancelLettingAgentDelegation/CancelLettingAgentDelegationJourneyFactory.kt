package uk.gov.communities.prsdb.webapp.journeys.cancelLettingAgentDelegation

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.CancelLettingAgentDelegationController
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.cancelLettingAgentDelegation.stepConfig.AreYouSureStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.PropertyOwnershipJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.initialiseFromPropertyOwnershipId

@PrsdbWebService
class CancelLettingAgentDelegationJourneyFactory(
    private val stateFactory: ObjectFactory<CancelLettingAgentDelegationJourney>,
) {
    fun createJourneySteps(propertyOwnershipId: Long): Map<String, StepLifecycleOrchestrator> {
        val state = getInitializedState(propertyOwnershipId)

        return journey(state) {
            unreachableStepStep { journey.areYouSureStep }
            step(journey.areYouSureStep) {
                routeSegment(AreYouSureStep.ROUTE_SEGMENT)
                initialStep()
                backUrl { PropertyDetailsController.getPropertyDetailsPath(propertyOwnershipId) }
                nextUrl {
                    "${
                        CancelLettingAgentDelegationController.getRemoveLettingAgentBasePath(propertyOwnershipId)
                    }/$CONFIRMATION_PATH_SEGMENT"
                }
            }
        }
    }

    private fun getInitializedState(propertyOwnershipId: Long): CancelLettingAgentDelegationJourney =
        stateFactory.getObject().initialiseFromPropertyOwnershipId(propertyOwnershipId)

    fun initializeJourneyState(propertyOwnershipId: Long): String = stateFactory.getObject().initializeState(propertyOwnershipId)
}

@JourneyFrameworkComponent
class CancelLettingAgentDelegationJourney(
    override val areYouSureStep: AreYouSureStep,
    journeyStateService: JourneyStateService,
) : AbstractJourneyState(journeyStateService),
    CancelLettingAgentDelegationJourneyState {
    override var isStateInitialized: Boolean by delegateProvider.requiredDelegate("isStateInitialized", false)
    override var propertyOwnershipId: Long by delegateProvider.requiredImmutableDelegate("propertyOwnershipId")

    override fun generateJourneyId(seed: Any?): String {
        val propertyOwnershipId = seed as? Long
        return super<AbstractJourneyState>.generateJourneyId(
            propertyOwnershipId?.let { generateSeedForPropertyOwnership(it) },
        )
    }

    companion object {
        private fun generateSeedForPropertyOwnership(propertyOwnershipId: Long): String =
            "Cancel letting agent delegation journey for property $propertyOwnershipId at time ${System.currentTimeMillis()}"
    }
}

interface CancelLettingAgentDelegationJourneyState : PropertyOwnershipJourneyState {
    val areYouSureStep: AreYouSureStep
}
