package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat as assertComponent

class InviteLocalCouncilUsersSinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Test
    fun `inviting a new LocalCouncil user shows validation errors if the email addresses don't match`() {
        val invitePage = navigator.goToInviteNewLocalCouncilUser(1)
        invitePage.submitMismatchedEmails("test@example.com", "different@example.com")
        assertThat(invitePage.form.getErrorMessage()).containsText("Both email addresses should match")
    }

    @Test
    fun `the invite a new LocalCouncil user page has a back link`() {
        val invitePage = navigator.goToInviteNewLocalCouncilUser(1)
        assertComponent(invitePage.backLink).isVisible()
    }
}
