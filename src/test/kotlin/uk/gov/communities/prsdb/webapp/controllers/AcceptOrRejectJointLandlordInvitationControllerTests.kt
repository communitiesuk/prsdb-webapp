package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.ServletException
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.constants.TOKEN
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrRejectJointLandlordInvitationController.Companion.ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrRejectJointLandlordInvitationController.Companion.JOINT_LANDLORD_INVITATION_ACCEPTED_CONFIRMATION_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrRejectJointLandlordInvitationController.Companion.JOINT_LANDLORD_INVITATION_REJECTED_CONFIRMATION_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.AcceptOrRejectJointLandlordInvitationJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps.AcceptOrRejectStep
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps.InviteUnavailableStep
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps.ValidateTokenStep
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService

@WebMvcTest(AcceptOrRejectJointLandlordInvitationController::class)
class AcceptOrRejectJointLandlordInvitationControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var journeyFactory: AcceptOrRejectJointLandlordInvitationJourneyFactory

    @MockitoBean
    private lateinit var invitationService: JointLandlordInvitationService

    private val validToken = "test-token-123"
    private val journeyId = "test-journey-id"

    @Nested
    inner class StartJourney {
        @Test
        fun `startJourney is accessible without authentication`() {
            whenever(journeyFactory.initializeJourneyState(validToken)).thenReturn(journeyId)

            mvc
                .get("$ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE?$TOKEN=$validToken")
                .andExpect {
                    status { is3xxRedirection() }
                }
        }

        @Test
        fun `startJourney initializes journey state and redirects to validate-token step`() {
            whenever(journeyFactory.initializeJourneyState(validToken)).thenReturn(journeyId)

            val expectedRedirectUrl =
                JourneyStateService
                    .urlWithJourneyState("$ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE/${ValidateTokenStep.ROUTE_SEGMENT}", journeyId)

            mvc
                .get("$ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE?$TOKEN=$validToken")
                .andExpect {
                    status { is3xxRedirection() }
                    redirectedUrl(expectedRedirectUrl)
                }
        }

        @Test
        fun `startJourney stores token in session`() {
            whenever(journeyFactory.initializeJourneyState(validToken)).thenReturn(journeyId)

            mvc
                .get("$ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE?$TOKEN=$validToken")

            verify(invitationService).storeTokenInSession(validToken)
        }
    }

    @Nested
    inner class GetJourneyStep {
        @Test
        fun `getJourneyStep is accessible without authentication for validate-token step`() {
            whenever(invitationService.getTokenFromSession()).thenReturn(validToken)
            whenever(journeyFactory.createJourneySteps(validToken)).thenThrow(NoSuchJourneyException())
            whenever(journeyFactory.initializeJourneyState(validToken)).thenReturn(journeyId)

            mvc
                .get("$ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE/${ValidateTokenStep.ROUTE_SEGMENT}")
                .andExpect {
                    status { is3xxRedirection() }
                }
        }

        @Test
        fun `getJourneyStep is accessible without authentication for invite-unavailable step`() {
            whenever(invitationService.getTokenFromSession()).thenReturn(validToken)
            whenever(journeyFactory.createJourneySteps(validToken)).thenThrow(NoSuchJourneyException())
            whenever(journeyFactory.initializeJourneyState(validToken)).thenReturn(journeyId)

            mvc
                .get("$ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE/${InviteUnavailableStep.ROUTE_SEGMENT}")
                .andExpect {
                    status { is3xxRedirection() }
                }
        }

        @Test
        fun `getJourneyStep is accessible without authentication for accept-or-reject step`() {
            whenever(invitationService.getTokenFromSession()).thenReturn(validToken)
            whenever(journeyFactory.createJourneySteps(validToken)).thenThrow(NoSuchJourneyException())
            whenever(journeyFactory.initializeJourneyState(validToken)).thenReturn(journeyId)

            mvc
                .get("$ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE/${AcceptOrRejectStep.ROUTE_SEGMENT}")
                .andExpect {
                    status { is3xxRedirection() }
                }
        }

        @Test
        fun `getJourneyStep returns a redirect for an unauthenticated user on a non-permitAll step`() {
            mvc
                .get("$ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE/some-other-step")
                .andExpect {
                    status { is3xxRedirection() }
                }
        }

        @Test
        fun `getJourneyStep throws exception when token is not in session`() {
            whenever(invitationService.getTokenFromSession()).thenReturn(null)

            assertThrows<ServletException> {
                mvc
                    .get("$ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE/${ValidateTokenStep.ROUTE_SEGMENT}")
            }
        }

        @Test
        fun `getJourneyStep redirects to initialize journey when no journey state exists`() {
            whenever(invitationService.getTokenFromSession()).thenReturn(validToken)
            whenever(journeyFactory.createJourneySteps(validToken)).thenThrow(NoSuchJourneyException())
            whenever(journeyFactory.initializeJourneyState(validToken)).thenReturn(journeyId)

            mvc
                .get("$ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE/${ValidateTokenStep.ROUTE_SEGMENT}")
                .andExpect {
                    status { is3xxRedirection() }
                    redirectedUrl(JourneyStateService.urlWithJourneyState(ValidateTokenStep.ROUTE_SEGMENT, journeyId))
                }
        }
    }

    @Nested
    inner class PostJourneyData {
        @Test
        fun `postJourneyData is accessible without authentication for validate-token step`() {
            whenever(invitationService.getTokenFromSession()).thenReturn(validToken)
            whenever(journeyFactory.createJourneySteps(validToken)).thenThrow(NoSuchJourneyException())
            whenever(journeyFactory.initializeJourneyState(validToken)).thenReturn(journeyId)

            mvc
                .post("$ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE/${ValidateTokenStep.ROUTE_SEGMENT}") {
                    param("formData", "")
                    with(csrf())
                }.andExpect {
                    status { is3xxRedirection() }
                }
        }

        @Test
        fun `postJourneyData is accessible without authentication for accept-or-reject step`() {
            whenever(invitationService.getTokenFromSession()).thenReturn(validToken)
            whenever(journeyFactory.createJourneySteps(validToken)).thenThrow(NoSuchJourneyException())
            whenever(journeyFactory.initializeJourneyState(validToken)).thenReturn(journeyId)

            mvc
                .post("$ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE/${AcceptOrRejectStep.ROUTE_SEGMENT}") {
                    param("formData", "")
                    with(csrf())
                }.andExpect {
                    status { is3xxRedirection() }
                }
        }

        @Test
        fun `postJourneyData returns a redirect for an unauthenticated user on a non-permitAll step`() {
            mvc
                .post("$ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE/some-other-step") {
                    param("formData", "")
                    with(csrf())
                }.andExpect {
                    status { is3xxRedirection() }
                }
        }

        @Test
        fun `postJourneyData throws exception when token is not in session`() {
            whenever(invitationService.getTokenFromSession()).thenReturn(null)

            assertThrows<ServletException> {
                mvc
                    .post("$ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE/${ValidateTokenStep.ROUTE_SEGMENT}") {
                        param("formData", "")
                        with(csrf())
                    }
            }
        }

        @Test
        fun `postJourneyData redirects to initialize journey when no journey state exists`() {
            whenever(invitationService.getTokenFromSession()).thenReturn(validToken)
            whenever(journeyFactory.createJourneySteps(validToken)).thenThrow(NoSuchJourneyException())
            whenever(journeyFactory.initializeJourneyState(validToken)).thenReturn(journeyId)

            mvc
                .post("$ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE/${ValidateTokenStep.ROUTE_SEGMENT}") {
                    param("formData", "")
                    with(csrf())
                }.andExpect {
                    status { is3xxRedirection() }
                    redirectedUrl(JourneyStateService.urlWithJourneyState(ValidateTokenStep.ROUTE_SEGMENT, journeyId))
                }
        }
    }

    @Nested
    inner class GetConfirmation {
        @Test
        fun `getConfirmation returns a redirect for an unauthenticated user`() {
            mvc
                .get(JOINT_LANDLORD_INVITATION_ACCEPTED_CONFIRMATION_ROUTE)
                .andExpect {
                    status { is3xxRedirection() }
                }
        }

        @WithMockUser
        @Test
        fun `getConfirmation returns 403 for user without LANDLORD role`() {
            mvc
                .get(JOINT_LANDLORD_INVITATION_ACCEPTED_CONFIRMATION_ROUTE)
                .andExpect {
                    status { isForbidden() }
                }

            verify(invitationService, never()).clearTokenFromSession()
        }

        @WithMockUser(roles = ["LANDLORD"])
        @Test
        fun `getConfirmation returns 200 for a landlord user`() {
            mvc
                .get(JOINT_LANDLORD_INVITATION_ACCEPTED_CONFIRMATION_ROUTE)
                .andExpect {
                    status { isOk() }
                }
        }
    }

    @Nested
    inner class GetRejectionConfirmation {
        @Test
        fun `getRejectionConfirmation is accessible without authentication`() {
            mvc
                .get(JOINT_LANDLORD_INVITATION_REJECTED_CONFIRMATION_ROUTE)
                .andExpect {
                    status { isOk() }
                }
        }
    }
}
