package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.ServletException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.CancelJointLandlordInvitationController.Companion.CANCEL_JOINT_LANDLORD_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.cancelJointLandlordInvitation.CancelJointLandlordInvitationJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.cancelJointLandlordInvitation.stepConfig.AreYouSureStep
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService

@WebMvcTest(CancelJointLandlordInvitationController::class)
class CancelJointLandlordInvitationControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var journeyFactory: CancelJointLandlordInvitationJourneyFactory

    @MockitoBean
    private lateinit var jointLandlordInvitationService: JointLandlordInvitationService

    @MockitoBean
    private lateinit var mockStepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    private val testInvitationId = 1L

    @Test
    fun `getJourneyStep returns a redirect for an unauthenticated user`() {
        mvc
            .get("$CANCEL_JOINT_LANDLORD_INVITATION_ROUTE/$testInvitationId/${AreYouSureStep.ROUTE_SEGMENT}")
            .andExpect {
                status { is3xxRedirection() }
            }
    }

    @Test
    @WithMockUser
    fun `getJourneyStep returns 403 for a user who is not a landlord`() {
        mvc
            .get("$CANCEL_JOINT_LANDLORD_INVITATION_ROUTE/$testInvitationId/${AreYouSureStep.ROUTE_SEGMENT}")
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getJourneyStep returns 200 for a landlord user`() {
        whenever(
            journeyFactory.createJourneySteps(testInvitationId, "user"),
        ).thenReturn(mapOf(AreYouSureStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))
        whenever(
            mockStepLifecycleOrchestrator.getStepModelAndView(),
        ).thenReturn(ModelAndView("placeholder", mapOf("title" to "placeholder")))

        mvc
            .get("$CANCEL_JOINT_LANDLORD_INVITATION_ROUTE/$testInvitationId/${AreYouSureStep.ROUTE_SEGMENT}")
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getJourneyStep returns 404 for an unknown step name`() {
        whenever(
            journeyFactory.createJourneySteps(testInvitationId, "user"),
        ).thenReturn(mapOf(AreYouSureStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))

        mvc
            .get("$CANCEL_JOINT_LANDLORD_INVITATION_ROUTE/$testInvitationId/unknown-step")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getJourneyStep redirects to initialize journey when no journey state exists`() {
        val journeyId = "test-journey-id"

        whenever(journeyFactory.createJourneySteps(testInvitationId, "user"))
            .thenThrow(NoSuchJourneyException())
        whenever(journeyFactory.initializeJourneyState()).thenReturn(journeyId)

        val expectedRedirectUrl =
            "$CANCEL_JOINT_LANDLORD_INVITATION_ROUTE/$testInvitationId/${JourneyStateService.urlWithJourneyState(
                AreYouSureStep.ROUTE_SEGMENT,
                journeyId,
            )}"

        mvc
            .get("$CANCEL_JOINT_LANDLORD_INVITATION_ROUTE/$testInvitationId/${AreYouSureStep.ROUTE_SEGMENT}")
            .andExpect {
                status { is3xxRedirection() }
                redirectedUrl(expectedRedirectUrl)
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `getConfirmation returns 200 when a cancellation exists in the session`() {
        whenever(jointLandlordInvitationService.getCancelledInvitationEmailFromSession())
            .thenReturn("test@example.com")

        mvc
            .get("$CANCEL_JOINT_LANDLORD_INVITATION_ROUTE/$CONFIRMATION_PATH_SEGMENT?propertyOwnershipId=1")
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `getConfirmation throws error when no cancellation exists in the session`() {
        whenever(jointLandlordInvitationService.getCancelledInvitationEmailFromSession())
            .thenReturn(null)

        assertThrows<ServletException> {
            mvc.get("$CANCEL_JOINT_LANDLORD_INVITATION_ROUTE/$CONFIRMATION_PATH_SEGMENT?propertyOwnershipId=1")
        }
    }
}
