package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.assertions.PlaywrightAssertions
import org.junit.jupiter.api.Test

class InviteLaUsersSinglePageTests : SinglePageTestWithSeedData("data-local.sql") {
    @Test
    fun `inviting a new LA user shows validation errors if the email addresses don't match`() {
        val invitePage = navigator.goToInviteNewLaUser(1)
        invitePage.submitMismatchedEmails("test@example.com", "different@example.com")
        PlaywrightAssertions.assertThat(invitePage.form.getErrorMessage()).containsText("Both email addresses should match")
    }
}
