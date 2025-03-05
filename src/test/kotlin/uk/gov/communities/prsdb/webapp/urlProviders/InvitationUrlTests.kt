package uk.gov.communities.prsdb.webapp.urlProviders

import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.controllers.ControllerTest
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalAuthorityUsersController
import uk.gov.communities.prsdb.webapp.controllers.RegisterLAUserController
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.forms.journeys.LaUserRegistrationJourney
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLaUserStepId
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplateModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LocalAuthorityInvitationEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityDataService
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityInvitationService
import uk.gov.communities.prsdb.webapp.testHelpers.MockLocalAuthorityData.Companion.createdLoggedInUserModel
import java.net.URLEncoder
import kotlin.test.Test

@WebMvcTest(ManageLocalAuthorityUsersController::class, RegisterLAUserController::class)
class InvitationUrlTests(
    context: WebApplicationContext,
) : ControllerTest(context) {
    @MockBean
    lateinit var anyEmailNotificationService: EmailNotificationService<EmailTemplateModel>

    @MockBean
    lateinit var localAuthorityInvitationService: LocalAuthorityInvitationService

    @MockBean
    lateinit var absoluteUrlProvider: AbsoluteUrlProvider

    @MockBean
    private lateinit var localAuthorityDataService: LocalAuthorityDataService

    @MockBean
    private lateinit var journeyDataService: JourneyDataService

    @MockBean
    private lateinit var laUserRegistrationJourney: LaUserRegistrationJourney

    @Test
    @WithMockUser(roles = ["LA_ADMIN"])
    fun `The invitation URL generated when a new user is invited is routed to the accept invitation controller method`() {
        // Arrange
        val loggedInUser = createdLoggedInUserModel()
        val localAuthority = createTestLocalAuthority()
        val testToken = "test token"
        val testEmail = "test@example.com"

        whenever(localAuthorityDataService.getUserAndLocalAuthorityIfAuthorizedUser(123, "user")).thenReturn(
            Pair(
                loggedInUser,
                localAuthority,
            ),
        )

        whenever(localAuthorityInvitationService.createInvitationToken(testEmail, localAuthority)).thenReturn(testToken)
        whenever(localAuthorityInvitationService.getAuthorityForToken(testToken)).thenReturn(localAuthority)
        whenever(absoluteUrlProvider.buildInvitationUri(testToken)).thenCallRealMethod()
        whenever(localAuthorityInvitationService.tokenIsValid(testToken)).thenReturn(true)

        val invitationCaptor = argumentCaptor<LocalAuthorityInvitationEmail>()
        Mockito
            .doNothing()
            .whenever(
                anyEmailNotificationService,
            ).sendEmail(any(), invitationCaptor.capture())

        val encodedConfirmedEmailContent = urlEncodedConfirmedEmailDataModel(testEmail)

        whenever(laUserRegistrationJourney.initialStepId).thenReturn(RegisterLaUserStepId.LandingPage)

        // Act
        mvc
            .post("/local-authority/123/invite-new-user") {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                content = encodedConfirmedEmailContent
                with(csrf())
            }.andExpect { status { is3xxRedirection() } }

        mvc
            .get(invitationCaptor.firstValue.invitationUri)
            .andExpect { status { is3xxRedirection() } }

        // Assert
        verify(localAuthorityInvitationService).tokenIsValid(testToken)
    }

    @Suppress("SameParameterValue")
    private fun urlEncodedConfirmedEmailDataModel(testEmail: String): String {
        val encodedTestEmail = URLEncoder.encode(testEmail, "UTF-8")
        return "email=$encodedTestEmail&confirmEmail=$encodedTestEmail"
    }

    private fun createTestLocalAuthority(): LocalAuthority {
        val localAuthority = LocalAuthority()
        ReflectionTestUtils.setField(localAuthority, "id", 123)
        ReflectionTestUtils.setField(localAuthority, "name", "Test Local Authority")
        return localAuthority
    }
}
