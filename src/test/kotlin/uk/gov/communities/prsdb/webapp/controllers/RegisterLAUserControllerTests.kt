package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.constants.LA_USER_ID
import uk.gov.communities.prsdb.webapp.constants.REGISTER_LA_USER_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.controllers.RegisterLAUserController.Companion.CONFIRMATION_PAGE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.forms.journeys.LaUserRegistrationJourney
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLaUserStepId
import uk.gov.communities.prsdb.webapp.mockObjects.MockLocalAuthorityData
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityDataService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityInvitationService

@WebMvcTest(RegisterLAUserController::class)
class RegisterLAUserControllerTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @MockBean
    lateinit var laUserRegistrationJourney: LaUserRegistrationJourney

    @MockBean
    lateinit var invitationService: LocalAuthorityInvitationService

    @MockBean
    lateinit var localAuthorityDataService: LocalAuthorityDataService

    private val validToken = "token123"

    private val invalidToken = "invalid-token"

    @BeforeEach
    fun setupMocks() {
        whenever(laUserRegistrationJourney.initialStepId).thenReturn(RegisterLaUserStepId.LandingPage)
        whenever(invitationService.tokenIsValid(validToken)).thenReturn(true)
        whenever(invitationService.getTokenFromSession()).thenReturn(validToken)
        whenever(invitationService.tokenIsValid(invalidToken)).thenReturn(false)
    }

    @Test
    @WithMockUser
    fun `acceptInvitation endpoint stores valid token in session and uses it to initialise journey data`() {
        mvc.get("/register-local-authority-user?token=$validToken").andExpect {
            status { is3xxRedirection() }
        }

        verify(invitationService).tokenIsValid(validToken)
        verify(invitationService).storeTokenInSession(validToken)
        verify(laUserRegistrationJourney).initialiseJourneyData(validToken)
    }

    @Test
    @WithMockUser
    fun `acceptInvitation endpoint rejects invalid token`() {
        mvc.get("/register-local-authority-user?token=$invalidToken").andExpect {
            status { is3xxRedirection() }
        }

        verify(invitationService).tokenIsValid(invalidToken)
        verify(invitationService, never()).storeTokenInSession(invalidToken)
        verify(laUserRegistrationJourney, never()).initialiseJourneyData(validToken)
    }

    @Test
    @WithMockUser
    fun `getConfirmation returns 200 if an LA user has been registered`() {
        val laUserId = 0L
        val localAuthorityUser = MockLocalAuthorityData.createLocalAuthorityUser()

        whenever(localAuthorityDataService.getUserRegisteredThisSession()).thenReturn(laUserId)
        whenever(localAuthorityDataService.getLocalAuthorityUserOrNull(laUserId)).thenReturn(localAuthorityUser)

        mvc
            .perform(
                MockMvcRequestBuilders
                    .get("/$REGISTER_LA_USER_JOURNEY_URL/$CONFIRMATION_PAGE_PATH_SEGMENT")
                    .sessionAttr(LA_USER_ID, laUserId),
            ).andExpect(MockMvcResultMatchers.status().isOk())
    }

    @Test
    @WithMockUser
    fun `getConfirmation returns 400 if there's no LA user ID in session`() {
        val laUserId = 0L
        val localAuthorityUser = MockLocalAuthorityData.createLocalAuthorityUser()

        whenever(localAuthorityDataService.getUserRegisteredThisSession()).thenReturn(null)
        whenever(localAuthorityDataService.getLocalAuthorityUserOrNull(laUserId)).thenReturn(localAuthorityUser)

        mvc
            .get("/$REGISTER_LA_USER_JOURNEY_URL/$CONFIRMATION_PAGE_PATH_SEGMENT")
            .andExpect { status { isBadRequest() } }
    }

    @Test
    @WithMockUser
    fun `getConfirmation returns 400 if the LA user ID in session is not valid`() {
        val laUserId = 0L

        whenever(localAuthorityDataService.getUserRegisteredThisSession()).thenReturn(laUserId)
        whenever(localAuthorityDataService.getLocalAuthorityUserOrNull(laUserId)).thenReturn(null)

        mvc
            .perform(
                MockMvcRequestBuilders
                    .get("/$REGISTER_LA_USER_JOURNEY_URL/$CONFIRMATION_PAGE_PATH_SEGMENT")
                    .sessionAttr(LA_USER_ID, laUserId),
            ).andExpect(MockMvcResultMatchers.status().isBadRequest)
    }
}
