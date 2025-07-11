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
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityInvitation
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.LaUserRegistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityDataService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityInvitationService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalAuthorityData

@WebMvcTest(RegisterLAUserController::class)
class RegisterLAUserControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockitoBean
    lateinit var laUserRegistrationJourneyFactory: LaUserRegistrationJourneyFactory

    @MockitoBean
    lateinit var invitationService: LocalAuthorityInvitationService

    @MockitoBean
    lateinit var localAuthorityDataService: LocalAuthorityDataService

    private val validToken = "token123"

    private val invalidToken = "invalid-token"

    private val expiredToken = "expired-token"

    @BeforeEach
    fun setupMocks() {
        whenever(invitationService.tokenIsValid(validToken)).thenReturn(true)
        whenever(invitationService.getTokenFromSession()).thenReturn(validToken)
        whenever(invitationService.tokenIsValid(invalidToken)).thenReturn(false)
    }

    @Test
    @WithMockUser
    fun `acceptInvitation endpoint stores valid token in session`() {
        mvc.get("${RegisterLAUserController.LA_USER_REGISTRATION_ROUTE}?$TOKEN=$validToken").andExpect {
            status { is3xxRedirection() }
        }

        verify(invitationService).tokenIsValid(validToken)
        verify(invitationService).storeTokenInSession(validToken)
    }

    @Test
    @WithMockUser
    fun `acceptInvitation endpoint rejects invalid token`() {
        mvc.get("${RegisterLAUserController.LA_USER_REGISTRATION_ROUTE}?$TOKEN=$invalidToken").andExpect {
            status { is3xxRedirection() }
            redirectedUrl(RegisterLAUserController.LA_USER_REGISTRATION_INVALID_LINK_ROUTE)
        }

        verify(invitationService).tokenIsValid(invalidToken)
        verify(invitationService, never()).storeTokenInSession(invalidToken)
    }

    @Test
    @WithMockUser
    fun `acceptInvitation endpoints deletes an expired token from the database and redirects to the invalid link page`() {
        val returnedInvitation = MockLocalAuthorityData.createLocalAuthorityInvitation()
        whenever(invitationService.tokenIsValid(expiredToken)).thenReturn(false)
        whenever(invitationService.getInvitationFromToken(expiredToken)).thenReturn(returnedInvitation)
        whenever(invitationService.getInvitationHasExpired(returnedInvitation)).thenReturn(true)

        mvc.get("${RegisterLAUserController.LA_USER_REGISTRATION_ROUTE}?$TOKEN=$expiredToken").andExpect {
            status { is3xxRedirection() }
            redirectedUrl(RegisterLAUserController.LA_USER_REGISTRATION_INVALID_LINK_ROUTE)
        }

        verify(invitationService).deleteInvitation(returnedInvitation)
        verify(invitationService, never()).storeTokenInSession(expiredToken)
    }

    @Test
    @WithMockUser
    fun `getConfirmation returns 200 if an LA user has been registered`() {
        val laUserId = 0L
        val localAuthorityUser = MockLocalAuthorityData.createLocalAuthorityUser()

        whenever(localAuthorityDataService.getLastUserIdRegisteredThisSession()).thenReturn(laUserId)
        whenever(localAuthorityDataService.getLocalAuthorityUserOrNull(laUserId)).thenReturn(localAuthorityUser)

        mvc
            .perform(
                MockMvcRequestBuilders
                    .get("${RegisterLAUserController.LA_USER_REGISTRATION_ROUTE}/$CONFIRMATION_PATH_SEGMENT")
                    .sessionAttr(LA_USER_ID, laUserId),
            ).andExpect(MockMvcResultMatchers.status().isOk())
    }

    @Test
    @WithMockUser
    fun `getConfirmation returns 400 if there's no LA user ID in session`() {
        val laUserId = 0L
        val localAuthorityUser = MockLocalAuthorityData.createLocalAuthorityUser()

        whenever(localAuthorityDataService.getLastUserIdRegisteredThisSession()).thenReturn(null)
        whenever(localAuthorityDataService.getLocalAuthorityUserOrNull(laUserId)).thenReturn(localAuthorityUser)

        mvc
            .get("${RegisterLAUserController.LA_USER_REGISTRATION_ROUTE}/$CONFIRMATION_PATH_SEGMENT")
            .andExpect { status { isBadRequest() } }
    }

    @Test
    @WithMockUser
    fun `getConfirmation returns 400 if the LA user ID in session is not valid`() {
        val laUserId = 0L

        whenever(localAuthorityDataService.getLastUserIdRegisteredThisSession()).thenReturn(laUserId)
        whenever(localAuthorityDataService.getLocalAuthorityUserOrNull(laUserId)).thenReturn(null)

        mvc
            .perform(
                MockMvcRequestBuilders
                    .get("${RegisterLAUserController.LA_USER_REGISTRATION_ROUTE}/$CONFIRMATION_PATH_SEGMENT")
                    .sessionAttr(LA_USER_ID, laUserId),
            ).andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    @WithMockUser
    fun `getLandingPage redirects if there is no valid token in the session and clears any token from the session`() {
        whenever(invitationService.getTokenFromSession()).thenReturn(null)
        mvc
            .get("${RegisterLAUserController.LA_USER_REGISTRATION_ROUTE}/$LANDING_PAGE_PATH_SEGMENT")
            .andExpect {
                status { is3xxRedirection() }
                redirectedUrl(RegisterLAUserController.LA_USER_REGISTRATION_INVALID_LINK_ROUTE)
            }

        verify(invitationService).clearTokenFromSession()
    }

    @Test
    @WithMockUser(roles = ["LA_USER"])
    fun `getLandingPage returns 302 for authenticated user with Local Authority role`() {
        whenever(userRolesService.getHasLocalAuthorityRole(any())).thenReturn(true)
        mvc
            .get("${RegisterLAUserController.LA_USER_REGISTRATION_ROUTE}/$LANDING_PAGE_PATH_SEGMENT") {
                with(oidcLogin())
            }.andExpectAll {
                status { is3xxRedirection() }
                redirectedUrl(LocalAuthorityDashboardController.LOCAL_AUTHORITY_DASHBOARD_URL)
            }
    }

    @Test
    @WithMockUser(roles = ["LA_USER"])
    fun `getLandingPage deletes the invitation for authenticated user with Local Authority role`() {
        val invitation = LocalAuthorityInvitation()
        whenever(invitationService.getInvitationFromToken(validToken)).thenReturn(invitation)
        whenever(userRolesService.getHasLocalAuthorityRole(any())).thenReturn(true)
        mvc
            .get("${RegisterLAUserController.LA_USER_REGISTRATION_ROUTE}/$LANDING_PAGE_PATH_SEGMENT") {
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
            .get("${RegisterLAUserController.LA_USER_REGISTRATION_ROUTE}/name")
            .andExpect {
                status { is3xxRedirection() }
                redirectedUrl(RegisterLAUserController.LA_USER_REGISTRATION_INVALID_LINK_ROUTE)
            }

        verify(invitationService).clearTokenFromSession()
    }
}
