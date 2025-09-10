package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.InviteNewLaUserSuccessPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LocalAuthorityDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import java.net.URI

class InviteLaUsersTests : IntegrationTestWithMutableData("data-local.sql") {
    @Test
    fun `inviting a new LA user ends with a success page with a button linking to the dashboard`(page: Page) {
        whenever(absoluteUrlProvider.buildInvitationUri(anyString()))
            .thenReturn(URI("www.prsd.gov.uk/register-la-user/test-token"))
        whenever(absoluteUrlProvider.buildLocalAuthorityDashboardUri()).thenReturn(URI("https:gov.uk"))

        val invitePage = navigator.goToInviteNewLaUser(2)
        invitePage.submitMatchingEmail("test@example.com")
        val successPage = assertPageIs(page, InviteNewLaUserSuccessPage::class)
        assertThat(successPage.confirmationBanner).containsText("You’ve sent test@example.com an invite to the database")

        // Go to dashboard button
        successPage.returnToDashboardButton.clickAndWait()
        assertPageIs(page, LocalAuthorityDashboardPage::class)
    }
}
