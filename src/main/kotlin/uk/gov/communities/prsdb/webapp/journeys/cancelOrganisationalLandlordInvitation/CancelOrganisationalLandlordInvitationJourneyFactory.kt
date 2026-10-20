package uk.gov.communities.prsdb.webapp.journeys.cancelOrganisationalLandlordInvitation

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.TeamMembersController
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.cancelOrganisationalLandlordInvitation.stepConfig.AreYouSureMode
import uk.gov.communities.prsdb.webapp.journeys.cancelOrganisationalLandlordInvitation.stepConfig.AreYouSureStep
import uk.gov.communities.prsdb.webapp.journeys.cancelOrganisationalLandlordInvitation.stepConfig.CancelInvitationStep
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordInvitationService

@PrsdbWebService
class CancelOrganisationalLandlordInvitationJourneyFactory(
    private val stateFactory: ObjectFactory<CancelOrganisationalLandlordInvitationJourney>,
    private val organisationalLandlordInvitationService: OrganisationalLandlordInvitationService,
) {
    fun createJourneySteps(invitationId: Long): Map<String, StepLifecycleOrchestrator> {
        val invitation = organisationalLandlordInvitationService.getPendingInvitationForCurrentUsersOrganisation(invitationId)
        val state = stateFactory.getObject()

        if (!state.isStateInitialized) {
            state.invitedEmail = invitation.invitedEmail
            state.invitationId = invitation.id
            state.isStateInitialized = true
        }

        val invitationsTabUrl = TeamMembersController.TEAM_MEMBERS_INVITATIONS_TAB_PATH

        return journey(state) {
            unreachableStepStep { journey.areYouSureStep }
            configure {
                withAdditionalContentProperty { "title" to "cancelOrganisationalLandlordInvitation.title" }
            }
            step(journey.areYouSureStep) {
                routeSegment(AreYouSureStep.ROUTE_SEGMENT)
                initialStep()
                backUrl { invitationsTabUrl }
                nextDestination { mode ->
                    if (mode == AreYouSureMode.DOES_NOT_WANT_TO_PROCEED) {
                        Destination.ExternalUrl(invitationsTabUrl)
                    } else {
                        Destination(journey.cancelInvitationStep)
                    }
                }
            }
            step(journey.cancelInvitationStep) {
                parents { journey.areYouSureStep.hasOutcome(AreYouSureMode.WANTS_TO_PROCEED) }
                nextDestination {
                    Destination
                        .ExternalUrl(invitationsTabUrl)
                        .withFlashAttribute("successBanner", "teamMembers.successBanner.invitationCancelled.heading")
                        .withFlashAttribute("successBannerBody", "teamMembers.successBanner.invitationCancelled.body")
                }
            }
        }
    }

    fun initializeJourneyState(): String = stateFactory.getObject().initializeState()
}

@JourneyFrameworkComponent
class CancelOrganisationalLandlordInvitationJourney(
    override val areYouSureStep: AreYouSureStep,
    override val cancelInvitationStep: CancelInvitationStep,
    journeyStateService: JourneyStateService,
) : AbstractJourneyState(journeyStateService),
    CancelOrganisationalLandlordInvitationJourneyState {
    var isStateInitialized: Boolean by delegateProvider.requiredDelegate("isStateInitialized", false)
    override var invitedEmail: String by delegateProvider.requiredDelegate("invitedEmail")
    override var invitationId: Long by delegateProvider.requiredDelegate("invitationId")

    override fun generateJourneyId(seed: Any?): String =
        super<AbstractJourneyState>.generateJourneyId(
            "Cancel organisational landlord invitation journey at time ${System.currentTimeMillis()}",
        )
}

interface CancelOrganisationalLandlordInvitationJourneyState : JourneyState {
    val areYouSureStep: AreYouSureStep
    val cancelInvitationStep: CancelInvitationStep
    var invitedEmail: String
    var invitationId: Long
}
