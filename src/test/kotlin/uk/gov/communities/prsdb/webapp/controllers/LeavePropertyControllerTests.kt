package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LEAVE_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.controllers.LeavePropertyController.Companion.getLeavePropertyBasePath
import uk.gov.communities.prsdb.webapp.controllers.LeavePropertyController.Companion.getLeavePropertyPath
import uk.gov.communities.prsdb.webapp.exceptions.PropertyOwnershipMismatchException
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.leaveProperty.LeavePropertyJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.leaveProperty.stepConfig.ConfirmStep
import uk.gov.communities.prsdb.webapp.services.LeavePropertyService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import kotlin.test.assertEquals

@WebMvcTest(LeavePropertyController::class)
class LeavePropertyControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var leavePropertyJourneyFactory: LeavePropertyJourneyFactory

    @MockitoBean
    private lateinit var leavePropertyService: LeavePropertyService

    @MockitoBean
    private lateinit var mockStepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    private val testPropertyOwnershipId = 1L

    @Test
    fun `getJourneyStep returns a redirect for an unauthenticated user`() {
        mvc
            .get(getLeavePropertyPath(testPropertyOwnershipId))
            .andExpect {
                status { is3xxRedirection() }
            }
    }

    @Test
    @WithMockUser
    fun `getJourneyStep returns 403 for a user who is not a landlord`() {
        mvc
            .get(getLeavePropertyPath(testPropertyOwnershipId))
            .andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getJourneyStep returns 404 for a landlord who cannot leave this property`() {
        whenever(
            leavePropertyService.getPropertyOwnershipIfUserCanLeave(eq(testPropertyOwnershipId), any()),
        ).thenThrow(ResponseStatusException(HttpStatus.NOT_FOUND, "not eligible"))

        mvc
            .get(getLeavePropertyPath(testPropertyOwnershipId))
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getJourneyStep returns 200 for a landlord who can leave this property`() {
        whenever(
            leavePropertyService.getPropertyOwnershipIfUserCanLeave(eq(testPropertyOwnershipId), any()),
        ).thenReturn(MockLandlordData.createPropertyOwnership())
        whenever(
            leavePropertyJourneyFactory.createJourneySteps(testPropertyOwnershipId, "user"),
        ).thenReturn(mapOf(ConfirmStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))
        whenever(
            mockStepLifecycleOrchestrator.getStepModelAndView(),
        ).thenReturn(ModelAndView("placeholder", mapOf("title" to "placeholder")))

        mvc
            .get(getLeavePropertyPath(testPropertyOwnershipId))
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getJourneyStep returns 404 for an unknown step name`() {
        whenever(
            leavePropertyService.getPropertyOwnershipIfUserCanLeave(eq(testPropertyOwnershipId), any()),
        ).thenReturn(MockLandlordData.createPropertyOwnership())
        whenever(
            leavePropertyJourneyFactory.createJourneySteps(testPropertyOwnershipId, "user"),
        ).thenReturn(mapOf(ConfirmStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))

        mvc
            .get("${getLeavePropertyBasePath(testPropertyOwnershipId)}/unknown-step")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getJourneyStep redirects to initialize journey when no journey state exists`() {
        val journeyId = "test-journey-id"

        whenever(
            leavePropertyService.getPropertyOwnershipIfUserCanLeave(eq(testPropertyOwnershipId), any()),
        ).thenReturn(MockLandlordData.createPropertyOwnership())
        whenever(leavePropertyJourneyFactory.createJourneySteps(testPropertyOwnershipId, "user"))
            .thenThrow(NoSuchJourneyException())
        whenever(leavePropertyJourneyFactory.initializeJourneyState(any())).thenReturn(journeyId)

        mvc
            .get(getLeavePropertyPath(testPropertyOwnershipId))
            .andExpect {
                status { is3xxRedirection() }
                redirectedUrl(JourneyStateService.urlWithJourneyState(ConfirmStep.ROUTE_SEGMENT, journeyId))
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getJourneyStep redirects to initialize journey when property ownership does not match`() {
        val journeyId = "test-journey-id"

        whenever(
            leavePropertyService.getPropertyOwnershipIfUserCanLeave(eq(testPropertyOwnershipId), any()),
        ).thenReturn(MockLandlordData.createPropertyOwnership())
        whenever(leavePropertyJourneyFactory.createJourneySteps(testPropertyOwnershipId, "user"))
            .thenThrow(PropertyOwnershipMismatchException("mismatch"))
        whenever(leavePropertyJourneyFactory.initializeJourneyState(any())).thenReturn(journeyId)

        mvc
            .get(getLeavePropertyPath(testPropertyOwnershipId))
            .andExpect {
                status { is3xxRedirection() }
                redirectedUrl(JourneyStateService.urlWithJourneyState(ConfirmStep.ROUTE_SEGMENT, journeyId))
            }
    }

    @Test
    fun `postJourneyData returns a redirect for an unauthenticated user`() {
        mvc
            .post(getLeavePropertyPath(testPropertyOwnershipId)) {
                with(csrf())
            }.andExpect {
                status { is3xxRedirection() }
            }
    }

    @Test
    @WithMockUser
    fun `postJourneyData returns 403 for a user who is not a landlord`() {
        mvc
            .post(getLeavePropertyPath(testPropertyOwnershipId)) {
                with(csrf())
            }.andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `postJourneyData returns 404 for a landlord who cannot leave this property`() {
        whenever(
            leavePropertyService.getPropertyOwnershipIfUserCanLeave(eq(testPropertyOwnershipId), any()),
        ).thenThrow(ResponseStatusException(HttpStatus.NOT_FOUND, "not eligible"))

        mvc
            .post(getLeavePropertyPath(testPropertyOwnershipId)) {
                with(csrf())
            }.andExpect {
                status { isNotFound() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `postJourneyData returns the step's model and view for a landlord who can leave this property`() {
        whenever(
            leavePropertyService.getPropertyOwnershipIfUserCanLeave(eq(testPropertyOwnershipId), any()),
        ).thenReturn(MockLandlordData.createPropertyOwnership())
        whenever(
            leavePropertyJourneyFactory.createJourneySteps(testPropertyOwnershipId, "user"),
        ).thenReturn(mapOf(ConfirmStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))
        whenever(
            mockStepLifecycleOrchestrator.postStepModelAndView(any()),
        ).thenReturn(ModelAndView("placeholder", mapOf("title" to "placeholder")))

        mvc
            .post(getLeavePropertyPath(testPropertyOwnershipId)) {
                with(csrf())
            }.andExpect {
                status { isOk() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `postJourneyData returns 404 for an unknown step name`() {
        whenever(
            leavePropertyService.getPropertyOwnershipIfUserCanLeave(eq(testPropertyOwnershipId), any()),
        ).thenReturn(MockLandlordData.createPropertyOwnership())
        whenever(
            leavePropertyJourneyFactory.createJourneySteps(testPropertyOwnershipId, "user"),
        ).thenReturn(mapOf(ConfirmStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))

        mvc
            .post("${getLeavePropertyBasePath(testPropertyOwnershipId)}/unknown-step") {
                with(csrf())
            }.andExpect {
                status { isNotFound() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `postJourneyData redirects to initialize journey when no journey state exists`() {
        val journeyId = "test-journey-id"

        whenever(
            leavePropertyService.getPropertyOwnershipIfUserCanLeave(eq(testPropertyOwnershipId), any()),
        ).thenReturn(MockLandlordData.createPropertyOwnership())
        whenever(leavePropertyJourneyFactory.createJourneySteps(testPropertyOwnershipId, "user"))
            .thenThrow(NoSuchJourneyException())
        whenever(leavePropertyJourneyFactory.initializeJourneyState(any())).thenReturn(journeyId)

        mvc
            .post(getLeavePropertyPath(testPropertyOwnershipId)) {
                with(csrf())
            }.andExpect {
                status { is3xxRedirection() }
                redirectedUrl(JourneyStateService.urlWithJourneyState(ConfirmStep.ROUTE_SEGMENT, journeyId))
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `postJourneyData redirects to initialize journey when property ownership does not match`() {
        val journeyId = "test-journey-id"

        whenever(
            leavePropertyService.getPropertyOwnershipIfUserCanLeave(eq(testPropertyOwnershipId), any()),
        ).thenReturn(MockLandlordData.createPropertyOwnership())
        whenever(leavePropertyJourneyFactory.createJourneySteps(testPropertyOwnershipId, "user"))
            .thenThrow(PropertyOwnershipMismatchException("mismatch"))
        whenever(leavePropertyJourneyFactory.initializeJourneyState(any())).thenReturn(journeyId)

        mvc
            .post(getLeavePropertyPath(testPropertyOwnershipId)) {
                with(csrf())
            }.andExpect {
                status { is3xxRedirection() }
                redirectedUrl(JourneyStateService.urlWithJourneyState(ConfirmStep.ROUTE_SEGMENT, journeyId))
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `getConfirmation returns 200 if the property was left in the session`() {
        whenever(leavePropertyService.getLeftPropertyOwnershipsFromSession())
            .thenReturn(mutableMapOf(testPropertyOwnershipId to "1 Example Road, EG1 1AA"))

        mvc
            .get("${getLeavePropertyBasePath(testPropertyOwnershipId)}/$CONFIRMATION_PATH_SEGMENT")
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `getConfirmation returns 404 if no properties were left in the session`() {
        whenever(leavePropertyService.getLeftPropertyOwnershipsFromSession())
            .thenReturn(mutableMapOf())

        mvc
            .get("${getLeavePropertyBasePath(testPropertyOwnershipId)}/$CONFIRMATION_PATH_SEGMENT")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `getConfirmation returns 404 if this propertyOwnershipId was not left in the session`() {
        whenever(leavePropertyService.getLeftPropertyOwnershipsFromSession())
            .thenReturn(mutableMapOf((2L to ""), (3L to "")))

        mvc
            .get("${getLeavePropertyBasePath(testPropertyOwnershipId)}/$CONFIRMATION_PATH_SEGMENT")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `getLeavePropertyPath returns a path to the confirm step`() {
        assertEquals(
            "/$LANDLORD_PATH_SEGMENT/$LEAVE_PROPERTY_JOURNEY_URL/$testPropertyOwnershipId/${ConfirmStep.ROUTE_SEGMENT}",
            getLeavePropertyPath(testPropertyOwnershipId),
        )
    }
}
