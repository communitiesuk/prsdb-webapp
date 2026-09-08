package uk.gov.communities.prsdb.webapp.controllers

import jakarta.servlet.ServletException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HouseholdStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.householdsAndTenants.UpdateHouseholdsAndTenantsJourneyFactory
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOccupiedPropertyOwnership
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLettingAgentData
import java.util.UUID

@WebMvcTest(LettingAgentUpdateHouseholdsAndTenantsController::class)
class LettingAgentUpdateHouseholdsAndTenantsControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var journeyFactory: UpdateHouseholdsAndTenantsJourneyFactory

    @MockitoBean
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    private lateinit var stepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    @MockitoBean
    private lateinit var featureFlagManager: FeatureFlagManager

    private val token: UUID = UUID.randomUUID()

    private val updateStepRoute =
        LettingAgentUpdateHouseholdsAndTenantsController.getUpdateHouseholdsAndTenantsRoute(token) +
            "/${HouseholdStep.ROUTE_SEGMENT}"

    @BeforeEach
    fun enableFeatureFlag() {
        whenever(featureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)).thenReturn(true)
    }

    @BeforeEach
    fun allowPastLettingAgentAccessInterceptor() {
        whenever(lettingAgentAccessService.getTokenIsValid(any())).thenReturn(true)
        whenever(lettingAgentAccessService.isTokenAuthorisedInSession(any())).thenReturn(true)
    }

    @Test
    fun `getUpdateStep dispatches the journey step for a valid token`() {
        val propertyOwnership = createOccupiedPropertyOwnership()
        stubValidTokenJourney(propertyOwnership)
        whenever(stepLifecycleOrchestrator.getStepModelAndView())
            .thenReturn(ModelAndView("placeholder", mapOf("title" to "placeholder")))

        mvc.get(updateStepRoute).andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `postUpdateStep dispatches the journey step for a valid token`() {
        val propertyOwnership = createOccupiedPropertyOwnership()
        stubValidTokenJourney(propertyOwnership)
        val redirectUrl = LettingAgentPropertyDetailsController.getLettingAgentPropertyDetailsPath(token)
        whenever(stepLifecycleOrchestrator.postStepModelAndView(any()))
            .thenReturn(ModelAndView("redirect:$redirectUrl"))

        mvc
            .post(updateStepRoute) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                content = "numberOfHouseholds=2"
                with(csrf())
            }.andExpect {
                status { is3xxRedirection() }
                redirectedUrl(redirectUrl)
            }
    }

    @Test
    fun `getUpdateStep returns not found when the token is not recognised`() {
        whenever(lettingAgentAccessService.getInvitationByTokenOrNull(eq(token))).thenReturn(null)

        mvc.get(updateStepRoute).andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `getUpdateStep surfaces an error when the journey state propertyId does not match the token's property`() {
        val propertyOwnership = createOccupiedPropertyOwnership()
        whenever(lettingAgentAccessService.getInvitationByTokenOrNull(eq(token)))
            .thenReturn(MockLettingAgentData.createLettingAgentAccess(token = token, propertyOwnership = propertyOwnership))
        val returnUrl = LettingAgentPropertyDetailsController.getLettingAgentPropertyDetailsPath(token)
        doThrow(PrsdbWebException("Journey state propertyId does not match provided propertyId"))
            .whenever(journeyFactory)
            .createJourneySteps(eq(propertyOwnership.id), eq(returnUrl))

        assertThrows<ServletException> {
            mvc.get(updateStepRoute)
        }
    }

    private fun stubValidTokenJourney(propertyOwnership: PropertyOwnership) {
        whenever(lettingAgentAccessService.getInvitationByTokenOrNull(eq(token)))
            .thenReturn(MockLettingAgentData.createLettingAgentAccess(token = token, propertyOwnership = propertyOwnership))
        val returnUrl = LettingAgentPropertyDetailsController.getLettingAgentPropertyDetailsPath(token)
        whenever(journeyFactory.createJourneySteps(eq(propertyOwnership.id), eq(returnUrl)))
            .thenReturn(mapOf(HouseholdStep.ROUTE_SEGMENT to stepLifecycleOrchestrator))
    }
}
