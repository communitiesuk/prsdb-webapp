package uk.gov.communities.prsdb.webapp.urlProviders

import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.constants.LOCAL_COUNCIL_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.ControllerTest
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalCouncilUsersController
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalCouncilUsersController.Companion.getLocalCouncilInviteNewUserRoute
import uk.gov.communities.prsdb.webapp.controllers.RegisterLocalCouncilUserController
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncil
import uk.gov.communities.prsdb.webapp.forms.journeys.LocalCouncilUserRegistrationJourney
import uk.gov.communities.prsdb.webapp.forms.journeys.factories.LocalCouncilUserRegistrationJourneyFactory
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterLocalCouncilUserStepId
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplateModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LocalCouncilInvitationEmail
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.LocalCouncilDataService
import uk.gov.communities.prsdb.webapp.services.LocalCouncilInvitationService
import uk.gov.communities.prsdb.webapp.services.LocalCouncilService
import uk.gov.communities.prsdb.webapp.services.SecurityContextService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalCouncilData.Companion.createdLoggedInUserModel
import java.net.URLEncoder
import kotlin.test.Test

@WebMvcTest(
    controllers = [ManageLocalCouncilUsersController::class, RegisterLocalCouncilUserController::class],
    properties = ["base-url.local-council=http://localhost:8080/$LOCAL_COUNCIL_PATH_SEGMENT"],
)
@Import(AbsoluteUrlProvider::class)
class InvitationUrlTests(
    context: WebApplicationContext,
) : ControllerTest(context) {
    @MockitoBean
    lateinit var anyEmailNotificationService: EmailNotificationService<EmailTemplateModel>

    @MockitoBean
    lateinit var localCouncilInvitationService: LocalCouncilInvitationService

    @MockitoBean
    private lateinit var localCouncilDataService: LocalCouncilDataService

    @MockitoBean
    private lateinit var localCouncilService: LocalCouncilService

    @MockitoBean
    private lateinit var securityContextService: SecurityContextService

    @MockitoBean
    private lateinit var journeyDataService: JourneyDataService

    @MockitoBean
    private lateinit var localCouncilUserRegistrationJourneyFactory: LocalCouncilUserRegistrationJourneyFactory

    @Mock
    private lateinit var localCouncilUserRegistrationJourney: LocalCouncilUserRegistrationJourney

    @Test
    @WithMockUser(roles = ["LOCAL_COUNCIL_ADMIN"])
    fun `The invitation URL generated when a new user is invited is routed to the accept invitation controller method`() {
        // Arrange
        val loggedInUser = createdLoggedInUserModel()
        val localCouncil = createTestLocalCouncil()
        val testToken = "test token"
        val testEmail = "test@example.com"

        whenever(localCouncilDataService.getUserAndLocalCouncilIfAuthorizedUser(123, "user")).thenReturn(
            Pair(
                loggedInUser,
                localCouncil,
            ),
        )

        whenever(localCouncilInvitationService.createInvitationToken(testEmail, localCouncil)).thenReturn(testToken)
        whenever(localCouncilInvitationService.getAuthorityForToken(testToken)).thenReturn(localCouncil)
        whenever(localCouncilInvitationService.tokenIsValid(testToken)).thenReturn(true)

        val invitationCaptor = argumentCaptor<LocalCouncilInvitationEmail>()
        Mockito
            .doNothing()
            .whenever(
                anyEmailNotificationService,
            ).sendEmail(any(), invitationCaptor.capture())

        val encodedConfirmedEmailContent = urlEncodedConfirmedEmailDataModel(testEmail)

        whenever(localCouncilUserRegistrationJourneyFactory.create(any())).thenReturn(localCouncilUserRegistrationJourney)
        whenever(localCouncilUserRegistrationJourney.initialStepId).thenReturn(RegisterLocalCouncilUserStepId.LandingPage)

        // Act
        mvc
            .post(getLocalCouncilInviteNewUserRoute(123)) {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                content = encodedConfirmedEmailContent
                with(csrf())
            }.andExpect { status { is3xxRedirection() } }

        mvc
            .get(invitationCaptor.firstValue.invitationUri)
            .andExpect { status { is3xxRedirection() } }

        // Assert
        verify(localCouncilInvitationService).getInvitationOrNull(testToken)
    }

    @Suppress("SameParameterValue")
    private fun urlEncodedConfirmedEmailDataModel(testEmail: String): String {
        val encodedTestEmail = URLEncoder.encode(testEmail, "UTF-8")
        return "email=$encodedTestEmail&confirmEmail=$encodedTestEmail"
    }

    private fun createTestLocalCouncil(): LocalCouncil {
        val localCouncil = LocalCouncil()
        ReflectionTestUtils.setField(localCouncil, "id", 123)
        ReflectionTestUtils.setField(localCouncil, "name", "Test Local Council")
        return localCouncil
    }
}
