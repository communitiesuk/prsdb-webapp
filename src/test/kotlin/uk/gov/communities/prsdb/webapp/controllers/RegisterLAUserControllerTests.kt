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
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLaUserStepId
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.mockObjects.MockLocalAuthorityData
import uk.gov.communities.prsdb.webapp.models.formModels.EmailFormModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
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
    lateinit var journeyDataService: JourneyDataService

    @MockBean
    lateinit var localAuthorityDataService: LocalAuthorityDataService

    @BeforeEach
    fun setupMocks() {
        whenever(laUserRegistrationJourney.initialStepId).thenReturn(RegisterLaUserStepId.LandingPage)
        whenever(invitationService.tokenIsValid("token123")).thenReturn(true)
        whenever(invitationService.tokenIsValid("invalid-token")).thenReturn(false)
        whenever(invitationService.getTokenFromSession()).thenReturn("token123")
    }

    @Test
    @WithMockUser
    fun `acceptInvitation endpoint checks token and stores in session if valid`() {
        mvc.get("/register-local-authority-user?token=token123").andExpect {
            status { is3xxRedirection() }
        }

        verify(invitationService).tokenIsValid("token123")
        verify(invitationService).storeTokenInSession("token123")
    }

    @Test
    @WithMockUser
    fun `acceptInvitation endpoint rejects invalid token`() {
        mvc.get("/register-local-authority-user?token=invalid-token").andExpect {
            status { is3xxRedirection() }
        }

        verify(invitationService).tokenIsValid("invalid-token")
        verify(invitationService, never()).storeTokenInSession("invalid-token")
    }

    @Test
    @WithMockUser
    fun `acceptInvitation prepopulates the email address in journeyData`() {
        whenever(laUserRegistrationJourney.steps).thenReturn(
            setOf(
                Step(
                    id = RegisterLaUserStepId.Email,
                    page =
                        Page(
                            EmailFormModel::class,
                            "forms/emailForm",
                            mutableMapOf("testKey" to "testValue"),
                        ),
                ),
            ),
        )
        whenever(invitationService.getEmailAddressForToken("token123")).thenReturn("invite@example.com")

        mvc.get("/register-local-authority-user?token=token123").andExpect {
            status { is3xxRedirection() }
        }

        val formData = mutableMapOf<String, Any?>("emailAddress" to "invite@example.com")
        val journeyData = mutableMapOf<String, Any?>("email" to formData)

        verify(journeyDataService).setJourneyData(journeyData)
    }

    @Test
    @WithMockUser
    fun `getConfirmation returns 200 if an LA user has been registered`() {
        val laUserId = 0L
        val localAuthorityUser = MockLocalAuthorityData.createLocalAuthorityUser()

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

        whenever(localAuthorityDataService.getLocalAuthorityUserOrNull(laUserId)).thenReturn(localAuthorityUser)

        mvc
            .get("/$REGISTER_LA_USER_JOURNEY_URL/$CONFIRMATION_PAGE_PATH_SEGMENT")
            .andExpect { status { isBadRequest() } }
    }

    @Test
    @WithMockUser
    fun `getConfirmation returns 400 if the LA user ID in session is not valid`() {
        val laUserId = 0L

        whenever(localAuthorityDataService.getLocalAuthorityUserOrNull(laUserId)).thenReturn(null)

        mvc
            .perform(
                MockMvcRequestBuilders
                    .get("/$REGISTER_LA_USER_JOURNEY_URL/$CONFIRMATION_PAGE_PATH_SEGMENT")
                    .sessionAttr(LA_USER_ID, laUserId),
            ).andExpect(MockMvcResultMatchers.status().isBadRequest)
    }
}
