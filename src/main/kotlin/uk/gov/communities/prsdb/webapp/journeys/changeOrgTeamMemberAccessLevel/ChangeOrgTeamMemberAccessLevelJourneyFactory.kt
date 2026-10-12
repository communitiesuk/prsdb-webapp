package uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.TeamMembersController
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel.stepConfig.AccessLevelStep
import uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel.stepConfig.CompleteChangeAccessLevelStep
import uk.gov.communities.prsdb.webapp.journeys.isComplete

@PrsdbWebService
class ChangeOrgTeamMemberAccessLevelJourneyFactory(
    private val stateFactory: ObjectFactory<ChangeOrgTeamMemberAccessLevelJourney>,
) {
    fun createJourneySteps(organisationalLandlordUserId: Long): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        if (!state.isStateInitialized) {
            state.organisationalLandlordUserId = organisationalLandlordUserId
            state.isStateInitialized = true
        }

        if (state.organisationalLandlordUserId != organisationalLandlordUserId) {
            throw PrsdbWebException(
                "Journey was initialized for organisational landlord user ${state.organisationalLandlordUserId} " +
                    "but request is for organisational landlord user $organisationalLandlordUserId",
            )
        }

        return journey(state) {
            unreachableStepStep { journey.accessLevelStep }
            step(journey.accessLevelStep) {
                routeSegment(AccessLevelStep.ROUTE_SEGMENT)
                initialStep()
                backUrl { TeamMembersController.TEAM_MEMBERS_ROUTE }
                nextStep { journey.completeChangeAccessLevelStep }
            }
            // TODO PDJB-1764: Add a split here for the remove journey
            step(journey.completeChangeAccessLevelStep) {
                parents { journey.accessLevelStep.isComplete() }
                nextUrl { TeamMembersController.TEAM_MEMBERS_ROUTE }
            }
        }
    }

    fun initializeJourneyState(organisationalLandlordUserId: Long): String =
        stateFactory.getObject().initializeState(organisationalLandlordUserId)
}

@JourneyFrameworkComponent
class ChangeOrgTeamMemberAccessLevelJourney(
    override val accessLevelStep: AccessLevelStep,
    override val completeChangeAccessLevelStep: CompleteChangeAccessLevelStep,
    journeyStateService: JourneyStateService,
) : AbstractJourneyState(journeyStateService),
    ChangeOrgTeamMemberAccessLevelJourneyState {
    override var isStateInitialized: Boolean by delegateProvider.requiredDelegate("isStateInitialized", false)
    override var organisationalLandlordUserId: Long by delegateProvider.requiredImmutableDelegate("organisationalLandlordUserId")

    override fun generateJourneyId(seed: Any?): String {
        val organisationalLandlordUserId = seed as? Long
        return super<AbstractJourneyState>.generateJourneyId(
            organisationalLandlordUserId?.let { generateSeedForMember(it) },
        )
    }

    companion object {
        private fun generateSeedForMember(organisationalLandlordUserId: Long): String =
            "Change access level journey for organisational landlord user $organisationalLandlordUserId " +
                "at time ${System.currentTimeMillis()}"
    }
}

interface ChangeOrgTeamMemberAccessLevelJourneyState : JourneyState {
    val accessLevelStep: AccessLevelStep
    val completeChangeAccessLevelStep: CompleteChangeAccessLevelStep
    var organisationalLandlordUserId: Long
    var isStateInitialized: Boolean
}
