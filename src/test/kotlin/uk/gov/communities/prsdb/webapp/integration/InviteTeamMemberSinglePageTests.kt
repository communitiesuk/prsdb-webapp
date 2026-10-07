package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.inviteTeamMemberJourneyPages.InviteTeamMemberFormPage

@WithOrgLandlordProfile
class InviteTeamMemberSinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Test
    fun `the invite page names the admin's organisation`() {
        // Act
        val invitePage = navigator.goToInviteTeamMemberPage()

        // Assert
        assertThat(invitePage.heading).hasText("Invite a team member")
        assertThat(invitePage.introduction).containsText("Local Organisation Landlord")
    }

    @Test
    fun `submitting without an email address or access level shows an error against each field`(page: Page) {
        // Arrange
        val invitePage = navigator.goToInviteTeamMemberPage()

        // Act
        invitePage.form.submit()

        // Assert
        assertPageIs(page, InviteTeamMemberFormPage::class)
        assertThat(invitePage.form.getErrorMessage("emailAddress")).containsText("Enter an email address")
        assertThat(invitePage.form.getErrorMessage("role")).containsText("Select their access level")
    }

    @Test
    fun `submitting an invalid email address only shows the email error`(page: Page) {
        // Arrange
        val invitePage = navigator.goToInviteTeamMemberPage()

        // Act
        invitePage.submitInvitation("not-an-email", OrganisationalLandlordUserRole.EDITOR)

        // Assert
        assertPageIs(page, InviteTeamMemberFormPage::class)
        assertThat(invitePage.form.getErrorMessage("emailAddress"))
            .containsText("Enter an email address in the correct format, like name@example.com")
        assertThat(invitePage.form.getErrorMessage("role")).hasCount(0)
        assertThat(invitePage.errorFormGroups).hasCount(1)
        assertThat(invitePage.accessLevelFieldset).not().hasAttribute("aria-describedby", "role-error")
    }
}
