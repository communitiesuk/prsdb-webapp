package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doThrow
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
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.DelegateToLettingAgentController.Companion.getDelegateToLettingAgentBasePath
import uk.gov.communities.prsdb.webapp.controllers.DelegateToLettingAgentController.Companion.getDelegateToLettingAgentPath
import uk.gov.communities.prsdb.webapp.exceptions.PropertyOwnershipMismatchException
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.delegateToLettingAgent.DelegateToLettingAgentJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.delegateToLettingAgent.stepConfig.AllowLettingAgentStep
import uk.gov.communities.prsdb.webapp.services.DelegateToLettingAgentService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import kotlin.test.assertEquals

@WebMvcTest(DelegateToLettingAgentController::class)
class DelegateToLettingAgentControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var delegateToLettingAgentJourneyFactory: DelegateToLettingAgentJourneyFactory

    @MockitoBean
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    private lateinit var delegateToLettingAgentService: DelegateToLettingAgentService

    @MockitoBean
    private lateinit var mockStepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    private val testPropertyOwnershipId = 1L

    private fun mockAuthorizedProperty() {
        doNothing()
            .whenever(propertyOwnershipService)
            .throwIfCurrentUserNotAuthorizedToEdit(eq(testPropertyOwnershipId))
    }

    private fun mockUnauthorizedProperty() {
        doThrow(ResponseStatusException(HttpStatus.NOT_FOUND, "not authorised"))
            .whenever(propertyOwnershipService)
            .throwIfCurrentUserNotAuthorizedToEdit(eq(testPropertyOwnershipId))
    }

    private fun mockJourneySteps() {
        whenever(
            delegateToLettingAgentJourneyFactory.createJourneySteps(testPropertyOwnershipId),
        ).thenReturn(mapOf(AllowLettingAgentStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))
    }

    private fun mockCompletedDelegation() {
        whenever(delegateToLettingAgentService.getDelegatedLettingAgentsFromSession())
            .thenReturn(mutableMapOf(testPropertyOwnershipId to "agent@example.com"))
        whenever(propertyOwnershipService.getPropertyOwnership(testPropertyOwnershipId))
            .thenReturn(MockLandlordData.createPropertyOwnership())
    }

    @Test
    fun `getJourneyStep returns a redirect for an unauthenticated user`() {
        mvc.get(getDelegateToLettingAgentPath(testPropertyOwnershipId)).andExpect {
            status { is3xxRedirection() }
        }
    }

    @Test
    @WithMockUser
    fun `getJourneyStep returns 403 for a user who is not a landlord`() {
        mvc.get(getDelegateToLettingAgentPath(testPropertyOwnershipId)).andExpect {
            status { isForbidden() }
        }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getJourneyStep returns 404 for a landlord not authorised for this property`() {
        mockUnauthorizedProperty()

        mvc.get(getDelegateToLettingAgentPath(testPropertyOwnershipId)).andExpect {
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

        mvc.get(getDelegateToLettingAgentPath(testPropertyOwnershipId)).andExpect {
            status { isOk() }
        }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getJourneyStep returns 404 for an unknown step name`() {
        mockAuthorizedProperty()
        mockJourneySteps()

        mvc.get("${getDelegateToLettingAgentBasePath(testPropertyOwnershipId)}/unknown-step").andExpect {
            status { isNotFound() }
        }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getJourneyStep redirects to initialize journey when no journey state exists`() {
        val journeyId = "test-journey-id"
        mockAuthorizedProperty()
        whenever(delegateToLettingAgentJourneyFactory.createJourneySteps(testPropertyOwnershipId))
            .thenThrow(NoSuchJourneyException())
        whenever(delegateToLettingAgentJourneyFactory.initializeJourneyState(any())).thenReturn(journeyId)

        mvc.get(getDelegateToLettingAgentPath(testPropertyOwnershipId)).andExpect {
            status { is3xxRedirection() }
            redirectedUrl("${getDelegateToLettingAgentPath(testPropertyOwnershipId)}?journeyId=$journeyId")
        }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getJourneyStep redirects to initialize journey when property ownership does not match`() {
        val journeyId = "test-journey-id"
        mockAuthorizedProperty()
        whenever(delegateToLettingAgentJourneyFactory.createJourneySteps(testPropertyOwnershipId))
            .thenThrow(PropertyOwnershipMismatchException("mismatch"))
        whenever(delegateToLettingAgentJourneyFactory.initializeJourneyState(any())).thenReturn(journeyId)

        mvc.get(getDelegateToLettingAgentPath(testPropertyOwnershipId)).andExpect {
            status { is3xxRedirection() }
            redirectedUrl("${getDelegateToLettingAgentPath(testPropertyOwnershipId)}?journeyId=$journeyId")
        }
    }

    @Test
    fun `postJourneyData returns a redirect for an unauthenticated user`() {
        mvc
            .post(getDelegateToLettingAgentPath(testPropertyOwnershipId)) {
                with(csrf())
            }.andExpect {
                status { is3xxRedirection() }
            }
    }

    @Test
    @WithMockUser
    fun `postJourneyData returns 403 for a user who is not a landlord`() {
        mvc
            .post(getDelegateToLettingAgentPath(testPropertyOwnershipId)) {
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
            .post(getDelegateToLettingAgentPath(testPropertyOwnershipId)) {
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
            .post(getDelegateToLettingAgentPath(testPropertyOwnershipId)) {
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
            .post("${getDelegateToLettingAgentBasePath(testPropertyOwnershipId)}/unknown-step") {
                with(csrf())
            }.andExpect {
                status { isNotFound() }
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `postJourneyData redirects to initialize journey when no journey state exists`() {
        val journeyId = "test-journey-id"
        mockAuthorizedProperty()
        whenever(delegateToLettingAgentJourneyFactory.createJourneySteps(testPropertyOwnershipId))
            .thenThrow(NoSuchJourneyException())
        whenever(delegateToLettingAgentJourneyFactory.initializeJourneyState(any())).thenReturn(journeyId)

        mvc
            .post(getDelegateToLettingAgentPath(testPropertyOwnershipId)) {
                with(csrf())
            }.andExpect {
                status { is3xxRedirection() }
                redirectedUrl("${getDelegateToLettingAgentPath(testPropertyOwnershipId)}?journeyId=$journeyId")
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `postJourneyData redirects to initialize journey when property ownership does not match`() {
        val journeyId = "test-journey-id"
        mockAuthorizedProperty()
        whenever(delegateToLettingAgentJourneyFactory.createJourneySteps(testPropertyOwnershipId))
            .thenThrow(PropertyOwnershipMismatchException("mismatch"))
        whenever(delegateToLettingAgentJourneyFactory.initializeJourneyState(any())).thenReturn(journeyId)

        mvc
            .post(getDelegateToLettingAgentPath(testPropertyOwnershipId)) {
                with(csrf())
            }.andExpect {
                status { is3xxRedirection() }
                redirectedUrl("${getDelegateToLettingAgentPath(testPropertyOwnershipId)}?journeyId=$journeyId")
            }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getConfirmation returns 200 for an authorised landlord who has just delegated to a letting agent`() {
        mockAuthorizedProperty()
        mockCompletedDelegation()

        mvc.get("${getDelegateToLettingAgentBasePath(testPropertyOwnershipId)}/$CONFIRMATION_PATH_SEGMENT").andExpect {
            status { isOk() }
        }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getConfirmation returns 404 when the property was not delegated in this session`() {
        mockAuthorizedProperty()
        whenever(delegateToLettingAgentService.getDelegatedLettingAgentsFromSession()).thenReturn(mutableMapOf())

        mvc.get("${getDelegateToLettingAgentBasePath(testPropertyOwnershipId)}/$CONFIRMATION_PATH_SEGMENT").andExpect {
            status { isNotFound() }
        }
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"], value = "user")
    fun `getConfirmation returns 404 for a landlord not authorised for this property`() {
        mockUnauthorizedProperty()

        mvc.get("${getDelegateToLettingAgentBasePath(testPropertyOwnershipId)}/$CONFIRMATION_PATH_SEGMENT").andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `getDelegateToLettingAgentPath returns a path to the allow letting agent step`() {
        assertEquals(
            "/$LANDLORD_PATH_SEGMENT/$DELEGATE_TO_LETTING_AGENT_JOURNEY_URL/$testPropertyOwnershipId/" +
                AllowLettingAgentStep.ROUTE_SEGMENT,
            getDelegateToLettingAgentPath(testPropertyOwnershipId),
        )
    }
}
