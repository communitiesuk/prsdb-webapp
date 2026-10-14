package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
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
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.MULTI_USER_ORGANISATIONS
import uk.gov.communities.prsdb.webapp.controllers.ChangeOrgTeamMemberAccessLevelController.Companion.getChangeAccessLevelBasePath
import uk.gov.communities.prsdb.webapp.controllers.ChangeOrgTeamMemberAccessLevelController.Companion.getChangeAccessLevelPath
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlordUser
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.NoSuchJourneyException
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel.ChangeOrgTeamMemberAccessLevelJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.changeOrgTeamMemberAccessLevel.stepConfig.AccessLevelStep
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordUserService
import uk.gov.communities.prsdb.webapp.services.UserToOrgLandlordUserService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOrganisationalLandlordUser

@WebMvcTest(ChangeOrgTeamMemberAccessLevelController::class)
class ChangeOrgTeamMemberAccessLevelControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var journeyFactory: ChangeOrgTeamMemberAccessLevelJourneyFactory

    @MockitoBean
    private lateinit var organisationalLandlordUserService: OrganisationalLandlordUserService

    @MockitoBean
    private lateinit var userToOrgLandlordUserService: UserToOrgLandlordUserService

    @MockitoBean
    private lateinit var mockStepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    @MockitoBean
    private lateinit var featureFlagManager: FeatureFlagManager

    private val testUserId = 1L
    private val placeholderModelAndView = ModelAndView("placeholder", mapOf("title" to "placeholder"))

    @BeforeEach
    fun enableFeatureFlagAndAuthoriseMember() {
        whenever(featureFlagManager.checkFeature(MULTI_USER_ORGANISATIONS)).thenReturn(true)
        whenever(organisationalLandlordUserService.getCurrentUserIsAuthorizedToEditRecord(testUserId)).thenReturn(true)
        whenever(userToOrgLandlordUserService.getOrganisationalLandlordUserForCurrentUser())
            .thenReturn(createOrganisationalLandlordUser())
    }

    private fun mockJourneySteps() {
        whenever(journeyFactory.createJourneySteps(testUserId))
            .thenReturn(mapOf(AccessLevelStep.ROUTE_SEGMENT to mockStepLifecycleOrchestrator))
    }

    @Test
    fun `getJourneyStep returns a redirect for an unauthenticated user`() {
        mvc.get(getChangeAccessLevelPath(testUserId)).andExpect {
            status { is3xxRedirection() }
        }
    }

    @Test
    @WithMockUser
    fun `getJourneyStep returns 403 for a user who is not an org admin`() {
        mvc.get(getChangeAccessLevelPath(testUserId)).andExpect {
            status { isForbidden() }
        }
    }

    @Test
    @WithMockUser(roles = ["ORG_ADMIN"], username = "user-123")
    fun `getJourneyStep returns 404 when the feature flag is disabled`() {
        whenever(featureFlagManager.checkFeature(MULTI_USER_ORGANISATIONS)).thenReturn(false)

        mvc.get(getChangeAccessLevelPath(testUserId)).andExpect {
            status { isNotFound() }
        }
    }

    @Test
    @WithMockUser(roles = ["ORG_ADMIN"], username = "user-123")
    fun `getJourneyStep returns 404 when the current user is not authorized to edit the member`() {
        whenever(organisationalLandlordUserService.getCurrentUserIsAuthorizedToEditRecord(testUserId)).thenReturn(false)

        mvc.get(getChangeAccessLevelPath(testUserId)).andExpect {
            status { isNotFound() }
        }
    }

    @Test
    @WithMockUser(roles = ["ORG_ADMIN"], username = "user-123")
    fun `getJourneyStep returns 200 for an org admin`() {
        mockJourneySteps()
        whenever(mockStepLifecycleOrchestrator.getStepModelAndView()).thenReturn(placeholderModelAndView)

        mvc.get(getChangeAccessLevelPath(testUserId)).andExpect {
            status { isOk() }
        }
    }

    @Test
    @WithMockUser(roles = ["ORG_ADMIN"], username = "user-123")
    fun `getJourneyStep returns 404 for an unknown step name`() {
        mockJourneySteps()

        mvc.get("${getChangeAccessLevelBasePath(testUserId)}/unknown-step").andExpect {
            status { isNotFound() }
        }
    }

    @Test
    @WithMockUser(roles = ["ORG_ADMIN"], username = "user-123")
    fun `getJourneyStep returns 400 when an admin tries to change their own access level`() {
        val currentMember = mock<OrganisationalLandlordUser>()
        whenever(currentMember.id).thenReturn(testUserId)
        whenever(userToOrgLandlordUserService.getOrganisationalLandlordUserForCurrentUser()).thenReturn(currentMember)

        mvc.get(getChangeAccessLevelPath(testUserId)).andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    @WithMockUser(roles = ["ORG_ADMIN"], username = "user-123")
    fun `getJourneyStep redirects to initialize journey when no journey state exists`() {
        val journeyId = "test-journey-id"
        whenever(journeyFactory.createJourneySteps(testUserId)).thenThrow(NoSuchJourneyException())
        whenever(journeyFactory.initializeJourneyState(any())).thenReturn(journeyId)

        mvc.get(getChangeAccessLevelPath(testUserId)).andExpect {
            status { is3xxRedirection() }
            redirectedUrl("${getChangeAccessLevelPath(testUserId)}?journeyId=$journeyId")
        }
    }

    @Test
    @WithMockUser(roles = ["ORG_ADMIN"], username = "user-123")
    fun `getJourneyStep redirects to initialize journey when the member does not match`() {
        val journeyId = "test-journey-id"
        whenever(journeyFactory.createJourneySteps(testUserId))
            .thenThrow(PrsdbWebException("mismatch"))
        whenever(journeyFactory.initializeJourneyState(any())).thenReturn(journeyId)

        mvc.get(getChangeAccessLevelPath(testUserId)).andExpect {
            status { is3xxRedirection() }
            redirectedUrl("${getChangeAccessLevelPath(testUserId)}?journeyId=$journeyId")
        }
    }

    @Test
    fun `postJourneyData returns a redirect for an unauthenticated user`() {
        mvc
            .post(getChangeAccessLevelPath(testUserId)) {
                with(csrf())
            }.andExpect {
                status { is3xxRedirection() }
            }
    }

    @Test
    @WithMockUser
    fun `postJourneyData returns 403 for a user who is not an org admin`() {
        mvc
            .post(getChangeAccessLevelPath(testUserId)) {
                with(csrf())
            }.andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["ORG_ADMIN"], username = "user-123")
    fun `postJourneyData returns the step's model and view for an org admin`() {
        mockJourneySteps()
        whenever(mockStepLifecycleOrchestrator.postStepModelAndView(any())).thenReturn(placeholderModelAndView)

        mvc
            .post(getChangeAccessLevelPath(testUserId)) {
                with(csrf())
            }.andExpect {
                status { isOk() }
            }
    }

    @Test
    @WithMockUser(roles = ["ORG_ADMIN"], username = "user-123")
    fun `postJourneyData returns 404 for an unknown step name`() {
        mockJourneySteps()

        mvc
            .post("${getChangeAccessLevelBasePath(testUserId)}/unknown-step") {
                with(csrf())
            }.andExpect {
                status { isNotFound() }
            }
    }
}
