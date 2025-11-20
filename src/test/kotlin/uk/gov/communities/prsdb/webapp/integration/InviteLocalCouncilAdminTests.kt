package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.InviteLaAdminPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.InviteLocalCouncilAdminConfirmationPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import java.net.URI

class InviteLocalCouncilAdminTests : IntegrationTestWithMutableData("data-local.sql") {
    @Test
    fun `inviting a new Local Council admin ends with a confirmation page`(page: Page) {
        whenever(absoluteUrlProvider.buildInvitationUri(anyString()))
            .thenReturn(URI("www.prsd.gov.uk/register-local-council-user/test-token"))

        val invitePage = navigator.goToInviteLocalCouncilAdmin()
        invitePage.fillInFormAndSubmit("BATH AND ", "BATH AND NORTH EAST SOMERSET COUNCIL", "admin@example.com", "admin@example.com")

        // Confirmation page
        val confirmationPage = assertPageIs(page, InviteLocalCouncilAdminConfirmationPage::class)
        assertThat(confirmationPage.confirmationBanner).containsText("admin@example.com")
        assertThat(confirmationPage.confirmationBanner).containsText("BATH AND NORTH EAST SOMERSET COUNCIL")

        // Invite another user
        confirmationPage.inviteAnotherUserButton.clickAndWait()
        assertPageIs(page, InviteLaAdminPage::class)

        // TODO PRSD-672 - check the Return to Dashboard button
    }
}
