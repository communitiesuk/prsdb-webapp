package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.InviteNewLaUserSuccessPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.LocalAuthorityDashboardPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs

@Sql("/data-local.sql")
class InviteLaUsersTests : IntegrationTest() {
    @Test
    fun `inviting a new LA user ends with a success page`(page: Page) {
        val invitePage = navigator.goToInviteNewLaUser(1)
        invitePage.submitMatchingEmail("test@example.com")
        val successPage = assertPageIs(page, InviteNewLaUserSuccessPage::class)
        assertThat(successPage.confirmationBanner).containsText("You've sent test@example.com an invite to the database")

        // Go to dashboard button
        successPage.returnToDashboardButton.clickAndWait()
        assertPageIs(page, LocalAuthorityDashboardPage::class)
    }

    @Test
    fun `inviting a new LA user shows validation errors if the email addresses don't match`() {
        val invitePage = navigator.goToInviteNewLaUser(1)
        invitePage.submitMismatchedEmails("test@example.com", "different@example.com")
        assertThat(invitePage.form.getErrorMessage()).containsText("Both email address should match")
    }
}
