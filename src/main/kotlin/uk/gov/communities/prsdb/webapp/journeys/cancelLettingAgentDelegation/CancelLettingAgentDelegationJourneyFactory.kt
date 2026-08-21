package uk.gov.communities.prsdb.webapp.journeys.cancelLettingAgentDelegation

import org.springframework.beans.factory.ObjectFactory
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.CancelLettingAgentDelegationController
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.cancelLettingAgentDelegation.stepConfig.AreYouSureMode
import uk.gov.communities.prsdb.webapp.journeys.cancelLettingAgentDelegation.stepConfig.AreYouSureStep
import uk.gov.communities.prsdb.webapp.journeys.cancelLettingAgentDelegation.stepConfig.RemoveDelegationStep
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.shared.states.PropertyOwnershipJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.initialiseFromPropertyOwnershipId
import uk.gov.communities.prsdb.webapp.services.LettingAgentAccessService

@PrsdbWebService
class CancelLettingAgentDelegationJourneyFactory(
    private val stateFactory: ObjectFactory<CancelLettingAgentDelegationJourney>,
    private val lettingAgentAccessService: LettingAgentAccessService,
) {
    fun createJourneySteps(propertyOwnershipId: Long): Map<String, StepLifecycleOrchestrator> {
        val state = getInitializedState(propertyOwnershipId)

        val propertyRecordUrl = PropertyDetailsController.getPropertyDetailsPath(propertyOwnershipId)

        return journey(state) {
            unreachableStepStep { journey.areYouSureStep }
            configure {
                withAdditionalContentProperty { "title" to "cancelLettingAgentDelegation.title" }
            }
            step(journey.areYouSureStep) {
                routeSegment(AreYouSureStep.ROUTE_SEGMENT)
                initialStep()
                backUrl { propertyRecordUrl }
                nextDestination { mode ->
                    if (mode == AreYouSureMode.DOES_NOT_WANT_TO_PROCEED) {
                        Destination.ExternalUrl(propertyRecordUrl)
                    } else {
                        Destination(journey.removeDelegationStep)
                    }
                }
            }
            step(journey.removeDelegationStep) {
                parents { journey.areYouSureStep.hasOutcome(AreYouSureMode.WANTS_TO_PROCEED) }
                nextUrl {
                    "${
                        CancelLettingAgentDelegationController.getRemoveLettingAgentBasePath(propertyOwnershipId)
                    }/$CONFIRMATION_PATH_SEGMENT"
                }
            }
        }
    }

    private fun getInitializedState(propertyOwnershipId: Long): CancelLettingAgentDelegationJourney {
        val lettingAgentAccess =
            lettingAgentAccessService.getInvitationByPropertyOwnershipId(propertyOwnershipId)
                ?: throw ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No letting agent delegation found for property ownership $propertyOwnershipId",
                )

        val state = stateFactory.getObject()
        val stateWasAlreadyInitialized = state.isStateInitialized
        state.initialiseFromPropertyOwnershipId(propertyOwnershipId)

        if (!stateWasAlreadyInitialized) {
            state.lettingAgentEmail = lettingAgentAccess.invitedEmail
        }

        return state
    }

    fun initializeJourneyState(propertyOwnershipId: Long): String = stateFactory.getObject().initializeState(propertyOwnershipId)
}

@JourneyFrameworkComponent
class CancelLettingAgentDelegationJourney(
    override val areYouSureStep: AreYouSureStep,
    override val removeDelegationStep: RemoveDelegationStep,
    journeyStateService: JourneyStateService,
) : AbstractJourneyState(journeyStateService),
    CancelLettingAgentDelegationJourneyState {
    override var isStateInitialized: Boolean by delegateProvider.requiredDelegate("isStateInitialized", false)
    override var propertyOwnershipId: Long by delegateProvider.requiredImmutableDelegate("propertyOwnershipId")
    override var lettingAgentEmail: String by delegateProvider.requiredDelegate("lettingAgentEmail")

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
    val removeDelegationStep: RemoveDelegationStep
    var lettingAgentEmail: String
}
