package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.config.featureFlags.FeatureFlagTestCallingEndpoints
import uk.gov.communities.prsdb.webapp.constants.MULTI_USER_ORGANISATIONS
import uk.gov.communities.prsdb.webapp.controllers.CancelOrganisationalLandlordInvitationController.Companion.getCancelInvitationPath
import uk.gov.communities.prsdb.webapp.controllers.TeamMembersController.Companion.TEAM_MEMBERS_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.cancelOrganisationalLandlordInvitation.CancelOrganisationalLandlordInvitationJourneyFactory
import uk.gov.communities.prsdb.webapp.journeys.cancelOrganisationalLandlordInvitation.stepConfig.AreYouSureStep
import uk.gov.communities.prsdb.webapp.services.ManageTeamMembersUrlProvider
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService

// TODO PDJB-1828: Delete this class when the MULTI_USER_ORGANISATIONS flag is removed
class CancelOrganisationalLandlordInvitationControllerFeatureFlagTests : FeatureFlagTestCallingEndpoints() {
    @MockitoBean
    private lateinit var journeyFactory: CancelOrganisationalLandlordInvitationJourneyFactory

    @MockitoBean
    private lateinit var userToLandlordService: UserToLandlordService

    @MockitoBean
    private lateinit var manageTeamMembersUrlProvider: ManageTeamMembersUrlProvider

    @MockitoBean
    private lateinit var stepLifecycleOrchestrator: StepLifecycleOrchestrator.VisitableStepLifecycleOrchestrator

    private val invitationId = 1L

    private fun mockJourneySteps() {
        whenever(journeyFactory.createJourneySteps(invitationId))
            .thenReturn(mapOf(AreYouSureStep.ROUTE_SEGMENT to stepLifecycleOrchestrator))
    }

    @WithMockUser(roles = ["ORG_ADMIN"])
    @Test
    fun `cancel invitation journey is unavailable if the multi-user organisations feature flag is disabled`() {
        featureFlagManager.disableFeature(MULTI_USER_ORGANISATIONS)

        mvc
            .get(getCancelInvitationPath(invitationId))
            .andExpect { status { isNotFound() } }
    }

    @WithMockUser(roles = ["ORG_ADMIN"])
    @Test
    fun `cancel invitation journey is available if the multi-user organisations feature flag is enabled`() {
        featureFlagManager.enableFeature(MULTI_USER_ORGANISATIONS)
        mockJourneySteps()
        whenever(stepLifecycleOrchestrator.getStepModelAndView()).thenReturn(ModelAndView("redirect:$TEAM_MEMBERS_ROUTE"))

        mvc
            .get(getCancelInvitationPath(invitationId))
            .andExpect { status { is3xxRedirection() } }
    }

    @WithMockUser(roles = ["ORG_ADMIN"])
    @Test
    fun `submitting the cancel invitation journey is unavailable if the multi-user organisations feature flag is disabled`() {
        featureFlagManager.disableFeature(MULTI_USER_ORGANISATIONS)

        mvc
            .post(getCancelInvitationPath(invitationId)) {
                param("formData", "")
                with(csrf())
            }.andExpect { status { isNotFound() } }
    }

    @WithMockUser(roles = ["ORG_ADMIN"])
    @Test
    fun `submitting the cancel invitation journey is available if the multi-user organisations feature flag is enabled`() {
        featureFlagManager.enableFeature(MULTI_USER_ORGANISATIONS)
        mockJourneySteps()
        whenever(stepLifecycleOrchestrator.postStepModelAndView(any()))
            .thenReturn(ModelAndView("redirect:$TEAM_MEMBERS_ROUTE"))

        mvc
            .post(getCancelInvitationPath(invitationId)) {
                param("formData", "")
                with(csrf())
            }.andExpect { status { is3xxRedirection() } }
    }
}
