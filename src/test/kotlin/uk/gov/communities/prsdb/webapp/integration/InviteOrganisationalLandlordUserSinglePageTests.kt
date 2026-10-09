package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.assertThat
import java.util.regex.Pattern

@WithOrgLandlordProfile
class InviteOrganisationalLandlordUserSinglePageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Test
    fun `the invite page names the admin's organisation`() {
        // Act
        val invitePage = navigator.goToInviteOrganisationalLandlordUserPage()

        // Assert
        assertThat(invitePage.heading).hasText("Invite a team member")
        assertThat(invitePage.introduction).containsText("Local Organisation Landlord")
    }

    @Test
    fun `an email address error does not mark the access level as errored`() {
        // Arrange
        val invitePage = navigator.goToInviteOrganisationalLandlordUserPage()

        // Act
        invitePage.submitInvitation("not-an-email", OrganisationalLandlordUserRole.EDITOR)

        // Assert
        assertThat(invitePage.form.getErrorMessage("emailAddress")).isVisible()
        assertThat(invitePage.accessLevelFormGroup).not().hasClass(Pattern.compile(".*govuk-form-group--error.*"))
    }

    @Test
    fun `a missing access level marks the access level as errored`() {
        // Arrange
        val invitePage = navigator.goToInviteOrganisationalLandlordUserPage()

        // Act
        invitePage.emailInput.fill("new.member@example.com")
        invitePage.form.submit()

        // Assert
        assertThat(invitePage.form.getErrorMessage("role")).isVisible()
        assertThat(invitePage.accessLevelFormGroup).hasClass(Pattern.compile(".*govuk-form-group--error.*"))
    }
}
