package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.config.featureFlags.FeatureFlagTestCallingEndpoints
import uk.gov.communities.prsdb.webapp.constants.MULTI_USER_ORGANISATIONS
import uk.gov.communities.prsdb.webapp.controllers.InviteTeamMemberController.Companion.INVITE_TEAM_MEMBER_START_PATH
import uk.gov.communities.prsdb.webapp.controllers.TeamMembersController.Companion.TEAM_MEMBERS_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.inviteTeamMember.InviteTeamMemberJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.inviteTeamMember.steps.InviteTeamMemberStep
import uk.gov.communities.prsdb.webapp.services.ManageTeamMembersUrlProvider
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOrgLandlord

// TODO PDJB-1828: Delete this class when the MULTI_USER_ORGANISATIONS flag is removed
class InviteTeamMemberControllerFeatureFlagTests : FeatureFlagTestCallingEndpoints() {
    @MockitoBean
    private lateinit var journeyFactory: InviteTeamMemberJourneyFactory

    @MockitoBean
    private lateinit var userToLandlordService: UserToLandlordService

    @MockitoBean
    private lateinit var manageTeamMembersUrlProvider: ManageTeamMembersUrlProvider

    @MockitoBean
    private lateinit var stepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    @WithMockUser(roles = ["ORG_ADMIN"])
    @Test
    fun `invite team member journey is unavailable if the multi-user organisations feature flag is disabled`() {
        featureFlagManager.disableFeature(MULTI_USER_ORGANISATIONS)

        mvc
            .get(INVITE_TEAM_MEMBER_START_PATH)
            .andExpect { status { isNotFound() } }
    }

    @WithMockUser(roles = ["ORG_ADMIN"])
    @Test
    fun `invite team member journey is available if the multi-user organisations feature flag is enabled`() {
        featureFlagManager.enableFeature(MULTI_USER_ORGANISATIONS)
        whenever(userToLandlordService.getCurrentLandlordForUser()).thenReturn(createOrgLandlord())
        whenever(journeyFactory.createJourneySteps())
            .thenReturn(mapOf(InviteTeamMemberStep.ROUTE_SEGMENT to stepLifecycleOrchestrator))
        whenever(stepLifecycleOrchestrator.getStepModelAndView()).thenReturn(ModelAndView("redirect:$TEAM_MEMBERS_ROUTE"))

        mvc
            .get(INVITE_TEAM_MEMBER_START_PATH)
            .andExpect { status { is3xxRedirection() } }
    }
}
