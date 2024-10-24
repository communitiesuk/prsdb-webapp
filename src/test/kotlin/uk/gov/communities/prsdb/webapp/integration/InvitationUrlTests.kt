package uk.gov.communities.prsdb.webapp.integration

import org.mockito.ArgumentCaptor
import org.mockito.ArgumentCaptor.captor
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
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
import java.net.URLEncoder
import kotlin.test.Test

// TODO PRSD-405: Update the controller here to be the accept invitation controller
@WebMvcTest(ManageLocalAuthorityUsersController::class, ExampleInvitationTokenController::class)
class InvitationUrlTests(
    context: WebApplicationContext,
) : ControllerTest(context) {
    // There is an issue with using Mockito in Kotlin, as Mockito was designed to be used with Java.
    // Java methods by default accept null, whereas Kotlin does not accept null unless the type is explicitly marked as nullable.
    // Various Mockito mock methods (any(), capture(), etc.) have the correct type at compile time, but actually return null -
    // this causes an exception in Kotlin. These two methods allow us to 'hide' the underlying null from Kotlin while still letting
    // Mockito do its thing.
    // Source - http://derekwilson.net/blog/2018/08/23/mokito-kotlin via Stack Overflow
    private fun <T> kotlinCapture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()

    private fun <T> kotlinAny(): T {
        @Suppress("UNCHECKED_CAST")
        fun <T> uninitialized(): T = null as T

        Mockito.any<T>()
        return uninitialized()
    }

    @Test
    @WithMockUser(roles = ["LA_ADMIN"])
    fun `The invitation URL generated when a new user is invited is routed to the accept invitation controller method`() {
        // Arrange
        val localAuthority = createTestLocalAuthority()
        val testToken = "test token"
        val testEmail = "test@example.com"

        Mockito.`when`(localAuthorityDataService.getLocalAuthorityForUser("user")).thenReturn(localAuthority)

        Mockito.`when`(localAuthorityInvitationService.createInvitationToken(testEmail, localAuthority)).thenReturn(testToken)
        Mockito.`when`(localAuthorityInvitationService.getAuthorityForToken(testToken)).thenReturn(localAuthority)
        Mockito.`when`(localAuthorityInvitationService.buildInvitationUri(testToken)).thenCallRealMethod()

        val invitationCaptor = captor<LocalAuthorityInvitationEmail>()
        Mockito
            .doNothing()
            .`when`(
                anyEmailNotificationService,
            ).sendEmail(kotlinAny(), kotlinCapture<LocalAuthorityInvitationEmail>(invitationCaptor))

        val encodedConfirmedEmailContent = urlEncodedConfirmedEmailDataModel(testEmail)

        // Act
        mvc
            .post("/manage-users/invite-new-user") {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
                content = encodedConfirmedEmailContent
                with(csrf())
            }.andExpect { status { isOk() } }

        mvc
            .get(invitationCaptor.value.invitationUri)
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
