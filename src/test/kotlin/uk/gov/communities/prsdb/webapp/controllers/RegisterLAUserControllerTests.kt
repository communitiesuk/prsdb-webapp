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
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityInvitation
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityUserRepository
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.journeys.LaUserRegistrationJourney
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLaUserStepId
import uk.gov.communities.prsdb.webapp.forms.steps.Step
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

    @MockBean
    lateinit var localAuthorityUserRepository: LocalAuthorityUserRepository

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
            listOf(
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
    fun `submitRegistration calls registerNewUser with journeyData values from the session`() {
        // Arrange
        val journeyData: JourneyData =
            mutableMapOf(
                "name" to mutableMapOf("name" to "Test Username"),
                "email" to mutableMapOf("emailAddress" to "test@example.com"),
            )
        whenever(journeyDataService.getJourneyDataFromSession()).thenReturn(journeyData)

        val localAuthority = LocalAuthority(1, "Local Authority 1")
        whenever(invitationService.getAuthorityForToken("token123")).thenReturn(localAuthority)

        // Act
        mvc.get("/register-local-authority-user/success").andExpect {
            status { isOk() }
        }

        // Assert
        verify(localAuthorityDataService).registerNewUser(
            "user",
            localAuthority,
            "Test Username",
            "test@example.com",
        )
    }

    @Test
    @WithMockUser
    fun `submitRegistration calls deleteInvitation and clears data from the session`() {
        // Arrange
        val journeyData: JourneyData =
            mutableMapOf(
                "name" to mutableMapOf("name" to "Test Username"),
                "email" to mutableMapOf("emailAddress" to "test@example.com"),
            )
        whenever(journeyDataService.getJourneyDataFromSession()).thenReturn(journeyData)

        val localAuthority = LocalAuthority(1, "Local Authority 1")
        whenever(invitationService.getAuthorityForToken("token123")).thenReturn(localAuthority)

        val invitation = LocalAuthorityInvitation()
        whenever(invitationService.getInvitationFromToken("token123")).thenReturn(invitation)

        // Act
        mvc.get("/register-local-authority-user/success").andExpect {
            status { isOk() }
        }

        // Assert
        verify(invitationService).deleteInvitation(invitation)
        verify(invitationService).clearTokenFromSession()
        verify(journeyDataService).clearJourneyDataFromSession()
    }
}
