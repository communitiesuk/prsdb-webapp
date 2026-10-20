package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.MULTI_USER_ORGANISATIONS
import uk.gov.communities.prsdb.webapp.controllers.CancelOrganisationalLandlordInvitationController.Companion.CANCEL_ORGANISATIONAL_LANDLORD_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.CancelOrganisationalLandlordInvitationController.Companion.getCancelInvitationPath
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.cancelOrganisationalLandlordInvitation.CancelOrganisationalLandlordInvitationJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.cancelOrganisationalLandlordInvitation.stepConfig.AreYouSureStep

@WebMvcTest(CancelOrganisationalLandlordInvitationController::class)
class CancelOrganisationalLandlordInvitationControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var journeyFactory: CancelOrganisationalLandlordInvitationJourneyFactory

    @MockitoBean
    private lateinit var mockStepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    // TODO PDJB-1828: Remove this mock when the MULTI_USER_ORGANISATIONS feature flag is removed
    @MockitoBean
    private lateinit var featureFlagManager: FeatureFlagManager

    private val testInvitationId = 1L
    private val placeholderModelAndView = ModelAndView("placeholder", mapOf("title" to "placeholder"))

    // TODO PDJB-1828: Remove this setup when the MULTI_USER_ORGANISATIONS feature flag is removed
    @BeforeEach
    fun enableFeatureFlag() {
        whenever(featureFlagManager.checkFeature(MULTI_USER_ORGANISATIONS)).thenReturn(true)
    }

    private fun mockJourneySteps() {
        whenever(journeyFactory.createJourneySteps(testInvitationId))
            .thenReturn(mapOf(AreYouSureStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))
    }

    @Test
    fun `getJourneyStep returns a redirect for an unauthenticated user`() {
        mvc.get(getCancelInvitationPath(testInvitationId)).andExpect {
            status { is3xxRedirection() }
        }
    }

    @Test
    @WithMockUser
    fun `getJourneyStep returns 403 for a user who is not an org admin`() {
        mvc.get(getCancelInvitationPath(testInvitationId)).andExpect {
            status { isForbidden() }
        }
    }

    @Test
    @WithMockUser(roles = ["ORG_ADMIN"], username = "user-123")
    fun `getJourneyStep returns 404 when the invitation cannot be cancelled by the current user`() {
        whenever(journeyFactory.createJourneySteps(testInvitationId)).thenThrow(ResponseStatusException(HttpStatus.NOT_FOUND))

        mvc.get(getCancelInvitationPath(testInvitationId)).andExpect {
            status { isNotFound() }
        }
    }

    @Test
    @WithMockUser(roles = ["ORG_ADMIN"], username = "user-123")
    fun `getJourneyStep returns 200 for an org admin`() {
        mockJourneySteps()
        whenever(mockStepLifecycleOrchestrator.getStepModelAndView()).thenReturn(placeholderModelAndView)

        mvc.get(getCancelInvitationPath(testInvitationId)).andExpect {
            status { isOk() }
        }
    }

    @Test
    @WithMockUser(roles = ["ORG_ADMIN"], username = "user-123")
    fun `getJourneyStep returns 404 for an unknown step name`() {
        mockJourneySteps()

        mvc.get("$CANCEL_ORGANISATIONAL_LANDLORD_INVITATION_ROUTE/$testInvitationId/unknown-step").andExpect {
            status { isNotFound() }
        }
    }

    @Test
    @WithMockUser(roles = ["ORG_ADMIN"], username = "user-123")
    fun `getJourneyStep redirects to initialise the journey when no journey state exists`() {
        val journeyId = "test-journey-id"
        whenever(journeyFactory.createJourneySteps(testInvitationId)).thenThrow(NoSuchJourneyException())
        whenever(journeyFactory.initializeJourneyState()).thenReturn(journeyId)

        mvc.get(getCancelInvitationPath(testInvitationId)).andExpect {
            status { is3xxRedirection() }
            redirectedUrl("${getCancelInvitationPath(testInvitationId)}?journeyId=$journeyId")
        }
    }

    @Test
    fun `postJourneyData returns a redirect for an unauthenticated user`() {
        mvc
            .post(getCancelInvitationPath(testInvitationId)) {
                with(csrf())
            }.andExpect {
                status { is3xxRedirection() }
            }
    }

    @Test
    @WithMockUser
    fun `postJourneyData returns 403 for a user who is not an org admin`() {
        mvc
            .post(getCancelInvitationPath(testInvitationId)) {
                with(csrf())
            }.andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["ORG_ADMIN"], username = "user-123")
    fun `postJourneyData returns 200 for an org admin`() {
        mockJourneySteps()
        whenever(mockStepLifecycleOrchestrator.postStepModelAndView(any())).thenReturn(placeholderModelAndView)

        mvc
            .post(getCancelInvitationPath(testInvitationId)) {
                with(csrf())
            }.andExpect {
                status { isOk() }
            }
    }
}
