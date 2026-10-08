package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
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
import uk.gov.communities.prsdb.webapp.controllers.InviteTeamMemberController.Companion.INVITE_TEAM_MEMBER_PATH
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.inviteTeamMember.InviteTeamMemberJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.inviteTeamMember.steps.InviteTeamMemberStep
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createIndividualLandlord
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOrgLandlord

@WebMvcTest(InviteTeamMemberController::class)
class InviteTeamMemberControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    private lateinit var journeyFactory: InviteTeamMemberJourneyFactory

    @MockitoBean
    private lateinit var userToLandlordService: UserToLandlordService

    @MockitoBean
    private lateinit var stepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    private val placeholderModelAndView = ModelAndView("placeholder", mapOf("title" to "placeholder"))

    private fun stubOrganisationLandlordJourney() {
        whenever(userToLandlordService.getCurrentLandlordForUser()).thenReturn(createOrgLandlord())
        whenever(journeyFactory.createJourneySteps())
            .thenReturn(mapOf(InviteTeamMemberStep.ROUTE_SEGMENT to stepLifecycleOrchestrator))
    }

    @Test
    fun `getJourneyStep returns a redirect for an unauthenticated user`() {
        mvc.get(INVITE_TEAM_MEMBER_PATH).andExpect {
            status { is3xxRedirection() }
        }
    }

    @Test
    @WithMockUser
    fun `getJourneyStep returns 403 for a user without a landlord role`() {
        mvc.get(INVITE_TEAM_MEMBER_PATH).andExpect {
            status { isForbidden() }
        }
    }

    @Test
    @WithMockUser(roles = ["ORG_EDITOR"])
    fun `getJourneyStep returns 403 for an organisation editor`() {
        mvc.get(INVITE_TEAM_MEMBER_PATH).andExpect {
            status { isForbidden() }
        }
    }

    @Test
    @WithMockUser(roles = ["ORG_ADMIN"])
    fun `getJourneyStep returns 403 for a non-organisation landlord`() {
        whenever(userToLandlordService.getCurrentLandlordForUser()).thenReturn(createIndividualLandlord())

        mvc.get(INVITE_TEAM_MEMBER_PATH).andExpect {
            status { isForbidden() }
        }
    }

    @Test
    @WithMockUser(roles = ["ORG_ADMIN"])
    fun `getJourneyStep returns 200 for an organisation admin`() {
        stubOrganisationLandlordJourney()
        whenever(stepLifecycleOrchestrator.getStepModelAndView()).thenReturn(placeholderModelAndView)

        mvc.get(INVITE_TEAM_MEMBER_PATH).andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `postJourneyData returns a redirect for an unauthenticated user`() {
        mvc
            .post(INVITE_TEAM_MEMBER_PATH) {
                param("formData", "")
                with(csrf())
            }.andExpect {
                status { is3xxRedirection() }
            }
    }

    @Test
    @WithMockUser
    fun `postJourneyData returns 403 for a user without a landlord role`() {
        mvc
            .post(INVITE_TEAM_MEMBER_PATH) {
                param("formData", "")
                with(csrf())
            }.andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["ORG_EDITOR"])
    fun `postJourneyData returns 403 for an organisation editor`() {
        mvc
            .post(INVITE_TEAM_MEMBER_PATH) {
                param("formData", "")
                with(csrf())
            }.andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["ORG_ADMIN"])
    fun `postJourneyData returns 403 for a non-organisation landlord`() {
        whenever(userToLandlordService.getCurrentLandlordForUser()).thenReturn(createIndividualLandlord())

        mvc
            .post(INVITE_TEAM_MEMBER_PATH) {
                param("formData", "")
                with(csrf())
            }.andExpect {
                status { isForbidden() }
            }
    }

    @Test
    @WithMockUser(roles = ["ORG_ADMIN"])
    fun `postJourneyData returns 200 for an organisation admin`() {
        stubOrganisationLandlordJourney()
        whenever(stepLifecycleOrchestrator.postStepModelAndView(any())).thenReturn(placeholderModelAndView)

        mvc
            .post(INVITE_TEAM_MEMBER_PATH) {
                param("formData", "")
                with(csrf())
            }.andExpect {
                status { isOk() }
            }
    }
}
