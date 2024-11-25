package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test

class InviteLaUsersTests : IntegrationTest() {
    @Test
    fun `inviting a new LA user ends with a success page`() {
        val invitePage = navigator.goToInviteNewLaUser(1)
        invitePage.fillBothEmailFields("test@example.com")
        val successPage = invitePage.submitFormAndAssertNextPage()
        assertThat(successPage.confirmationBanner).containsText("You've sent test@example.com an invite to the database")
    }

    @Test
    fun `inviting a new LA user shows validation errors if the email addresses don't match`() {
        val invitePage = navigator.goToInviteNewLaUser(1)
        invitePage.emailInput.fill("test@example.com")
        invitePage.confirmEmailInput.fill("different@example.com")
        invitePage.submitInvalidForm()
        assertThat(invitePage.form.getErrorMessage()).containsText("Both email address should match")
    }
}
