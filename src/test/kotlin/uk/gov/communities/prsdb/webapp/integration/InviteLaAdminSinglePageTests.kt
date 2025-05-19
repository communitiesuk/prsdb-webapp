package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test

class InviteLaAdminSinglePageTests : SinglePageTestWithSeedData("data-local.sql") {
    @Test
    fun `inviting a new LA admin shows validation errors if the email is invalid or the email addresses don't match`(page: Page) {
        val invitePage = navigator.goToInviteLaAdmin()
        invitePage.fillInFormAndSubmit("ISLE OF", "ISLE OF MAN", "not-an-email", "different@example.com")
        assertThat(invitePage.form.getErrorMessage("email")).containsText("Enter an email address in the correct format")
        assertThat(invitePage.form.getErrorMessage("confirmEmail")).containsText("Both email addresses should match")
    }

    @Test
    fun `inviting a new LA admin shows validation errors if any of the fields are empty`(page: Page) {
        val invitePage = navigator.goToInviteLaAdmin()
        invitePage.form.submit()
        assertThat(invitePage.form.getErrorMessage("localAuthorityId")).containsText("Select a local authority to continue")
        assertThat(invitePage.form.getErrorMessage("email")).containsText("You must enter an email address")
        assertThat(invitePage.form.getErrorMessage("confirmEmail")).containsText("You must enter and confirm their email address")
    }
}
