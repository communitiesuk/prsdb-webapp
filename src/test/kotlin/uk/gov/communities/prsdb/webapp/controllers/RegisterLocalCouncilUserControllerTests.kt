package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDING_PAGE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LA_USER_ID
import uk.gov.communities.prsdb.webapp.constants.TOKEN
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncilInvitation
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.LocalCouncilUserRegistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.services.LocalCouncilDataService
import uk.gov.communities.prsdb.webapp.services.LocalCouncilInvitationService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalCouncilData

@WebMvcTest(RegisterLocalCouncilUserController::class)
class RegisterLocalCouncilUserControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    lateinit var localCouncilUserRegistrationJourneyFactory: LocalCouncilUserRegistrationJourneyFactory

    @MockitoBean
    lateinit var invitationService: LocalCouncilInvitationService

    @MockitoBean
    lateinit var localCouncilDataService: LocalCouncilDataService

    private val validToken = "token123"

    private val invalidToken = "invalid-token"

    private val expiredToken = "expired-token"

    private val invitation = MockLocalCouncilData.createLocalAuthorityInvitation()

    @BeforeEach
    fun setupMocks() {
        whenever(invitationService.tokenIsValid(validToken)).thenReturn(true)
        whenever(invitationService.getTokenFromSession()).thenReturn(validToken)
    }

    @Test
    @WithMockUser
    fun `acceptInvitation endpoint stores valid token in session and redirects to the registration landing page`() {
        whenever(invitationService.getInvitationOrNull(validToken)).thenReturn(invitation)
        whenever(invitationService.getInvitationHasExpired(invitation)).thenReturn(false)

        mvc.get("${RegisterLocalCouncilUserController.LA_USER_REGISTRATION_ROUTE}?$TOKEN=$validToken").andExpect {
            status { is3xxRedirection() }
            redirectedUrl("${RegisterLocalCouncilUserController.LA_USER_REGISTRATION_ROUTE}/$LANDING_PAGE_PATH_SEGMENT")
        }

        verify(invitationService).getInvitationOrNull(validToken)
        verify(invitationService).getInvitationHasExpired(invitation)
        verify(invitationService).storeTokenInSession(validToken)
    }

    @Test
    @WithMockUser
    fun `acceptInvitation endpoint rejects invalid token and redirects to the invalid link page`() {
        whenever(invitationService.getInvitationOrNull(invalidToken)).thenReturn(null)

        mvc.get("${RegisterLocalCouncilUserController.LA_USER_REGISTRATION_ROUTE}?$TOKEN=$invalidToken").andExpect {
            status { is3xxRedirection() }
            redirectedUrl(RegisterLocalCouncilUserController.LA_USER_REGISTRATION_INVALID_LINK_ROUTE)
        }

        verify(invitationService).getInvitationOrNull(invalidToken)
        verify(invitationService, never()).storeTokenInSession(invalidToken)
    }

    @Test
    @WithMockUser
    fun `acceptInvitation endpoints deletes an expired token from the database and redirects to the invalid link page`() {
        whenever(invitationService.getInvitationOrNull(expiredToken)).thenReturn(invitation)
        whenever(invitationService.getInvitationHasExpired(invitation)).thenReturn(true)

        mvc.get("${RegisterLocalCouncilUserController.LA_USER_REGISTRATION_ROUTE}?$TOKEN=$expiredToken").andExpect {
            status { is3xxRedirection() }
            redirectedUrl(RegisterLocalCouncilUserController.LA_USER_REGISTRATION_INVALID_LINK_ROUTE)
        }

        verify(invitationService).getInvitationOrNull(expiredToken)
        verify(invitationService).getInvitationHasExpired(invitation)
        verify(invitationService).deleteInvitation(invitation)
        verify(invitationService, never()).storeTokenInSession(expiredToken)
    }

    @Test
    @WithMockUser
    fun `getConfirmation returns 200 if an LA user has been registered`() {
        val laUserId = 0L
        val localAuthorityUser = MockLocalCouncilData.createLocalAuthorityUser()

        whenever(localCouncilDataService.getLastUserIdRegisteredThisSession()).thenReturn(laUserId)
        whenever(localCouncilDataService.getLocalAuthorityUserOrNull(laUserId)).thenReturn(localAuthorityUser)

        mvc
            .perform(
                MockMvcRequestBuilders
                    .get("${RegisterLocalCouncilUserController.LA_USER_REGISTRATION_ROUTE}/$CONFIRMATION_PATH_SEGMENT")
                    .sessionAttr(LA_USER_ID, laUserId),
            ).andExpect(MockMvcResultMatchers.status().isOk())
    }

    @Test
    @WithMockUser
    fun `getConfirmation returns 400 if there's no LA user ID in session`() {
        val laUserId = 0L
        val localAuthorityUser = MockLocalCouncilData.createLocalAuthorityUser()

        whenever(localCouncilDataService.getLastUserIdRegisteredThisSession()).thenReturn(null)
        whenever(localCouncilDataService.getLocalAuthorityUserOrNull(laUserId)).thenReturn(localAuthorityUser)

        mvc
            .get("${RegisterLocalCouncilUserController.LA_USER_REGISTRATION_ROUTE}/$CONFIRMATION_PATH_SEGMENT")
            .andExpect { status { isBadRequest() } }
    }

    @Test
    @WithMockUser
    fun `getConfirmation returns 400 if the LA user ID in session is not valid`() {
        val laUserId = 0L

        whenever(localCouncilDataService.getLastUserIdRegisteredThisSession()).thenReturn(laUserId)
        whenever(localCouncilDataService.getLocalAuthorityUserOrNull(laUserId)).thenReturn(null)

        mvc
            .perform(
                MockMvcRequestBuilders
                    .get("${RegisterLocalCouncilUserController.LA_USER_REGISTRATION_ROUTE}/$CONFIRMATION_PATH_SEGMENT")
                    .sessionAttr(LA_USER_ID, laUserId),
            ).andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    @WithMockUser
    fun `getLandingPage redirects if there is no valid token in the session and clears any token from the session`() {
        whenever(invitationService.getTokenFromSession()).thenReturn(null)
        mvc
            .get("${RegisterLocalCouncilUserController.LA_USER_REGISTRATION_ROUTE}/$LANDING_PAGE_PATH_SEGMENT")
            .andExpect {
                status { is3xxRedirection() }
                redirectedUrl(RegisterLocalCouncilUserController.LA_USER_REGISTRATION_INVALID_LINK_ROUTE)
            }

        verify(invitationService).clearTokenFromSession()
    }

    @Test
    @WithMockUser(roles = ["LA_USER"])
    fun `getLandingPage returns 302 for authenticated user with Local Authority role`() {
        whenever(userRolesService.getHasLocalAuthorityRole(any())).thenReturn(true)
        mvc
            .get("${RegisterLocalCouncilUserController.LA_USER_REGISTRATION_ROUTE}/$LANDING_PAGE_PATH_SEGMENT") {
                with(oidcLogin())
            }.andExpectAll {
                status { is3xxRedirection() }
                redirectedUrl(LocalCouncilDashboardController.LOCAL_AUTHORITY_DASHBOARD_URL)
            }
    }

    @Test
    @WithMockUser(roles = ["LA_USER"])
    fun `getLandingPage deletes the invitation for authenticated user with Local Authority role`() {
        val invitation = LocalCouncilInvitation()
        whenever(invitationService.getInvitationFromToken(validToken)).thenReturn(invitation)
        whenever(userRolesService.getHasLocalAuthorityRole(any())).thenReturn(true)
        mvc
            .get("${RegisterLocalCouncilUserController.LA_USER_REGISTRATION_ROUTE}/$LANDING_PAGE_PATH_SEGMENT") {
                with(oidcLogin())
            }

        verify(invitationService).deleteInvitation(invitation)
        verify(invitationService).clearTokenFromSession()
    }

    @Test
    @WithMockUser
    fun `getJourneyStep redirects if there is no valid token in the session and clears any token from the session`() {
        whenever(invitationService.getTokenFromSession()).thenReturn(null)
        mvc
            .get("${RegisterLocalCouncilUserController.LA_USER_REGISTRATION_ROUTE}/name")
            .andExpect {
                status { is3xxRedirection() }
                redirectedUrl(RegisterLocalCouncilUserController.LA_USER_REGISTRATION_INVALID_LINK_ROUTE)
            }

        verify(invitationService).clearTokenFromSession()
    }
}
