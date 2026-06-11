package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.ServletException
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.constants.JOURNEY_ID
import uk.gov.communities.prsdb.webapp.constants.TOKEN
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrRejectJointLandlordInvitationController.Companion.ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrRejectJointLandlordInvitationController.Companion.JOINT_LANDLORD_INVITATION_ACCEPTED_CONFIRMATION_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.AcceptOrRejectJointLandlordInvitationController.Companion.JOINT_LANDLORD_INVITATION_REJECTED_CONFIRMATION_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.AcceptOrRejectJointLandlordInvitationJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps.CheckUserRoleStep
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

    @MockitoBean
    private lateinit var mockStepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    private val validToken = "test-token-123"
    private val journeyId = "test-journey-id"
    private val placeholderModelAndView = ModelAndView("placeholder", mapOf("title" to "placeholder"))

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

            verify(invitationService).addJourneyIdInvitationTokenPairToSession(journeyId, validToken)
        }
    }

    @Nested
    inner class GetJourneyStep {
        @Test
        fun `getJourneyStep is accessible without authentication (steps that are not CheckUserRole)`() {
            whenever(journeyFactory.createJourneySteps())
                .thenReturn(mapOf(ValidateTokenStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))
            whenever(mockStepLifecycleOrchestrator.getStepModelAndView()).thenReturn(placeholderModelAndView)

            mvc
                .get("$ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE/${ValidateTokenStep.ROUTE_SEGMENT}")
                .andExpect {
                    status { isOk() }
                }
        }

        @Test
        fun `getJourneyStep returns 404 when step is not found in journey map`() {
            whenever(journeyFactory.createJourneySteps()).thenReturn(emptyMap())

            mvc
                .get("$ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE/${ValidateTokenStep.ROUTE_SEGMENT}")
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun `getJourneyStep redirects to start route when NoSuchJourneyException is thrown`() {
            whenever(journeyFactory.createJourneySteps()).thenThrow(NoSuchJourneyException())

            mvc
                .get("$ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE/${ValidateTokenStep.ROUTE_SEGMENT}")
                .andExpect {
                    status { is3xxRedirection() }
                    redirectedUrl(ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE)
                }
        }
    }

    @Nested
    inner class CheckUserRoleStepStep {
        @WithMockUser
        @Test
        fun `getJourneyStep for CheckUserRole is accessible for an authenticated user`() {
            whenever(journeyFactory.createJourneySteps())
                .thenReturn(mapOf(CheckUserRoleStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))
            whenever(mockStepLifecycleOrchestrator.getStepModelAndView()).thenReturn(placeholderModelAndView)
            whenever(userRolesService.getHasLandlordUserRole("user")).thenReturn(true)

            mvc
                .get("$ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE/${CheckUserRoleStep.ROUTE_SEGMENT}?$JOURNEY_ID=$journeyId")
                .andExpect {
                    status { isOk() }
                }
        }

        @WithMockUser(roles = ["LANDLORD"])
        @Test
        fun `getJourneyStep for CheckUserRole is accessible for a landlord user`() {
            whenever(journeyFactory.createJourneySteps())
                .thenReturn(mapOf(CheckUserRoleStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))
            whenever(mockStepLifecycleOrchestrator.getStepModelAndView()).thenReturn(placeholderModelAndView)
            whenever(userRolesService.getHasLandlordUserRole("user")).thenReturn(true)

            mvc
                .get("$ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE/${CheckUserRoleStep.ROUTE_SEGMENT}?$JOURNEY_ID=$journeyId")
                .andExpect {
                    status { isOk() }
                }
        }

        @Test
        fun `getJourneyStep for CheckUserRole redirects an unauthenticated user`() {
            mvc
                .get("$ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE/${CheckUserRoleStep.ROUTE_SEGMENT}?$JOURNEY_ID=$journeyId")
                .andExpect {
                    status { is3xxRedirection() }
                }
        }
    }

    @Nested
    inner class PostJourneyData {
        @Test
        fun `postJourneyData is accessible without authentication (steps that are not CheckUserRole)`() {
            whenever(journeyFactory.createJourneySteps()).thenReturn(emptyMap())

            mvc
                .post("$ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE/${ValidateTokenStep.ROUTE_SEGMENT}") {
                    param("formData", "")
                    with(csrf())
                }.andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun `postJourneyData returns 404 when step is not found in journey map`() {
            whenever(journeyFactory.createJourneySteps()).thenReturn(emptyMap())

            mvc
                .post("$ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE/${ValidateTokenStep.ROUTE_SEGMENT}") {
                    param("formData", "")
                    with(csrf())
                }.andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun `postJourneyData redirects to start route when NoSuchJourneyException is thrown`() {
            whenever(journeyFactory.createJourneySteps()).thenThrow(NoSuchJourneyException())

            mvc
                .post("$ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE/${ValidateTokenStep.ROUTE_SEGMENT}") {
                    param("formData", "")
                    with(csrf())
                }.andExpect {
                    status { is3xxRedirection() }
                    redirectedUrl(ACCEPT_OR_REJECT_JOINT_LANDLORD_INVITATION_ROUTE)
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
        fun `getRejectionConfirmation is accessible without authentication and returns 200 with session data`() {
            whenever(invitationService.getRejectionConfirmationDataFromSession())
                .thenReturn("Flat 1, 11 Elm Drive, London, NW8 2DK")

            mvc
                .get(JOINT_LANDLORD_INVITATION_REJECTED_CONFIRMATION_ROUTE)
                .andExpect {
                    status { isOk() }
                    model {
                        attribute("propertyAddress", "Flat 1, 11 Elm Drive, London, NW8 2DK")
                    }
                    view { name("invitationRejectedConfirmation") }
                }
        }

        @Test
        fun `getRejectionConfirmation throws error when no rejection data in session`() {
            whenever(invitationService.getRejectionConfirmationDataFromSession()).thenReturn(null)

            assertThrows<ServletException> {
                mvc.get(JOINT_LANDLORD_INVITATION_REJECTED_CONFIRMATION_ROUTE)
            }
        }
    }
}
