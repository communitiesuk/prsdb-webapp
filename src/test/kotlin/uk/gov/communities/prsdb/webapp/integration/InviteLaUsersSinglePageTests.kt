package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test

class InviteLaUsersSinglePageTests : SinglePageTestWithSeedData("data-local.sql") {
    @Test
    fun `inviting a new LA user shows validation errors if the email addresses don't match`() {
        val invitePage = navigator.goToInviteNewLaUser(2)
        invitePage.submitMismatchedEmails("test@example.com", "different@example.com")
        assertThat(invitePage.form.getErrorMessage()).containsText("Both email addresses should match")
    }
}
