package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLocalCouncilUsersPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs

class InviteLocalCouncilUsersSinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Test
    fun `inviting a new LocalCouncil user shows validation errors if the email addresses don't match`() {
        val invitePage = navigator.goToInviteNewLocalCouncilUser(1)
        invitePage.submitMismatchedEmails("test@example.com", "different@example.com")
        assertThat(invitePage.form.getErrorMessage()).containsText("Both email addresses should match")
    }

    @Test
    fun `the invite a new LocalCouncil user page back link returns to the manage users page`(page: Page) {
        val invitePage = navigator.goToInviteNewLocalCouncilUser(1)
        invitePage.backLink.clickAndWait()
        assertPageIs(page, ManageLocalCouncilUsersPage::class)
    }
}
