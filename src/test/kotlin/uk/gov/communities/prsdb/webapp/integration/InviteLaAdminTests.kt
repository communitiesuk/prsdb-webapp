package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.InviteLaAdminConfirmationPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.InviteLaAdminPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import java.net.URI

class InviteLaAdminTests : JourneyTestWithSeedData("data-local.sql") {
    @Test
    fun `inviting a new LA admin ends with a confirmation page`(page: Page) {
        whenever(absoluteUrlProvider.buildInvitationUri(anyString()))
            .thenReturn(URI("www.prsd.gov.uk/register-la-user/test-token"))

        val invitePage = navigator.goToInviteLaAdmin()
        invitePage.fillInFormAndSubmit("ISLE OF ", "ISLE OF MAN", "admin@example.com", "admin@example.com")

        // Confirmation page
        val confirmationPage = assertPageIs(page, InviteLaAdminConfirmationPage::class)
        assertThat(confirmationPage.confirmationBanner).containsText("admin@example.com")
        assertThat(confirmationPage.confirmationBanner).containsText("ISLE OF MAN")

        // Invite another user
        confirmationPage.inviteAnotherUserButton.clickAndWait()
        assertPageIs(page, InviteLaAdminPage::class)

        // TODO PRSD-672 - check the Return to Dashboard button
    }
}
