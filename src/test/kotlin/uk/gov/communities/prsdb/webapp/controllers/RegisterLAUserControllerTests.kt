package uk.gov.communities.prsdb.webapp.controllers

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.forms.journeys.LaUserRegistrationJourney
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLaUserStepId
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
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

    @BeforeEach
    fun setupMocks() {
        whenever(laUserRegistrationJourney.initialStepId).thenReturn(RegisterLaUserStepId.LandingPage)
        whenever(invitationService.tokenIsValid("token123")).thenReturn(true)
        whenever(invitationService.tokenIsValid("invalid-token")).thenReturn(false)
    }

    @Test
    fun `acceptInvitation endpoint checks token and stores in session if valid`() {
        mvc.get("/register-local-authority-user/?token=token123").andExpect {
            status { is3xxRedirection() }
        }

        verify(invitationService).tokenIsValid("token123")
        verify(invitationService).storeTokenInSession("token123")
    }

    @Test
    fun `acceptInvitation endpoint rejects invalid token`() {
        mvc.get("/register-local-authority-user/?token=invalid-token").andExpect {
            status { is3xxRedirection() }
        }

        verify(invitationService).tokenIsValid("invalid-token")
        verify(invitationService, never()).storeTokenInSession("invalid-token")
    }

    @Test
    fun `acceptInvitation prepopulates the email address in journeyData`() {
        whenever(invitationService.getEmailAddressForToken("token123")).thenReturn("invite@example.com")

        mvc.get("/register-local-authority-user/?token=invalid-token").andExpect {
            status { is3xxRedirection() }
        }

        val formData = mutableMapOf<String, Any?>("emailAddress" to "invite@example.com")
        val journeyData = mutableMapOf<String, Any?>("email" to formData)

        verify(journeyDataService).setJourneyData(journeyData)
    }
}
