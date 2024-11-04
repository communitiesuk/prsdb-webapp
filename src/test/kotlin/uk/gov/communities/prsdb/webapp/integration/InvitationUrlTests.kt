package uk.gov.communities.prsdb.webapp.integration

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
import uk.gov.communities.prsdb.webapp.controllers.ExampleInvitationTokenController
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalAuthorityUsersController
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.models.viewModels.LocalAuthorityInvitationEmail
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityDataService
import java.net.URLEncoder
import kotlin.test.Test

// TODO PRSD-405: Update the controller here to be the accept invitation controller
@WebMvcTest(ManageLocalAuthorityUsersController::class, ExampleInvitationTokenController::class)
class InvitationUrlTests(
    context: WebApplicationContext,
) : ControllerTest(context) {
    @MockBean
    private lateinit var localAuthorityDataService: LocalAuthorityDataService

    @Test
    @WithMockUser(roles = ["LA_ADMIN"])
    fun `The invitation URL generated when a new user is invited is routed to the accept invitation controller method`() {
        // Arrange
        val localAuthority = createTestLocalAuthority()
        val testToken = "test token"
        val testEmail = "test@example.com"

        whenever(localAuthorityDataService.getLocalAuthorityForUser("user")).thenReturn(localAuthority)

        whenever(localAuthorityInvitationService.createInvitationToken(testEmail, localAuthority)).thenReturn(testToken)
        whenever(localAuthorityInvitationService.getAuthorityForToken(testToken)).thenReturn(localAuthority)
        whenever(localAuthorityInvitationService.buildInvitationUri(testToken)).thenCallRealMethod()

        val invitationCaptor = argumentCaptor<LocalAuthorityInvitationEmail>()
        Mockito
            .doNothing()
            .whenever(
                anyEmailNotificationService,
            ).sendEmail(any(), invitationCaptor.capture())

        val encodedConfirmedEmailContent = urlEncodedConfirmedEmailDataModel(testEmail)

        // Act
        mvc
            .post("/local-authority/1/manage-users/invite-new-user") {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                content = encodedConfirmedEmailContent
                with(csrf())
            }.andExpect { status { isOk() } }

        mvc
            .get(invitationCaptor.firstValue.invitationUri)
            .andExpect { status { isOk() } }

        // Assert
        verify(localAuthorityInvitationService).getAuthorityForToken(testToken)
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
