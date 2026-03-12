package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.exceptions.TransientEmailSentException
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.InviteLocalCouncilAdminConfirmationPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.InviteLocalCouncilAdminPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LocalCouncilAdminInvitationEmail
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import java.net.URI

class InviteLocalCouncilAdminTests : IntegrationTestWithMutableData("data-local.sql") {
    @MockitoBean
    private lateinit var invitationEmailSender: EmailNotificationService<LocalCouncilAdminInvitationEmail>

    @BeforeEach
    fun setup() {
        whenever(absoluteUrlProvider.buildInvitationUri(anyString(), any()))
            .thenReturn(URI("www.prsd.gov.uk/register-local-council-user/test-token"))
    }

    @Test
    fun `inviting a new Local Council admin ends with a confirmation page`(page: Page) {
        val invitePage = navigator.goToInviteLocalCouncilAdmin()
        invitePage.fillInFormAndSubmit("BATH AND ", "BATH AND NORTH EAST SOMERSET COUNCIL", "admin@example.com", "admin@example.com")

        // Confirmation page
        val confirmationPage = assertPageIs(page, InviteLocalCouncilAdminConfirmationPage::class)
        assertThat(confirmationPage.confirmationBanner).containsText("admin@example.com")
        assertThat(confirmationPage.confirmationBanner).containsText("BATH AND NORTH EAST SOMERSET COUNCIL")

        // Invite another user
        confirmationPage.inviteAnotherUserButton.clickAndWait()
        assertPageIs(page, InviteLocalCouncilAdminPage::class)

        // TODO PRSD-672 - check the Return to Dashboard button
    }

    @Test
    fun `if a TransientEmailException is returned the page is re-rendered with an error message`() {
        val emailAddressTriggeringTransientError = "transient@example.com"
        whenever(invitationEmailSender.sendEmail(eq(emailAddressTriggeringTransientError), any()))
            .thenThrow(TransientEmailSentException("email failed"))

        val invitePage = navigator.goToInviteLocalCouncilAdmin()
        invitePage.fillInFormAndSubmit(
            "BATH AND ",
            "BATH AND NORTH EAST SOMERSET COUNCIL",
            emailAddressTriggeringTransientError,
            emailAddressTriggeringTransientError,
        )
        assertThat(invitePage.errorSummary).containsText("Please try again")
    }
}
