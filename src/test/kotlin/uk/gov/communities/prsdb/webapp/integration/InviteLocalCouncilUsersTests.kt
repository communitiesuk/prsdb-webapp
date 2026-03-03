package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.exceptions.TransientEmailSentException
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.InviteNewLocalCouncilUserSuccessPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LocalCouncilDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LocalCouncilInvitationEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LocalCouncilUserInvitationInformAdminEmail
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import java.net.URI

class InviteLocalCouncilUsersTests : IntegrationTestWithMutableData("data-local.sql") {
    @MockitoBean
    private lateinit var invitationConfirmationSenderAdmin: EmailNotificationService<LocalCouncilUserInvitationInformAdminEmail>

    @MockitoBean
    private lateinit var invitationEmailSender: EmailNotificationService<LocalCouncilInvitationEmail>

    @BeforeEach
    fun setup() {
        whenever(absoluteUrlProvider.buildInvitationUri(anyString()))
            .thenReturn(URI("www.prsd.gov.uk/register-local-council-user/test-token"))
        whenever(absoluteUrlProvider.buildLocalCouncilDashboardUri()).thenReturn(URI("https:gov.uk"))
    }

    @Test
    fun `inviting a new LocalCouncil user ends with a success page with a button linking to the dashboard`(page: Page) {
        val invitePage = navigator.goToInviteNewLocalCouncilUser(1)
        invitePage.submitMatchingEmail("test@example.com")
        val successPage = assertPageIs(page, InviteNewLocalCouncilUserSuccessPage::class, mapOf("localCouncilId" to "1"))
        assertThat(successPage.confirmationBanner).containsText("You’ve sent test@example.com an invite to the database")

        verify(invitationConfirmationSenderAdmin, times(5)).sendEmail(
            any(),
            any(),
        )

        // Go to dashboard button
        successPage.returnToDashboardButton.clickAndWait()
        assertPageIs(page, LocalCouncilDashboardPage::class)
    }

    @Test
    fun `if a TransientEmailException is returned the page is re-rendered with an error message`() {
        val emailAddressTriggeringTransientError = "transient@example.com"
        whenever(invitationEmailSender.sendEmail(eq(emailAddressTriggeringTransientError), any()))
            .thenThrow(TransientEmailSentException("email failed"))

        val invitePage = navigator.goToInviteNewLocalCouncilUser(2)
        invitePage.submitMatchingEmail(emailAddressTriggeringTransientError)
        assertThat(invitePage.errorSummary).containsText("Please try again")
    }
}
