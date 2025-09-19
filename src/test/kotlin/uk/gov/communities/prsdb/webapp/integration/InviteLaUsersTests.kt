package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.InviteNewLaUserSuccessPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LocalAuthorityDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LocalCouncilUserInvitationInformAdminEmail
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import java.net.URI

class InviteLaUsersTests : IntegrationTestWithMutableData("data-local.sql") {
    @MockitoBean
    private lateinit var sendUserInvitedEmailsToAdmins: EmailNotificationService<LocalCouncilUserInvitationInformAdminEmail>

    @Test
    fun `inviting a new LA user ends with a success page with a button linking to the dashboard`(page: Page) {
        whenever(absoluteUrlProvider.buildInvitationUri(anyString()))
            .thenReturn(URI("www.prsd.gov.uk/register-la-user/test-token"))
        whenever(absoluteUrlProvider.buildLocalAuthorityDashboardUri()).thenReturn(URI("https:gov.uk"))

        val invitePage = navigator.goToInviteNewLaUser(2)
        invitePage.submitMatchingEmail("test@example.com")
        val successPage = assertPageIs(page, InviteNewLaUserSuccessPage::class)
        assertThat(successPage.confirmationBanner).containsText("You’ve sent test@example.com an invite to the database")

        verify(sendUserInvitedEmailsToAdmins).sendEmail(
            eq("Ford.Prefect@test.com"),
            any(),
        )

        // Go to dashboard button
        successPage.returnToDashboardButton.clickAndWait()
        assertPageIs(page, LocalAuthorityDashboardPage::class)
    }
}
