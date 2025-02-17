package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.InviteNewLaUserSuccessPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs

@Sql("/data-local.sql")
class InviteLaUsersTests : IntegrationTest() {
    @Test
    fun `inviting a new LA user ends with a success page`(page: Page) {
        val invitePage = navigator.goToInviteNewLaUser(1)
        invitePage.fillBothEmailFields("test@example.com")
        invitePage.form.submit()
        val successPage = assertPageIs(page, InviteNewLaUserSuccessPage::class)
        assertThat(successPage.confirmationBanner).containsText("You've sent test@example.com an invite to the database")
    }

    @Test
    fun `inviting a new LA user shows validation errors if the email addresses don't match`() {
        val invitePage = navigator.goToInviteNewLaUser(1)
        invitePage.emailInput.fill("test@example.com")
        invitePage.confirmEmailInput.fill("different@example.com")
        invitePage.form.submit()
        assertThat(invitePage.form.getErrorMessage()).containsText("Both email address should match")
    }
}
