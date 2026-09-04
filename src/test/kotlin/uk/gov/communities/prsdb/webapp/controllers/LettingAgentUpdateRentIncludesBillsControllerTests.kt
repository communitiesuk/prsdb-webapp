package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentIncludesBillsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.rentIncludesBills.UpdateRentIncludesBillsJourneyFactory
import uk.gov.communities.prsdb.webapp.services.LettingAgentAccessService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOccupiedPropertyOwnership
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLettingAgentData
import java.util.UUID

@WebMvcTest(LettingAgentUpdateRentIncludesBillsController::class)
class LettingAgentUpdateRentIncludesBillsControllerTests(
    @Autowired webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var journeyFactory: UpdateRentIncludesBillsJourneyFactory

    @MockitoBean
    private lateinit var lettingAgentAccessService: LettingAgentAccessService

    @MockitoBean
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    private lateinit var stepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    @MockitoBean
    private lateinit var featureFlagManager: FeatureFlagManager

    private val token: UUID = UUID.randomUUID()

    private val updateStepRoute =
        LettingAgentUpdateRentIncludesBillsController.getUpdateRentIncludesBillsRoute(token) +
            "/${RentIncludesBillsStep.ROUTE_SEGMENT}"

    private val formContent = "rentIncludesBills=true"

    @BeforeEach
    fun enableFeatureFlag() {
        whenever(featureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)).thenReturn(true)
    }

    private fun stubValidTokenForOccupiedProperty() {
        val propertyOwnership = createOccupiedPropertyOwnership()
        whenever(lettingAgentAccessService.getInvitationByTokenOrNull(eq(token)))
            .thenReturn(MockLettingAgentData.createLettingAgentAccess(token = token, propertyOwnership = propertyOwnership))
        whenever(journeyFactory.createJourneySteps(eq(propertyOwnership.id), any()))
            .thenReturn(mapOf(RentIncludesBillsStep.ROUTE_SEGMENT to stepLifecycleOrchestrator))
    }

    @Test
    fun `getUpdateStep returns 200 when the token maps to a property the user can edit`() {
        stubValidTokenForOccupiedProperty()
        whenever(stepLifecycleOrchestrator.getStepModelAndView())
            .thenReturn(ModelAndView("placeholder", mapOf("title" to "placeholder")))

        mvc.get(updateStepRoute).andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `getUpdateStep seeds the journey with the token and returns to the letting agent property details page`() {
        val propertyOwnership = createOccupiedPropertyOwnership()
        whenever(lettingAgentAccessService.getInvitationByTokenOrNull(eq(token)))
            .thenReturn(MockLettingAgentData.createLettingAgentAccess(token = token, propertyOwnership = propertyOwnership))
        val expectedReturnUrl = LettingAgentPropertyDetailsController.getLettingAgentPropertyDetailsPath(token)
        whenever(journeyFactory.createJourneySteps(eq(propertyOwnership.id), eq(expectedReturnUrl)))
            .thenReturn(mapOf(RentIncludesBillsStep.ROUTE_SEGMENT to stepLifecycleOrchestrator))
        whenever(stepLifecycleOrchestrator.getStepModelAndView())
            .thenReturn(ModelAndView("placeholder", mapOf("title" to "placeholder")))

        mvc.get(updateStepRoute).andExpect {
            status { isOk() }
        }

        verify(journeyFactory).initializeJourneyState(eq(token))
        verify(journeyFactory).createJourneySteps(eq(propertyOwnership.id), eq(expectedReturnUrl))
    }

    @Test
    fun `getUpdateStep returns 404 when the token is not recognised`() {
        whenever(lettingAgentAccessService.getInvitationByTokenOrNull(eq(token))).thenReturn(null)

        mvc.get(updateStepRoute).andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `getUpdateStep returns 404 when the current user is not authorised to edit the property`() {
        val propertyOwnership = createOccupiedPropertyOwnership()
        whenever(lettingAgentAccessService.getInvitationByTokenOrNull(eq(token)))
            .thenReturn(MockLettingAgentData.createLettingAgentAccess(token = token, propertyOwnership = propertyOwnership))
        whenever(propertyOwnershipService.throwIfCurrentUserNotAuthorizedToEdit(eq(propertyOwnership.id)))
            .thenThrow(ResponseStatusException(HttpStatus.NOT_FOUND))

        mvc.get(updateStepRoute).andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `getUpdateStep returns 404 when the feature flag is disabled`() {
        whenever(featureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)).thenReturn(false)

        mvc.get(updateStepRoute).andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `postUpdateStep redirects for a valid token`() {
        stubValidTokenForOccupiedProperty()
        val redirectUrl = LettingAgentPropertyDetailsController.getLettingAgentPropertyDetailsPath(token)
        whenever(stepLifecycleOrchestrator.postStepModelAndView(any()))
            .thenReturn(ModelAndView("redirect:$redirectUrl"))

        mvc
            .post(updateStepRoute) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                content = formContent
                with(csrf())
            }.andExpect {
                status { is3xxRedirection() }
                redirectedUrl(redirectUrl)
            }
    }

    @Test
    fun `postUpdateStep seeds the journey with the token and returns to the letting agent property details page`() {
        val propertyOwnership = createOccupiedPropertyOwnership()
        whenever(lettingAgentAccessService.getInvitationByTokenOrNull(eq(token)))
            .thenReturn(MockLettingAgentData.createLettingAgentAccess(token = token, propertyOwnership = propertyOwnership))
        val expectedReturnUrl = LettingAgentPropertyDetailsController.getLettingAgentPropertyDetailsPath(token)
        whenever(journeyFactory.createJourneySteps(eq(propertyOwnership.id), eq(expectedReturnUrl)))
            .thenReturn(mapOf(RentIncludesBillsStep.ROUTE_SEGMENT to stepLifecycleOrchestrator))
        whenever(stepLifecycleOrchestrator.postStepModelAndView(any()))
            .thenReturn(ModelAndView("redirect:$expectedReturnUrl"))

        mvc
            .post(updateStepRoute) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                content = formContent
                with(csrf())
            }.andExpect {
                status { is3xxRedirection() }
            }

        verify(journeyFactory).initializeJourneyState(eq(token))
        verify(journeyFactory).createJourneySteps(eq(propertyOwnership.id), eq(expectedReturnUrl))
    }

    @Test
    fun `postUpdateStep returns 404 when the token is not recognised`() {
        whenever(lettingAgentAccessService.getInvitationByTokenOrNull(eq(token))).thenReturn(null)

        mvc
            .post(updateStepRoute) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                content = formContent
                with(csrf())
            }.andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `postUpdateStep returns 404 when the current user is not authorised to edit the property`() {
        val propertyOwnership = createOccupiedPropertyOwnership()
        whenever(lettingAgentAccessService.getInvitationByTokenOrNull(eq(token)))
            .thenReturn(MockLettingAgentData.createLettingAgentAccess(token = token, propertyOwnership = propertyOwnership))
        whenever(propertyOwnershipService.throwIfCurrentUserNotAuthorizedToEdit(eq(propertyOwnership.id)))
            .thenThrow(ResponseStatusException(HttpStatus.NOT_FOUND))

        mvc
            .post(updateStepRoute) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                content = formContent
                with(csrf())
            }.andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `postUpdateStep returns 404 when the feature flag is disabled`() {
        whenever(featureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)).thenReturn(false)

        mvc
            .post(updateStepRoute) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                content = formContent
                with(csrf())
            }.andExpect {
                status { isNotFound() }
            }
    }
}
