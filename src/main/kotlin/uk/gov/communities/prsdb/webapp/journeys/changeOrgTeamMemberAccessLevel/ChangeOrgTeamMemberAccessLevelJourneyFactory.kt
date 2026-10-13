package uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.controllers.TeamMembersController
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.exceptions.OrganisationalLandlordUserMismatchException
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel.stepConfig.AccessLevelStep
import uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel.stepConfig.CompleteChangeAccessLevelStep
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ChangeOrgTeamMemberAccessLevelFormModel

@PrsdbWebService
class ChangeOrgTeamMemberAccessLevelJourneyFactory(
    private val stateFactory: ObjectFactory<ChangeOrgTeamMemberAccessLevelJourney>,
) {
    fun createJourneySteps(otherOrganisationalLandlordUserId: Long): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        if (!state.isStateInitialized) {
            state.otherOrganisationalLandlordUserId = otherOrganisationalLandlordUserId
            state.isStateInitialized = true
        }

        if (state.otherOrganisationalLandlordUserId != otherOrganisationalLandlordUserId) {
            throw OrganisationalLandlordUserMismatchException(
                "Journey was initialized for organisational landlord user ${state.otherOrganisationalLandlordUserId} " +
                    "but request is for organisational landlord user $otherOrganisationalLandlordUserId",
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

    fun initializeJourneyState(otherOrganisationalLandlordUserId: Long): String =
        stateFactory.getObject().initializeState(otherOrganisationalLandlordUserId)
}

@JourneyFrameworkComponent
class ChangeOrgTeamMemberAccessLevelJourney(
    override val accessLevelStep: AccessLevelStep,
    override val completeChangeAccessLevelStep: CompleteChangeAccessLevelStep,
    journeyStateService: JourneyStateService,
) : AbstractJourneyState(journeyStateService),
    ChangeOrgTeamMemberAccessLevelJourneyState {
    override var isStateInitialized: Boolean by delegateProvider.requiredDelegate("isStateInitialized", false)
    override var otherOrganisationalLandlordUserId: Long by delegateProvider.requiredImmutableDelegate("otherOrganisationalLandlordUserId")

    override fun generateJourneyId(seed: Any?): String {
        val otherOrganisationalLandlordUserId = seed as? Long
        return super<AbstractJourneyState>.generateJourneyId(
            otherOrganisationalLandlordUserId?.let { generateSeedForMember(it) },
        )
    }

    companion object {
        private fun generateSeedForMember(otherOrganisationalLandlordUserId: Long): String =
            "Change access level journey for organisational landlord user $otherOrganisationalLandlordUserId " +
                "at time ${System.currentTimeMillis()}"
    }
}

interface ChangeOrgTeamMemberAccessLevelJourneyState : JourneyState {
    val accessLevelStep: AccessLevelStep
    val completeChangeAccessLevelStep: CompleteChangeAccessLevelStep
    var otherOrganisationalLandlordUserId: Long
    var isStateInitialized: Boolean

    val selectedRole: OrganisationalLandlordUserRole
        get() = accessLevelStep.formModel.notNullValue(ChangeOrgTeamMemberAccessLevelFormModel::role)
}
