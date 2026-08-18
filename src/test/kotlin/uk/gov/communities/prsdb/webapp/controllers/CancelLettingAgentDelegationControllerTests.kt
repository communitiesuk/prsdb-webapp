package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
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
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_DETAILS_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.REMOVE_LETTING_AGENT_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.CancelLettingAgentDelegationController.Companion.getRemoveLettingAgentBasePath
import uk.gov.communities.prsdb.webapp.controllers.CancelLettingAgentDelegationController.Companion.getRemoveLettingAgentPath
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.exceptions.PropertyOwnershipMismatchException
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.cancelLettingAgentDelegation.CancelLettingAgentDelegationJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.cancelLettingAgentDelegation.stepConfig.AreYouSureStep
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import kotlin.test.assertEquals

@WebMvcTest(CancelLettingAgentDelegationController::class)
class CancelLettingAgentDelegationControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var cancelLettingAgentDelegationJourneyFactory: CancelLettingAgentDelegationJourneyFactory

    @MockitoBean
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    private lateinit var mockStepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    private val testPropertyOwnershipId = 1L

    private fun mockAuthorizedProperty() {
        doReturn(mock<PropertyOwnership>())
            .whenever(propertyOwnershipService)
            .getPropertyOwnershipIfCurrentUserAuthorized(eq(testPropertyOwnershipId))
    }

    private fun mockUnauthorizedProperty() {
        doThrow(ResponseStatusException(HttpStatus.NOT_FOUND, "not authorised"))
            .whenever(propertyOwnershipService)
            .getPropertyOwnershipIfCurrentUserAuthorized(eq(testPropertyOwnershipId))
    }

    private fun mockJourneySteps() {
        whenever(
            cancelLettingAgentDelegationJourneyFactory.createJourneySteps(testPropertyOwnershipId),
        ).thenReturn(mapOf(AreYouSureStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))
    }

    @Test
    fun `getJourneyStep returns a redirect for an unauthenticated user`() {
        mvc.get(getRemoveLettingAgentPath(testPropertyOwnershipId)).andExpect {
            status { is3xxRedirection() }
        }
    }

    @Test
    @WithMockUser
    fun `getJourneyStep returns 403 for a user who is not a landlord`() {
        mvc.get(getRemoveLettingAgentPath(testPropertyOwnershipId)).andExpect {
            status { isForbidden() }
        }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getJourneyStep returns 404 for a landlord not authorised for this property`() {
        mockUnauthorizedProperty()

        mvc.get(getRemoveLettingAgentPath(testPropertyOwnershipId)).andExpect {
            status { isNotFound() }
        }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getJourneyStep returns 200 for an authorised landlord`() {
        mockAuthorizedProperty()
        mockJourneySteps()
        whenever(mockStepLifecycleOrchestrator.getStepModelAndView())
            .thenReturn(ModelAndView("placeholder", mapOf("title" to "placeholder")))

        mvc.get(getRemoveLettingAgentPath(testPropertyOwnershipId)).andExpect {
            status { isOk() }
        }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getJourneyStep returns 404 for an unknown step name`() {
        mockAuthorizedProperty()
        mockJourneySteps()

        mvc.get("${getRemoveLettingAgentBasePath(testPropertyOwnershipId)}/unknown-step").andExpect {
            status { isNotFound() }
        }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getJourneyStep redirects to initialize journey when no journey state exists`() {
        val journeyId = "test-journey-id"
        mockAuthorizedProperty()
        whenever(cancelLettingAgentDelegationJourneyFactory.createJourneySteps(testPropertyOwnershipId))
            .thenThrow(NoSuchJourneyException())
        whenever(cancelLettingAgentDelegationJourneyFactory.initializeJourneyState(any())).thenReturn(journeyId)

        mvc.get(getRemoveLettingAgentPath(testPropertyOwnershipId)).andExpect {
            status { is3xxRedirection() }
            redirectedUrl("${getRemoveLettingAgentPath(testPropertyOwnershipId)}?journeyId=$journeyId")
        }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getJourneyStep redirects to initialize journey when property ownership does not match`() {
        val journeyId = "test-journey-id"
        mockAuthorizedProperty()
        whenever(cancelLettingAgentDelegationJourneyFactory.createJourneySteps(testPropertyOwnershipId))
            .thenThrow(PropertyOwnershipMismatchException("mismatch"))
        whenever(cancelLettingAgentDelegationJourneyFactory.initializeJourneyState(any())).thenReturn(journeyId)

        mvc.get(getRemoveLettingAgentPath(testPropertyOwnershipId)).andExpect {
            status { is3xxRedirection() }
            redirectedUrl("${getRemoveLettingAgentPath(testPropertyOwnershipId)}?journeyId=$journeyId")
        }
    }

    @Test
    fun `postJourneyData returns a redirect for an unauthenticated user`() {
        mvc
            .post(getRemoveLettingAgentPath(testPropertyOwnershipId)) {
                with(csrf())
            }.andExpect {
                status { is3xxRedirection() }
            }
    }

    @Test
    @WithMockUser
    fun `postJourneyData returns 403 for a user who is not a landlord`() {
        mvc
            .post(getRemoveLettingAgentPath(testPropertyOwnershipId)) {
                with(csrf())
            }.andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `postJourneyData returns 404 for a landlord not authorised for this property`() {
        mockUnauthorizedProperty()

        mvc
            .post(getRemoveLettingAgentPath(testPropertyOwnershipId)) {
                with(csrf())
            }.andExpect {
                status { isNotFound() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `postJourneyData returns the step's model and view for an authorised landlord`() {
        mockAuthorizedProperty()
        mockJourneySteps()
        whenever(mockStepLifecycleOrchestrator.postStepModelAndView(any()))
            .thenReturn(ModelAndView("placeholder", mapOf("title" to "placeholder")))

        mvc
            .post(getRemoveLettingAgentPath(testPropertyOwnershipId)) {
                with(csrf())
            }.andExpect {
                status { isOk() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `postJourneyData returns 404 for an unknown step name`() {
        mockAuthorizedProperty()
        mockJourneySteps()

        mvc
            .post("${getRemoveLettingAgentBasePath(testPropertyOwnershipId)}/unknown-step") {
                with(csrf())
            }.andExpect {
                status { isNotFound() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getConfirmation returns 200 for an authorised landlord`() {
        mockAuthorizedProperty()

        mvc.get("${getRemoveLettingAgentBasePath(testPropertyOwnershipId)}/$CONFIRMATION_PATH_SEGMENT").andExpect {
            status { isOk() }
        }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getConfirmation returns 404 for a landlord not authorised for this property`() {
        mockUnauthorizedProperty()

        mvc.get("${getRemoveLettingAgentBasePath(testPropertyOwnershipId)}/$CONFIRMATION_PATH_SEGMENT").andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `getRemoveLettingAgentPath returns a path to the are you sure step`() {
        assertEquals(
            "/$LANDLORD_PATH_SEGMENT/$PROPERTY_DETAILS_SEGMENT/$testPropertyOwnershipId/$REMOVE_LETTING_AGENT_PATH_SEGMENT/" +
                AreYouSureStep.ROUTE_SEGMENT,
            getRemoveLettingAgentPath(testPropertyOwnershipId),
        )
    }
}
