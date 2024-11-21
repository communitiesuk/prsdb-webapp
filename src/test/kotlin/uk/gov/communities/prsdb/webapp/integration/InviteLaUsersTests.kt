package uk.gov.communities.prsdb.webapp.integration

import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql

@Sql("/data-local.sql")
class InviteLaUsersTests : IntegrationTest() {
    @Test
    fun `inviting a new LA user ends with a success page`() {
        val invitePage = navigator.goToInviteNewLaUser(1)
        invitePage.fillBothEmailFields("test@example.com")
        val successPage = invitePage.submit()
        successPage.confirmationBanner.assertHasMessage("You've sent test@example.com an invite to the database")
    }

    @Test
    fun `inviting a new LA user shows validation errors if the email addresses don't match`() {
        val invitePage = navigator.goToInviteNewLaUser(1)
        invitePage.fillEmail("test@example.com")
        invitePage.fillConfirmEmail("different@example.com")
        invitePage.submitUnsuccessfully()
        invitePage.assertConfirmEmailErrorContains("Both email address should match")
    }
}
