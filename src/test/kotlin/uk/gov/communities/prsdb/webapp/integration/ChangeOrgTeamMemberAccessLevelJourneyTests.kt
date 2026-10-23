package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordUserRepository
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ErrorPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.TeamMembersPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.changeOrgTeamMemberAccessLevelPages.AccessLevelPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.changeOrgTeamMemberAccessLevelPages.RemoveOrgUserConfirmationPage
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordUserService
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private const val ADMIN_USER_ID = "urn:fdc:gov.uk:2022:ORG02"
private const val EDITOR_USER_ID = "urn:fdc:gov.uk:2022:ORG03"

@WithOrgLandlordProfile
class ChangeOrgTeamMemberAccessLevelJourneyTests :
    IntegrationTestWithMutableData("data-local.sql") {
    @Autowired
    private lateinit var organisationalLandlordUserRepository: OrganisationalLandlordUserRepository

    @Autowired
    private lateinit var organisationalLandlordUserService: OrganisationalLandlordUserService

    @Test
    fun `a signed in organisation admin sees the forbidden page after their membership is removed`(page: Page) {
        // Arrange
        val dashboard = navigator.goToLandlordDashboard()
        BaseComponent.assertThat(dashboard.dashboardBannerHeading).hasText("Local Organisation Landlord")
        val member = organisationalLandlordUserRepository.findByBaseUser_Id("urn:fdc:gov.uk:2022:ORG01").single()
        assertEquals(OrganisationalLandlordUserRole.ADMIN, member.role)

        // Act
        organisationalLandlordUserService.removeOrganisationalLandlordUser(member.id)
        val response = assertNotNull(page.reload())

        // Assert
        assertEquals(403, response.status())
        val errorPage = assertPageIs(page, ErrorPage::class)
        BaseComponent.assertThat(errorPage.heading).hasText("You do not have permission to access this page")
    }

    @Test
    fun `an admin can change another administrator's access level to editor`(page: Page) {
        // Arrange
        val teamMembersPage = navigator.goToTeamMembers()
        teamMembersPage.administratorsSummaryList.getRowByIndex(0).clickFirstActionLinkAndWait()

        // Act
        val accessLevelPage = assertPageIs(page, AccessLevelPage::class)
        assertThat(accessLevelPage.currentLevelHint).hasText("Beth Admin is currently an administrator.")
        accessLevelPage.submitAccessLevel(OrganisationalLandlordUserRole.EDITOR.name)

        // Assert
        val teamMembersPageAfter = assertPageIs(page, TeamMembersPage::class)
        BaseComponent.assertThat(teamMembersPageAfter.successBanner.content.heading).hasText("Access level changed")
        BaseComponent.assertThat(teamMembersPageAfter.successBanner.content).containsText("Beth Admin is now an editor.")
        val updatedMember = organisationalLandlordUserRepository.findByBaseUser_Id(ADMIN_USER_ID).single()
        assertEquals(OrganisationalLandlordUserRole.EDITOR, updatedMember.role)
    }

    @Test
    fun `an admin can change an editor's access level to administrator`(page: Page) {
        // Arrange - Carl Editor (ORG03) is the only editor
        val teamMembersPage = navigator.goToTeamMembers()
        teamMembersPage.tabs.goToEditors(editorCount = 1)
        teamMembersPage.editorsSummaryList.getRowByIndex(0).clickFirstActionLinkAndWait()

        // Act
        val accessLevelPage = assertPageIs(page, AccessLevelPage::class)
        assertThat(accessLevelPage.currentLevelHint).hasText("Carl Editor is currently an editor.")
        accessLevelPage.submitAccessLevel(OrganisationalLandlordUserRole.ADMIN.name)

        // Assert
        val teamMembersPageAfter = assertPageIs(page, TeamMembersPage::class)
        BaseComponent.assertThat(teamMembersPageAfter.successBanner.content.heading).hasText("Access level changed")
        BaseComponent.assertThat(teamMembersPageAfter.successBanner.content).containsText("Carl Editor is now an administrator.")
        val updatedMember = organisationalLandlordUserRepository.findByBaseUser_Id(EDITOR_USER_ID).single()
        assertEquals(OrganisationalLandlordUserRole.ADMIN, updatedMember.role)
    }

    @Test
    fun `an admin can confirm removal of another organisation member`(page: Page) {
        val teamMembersPage = navigator.goToTeamMembers()
        teamMembersPage.administratorsSummaryList.getRowByIndex(0).clickFirstActionLinkAndWait()

        val accessLevelPage = assertPageIs(page, AccessLevelPage::class)
        accessLevelPage.removeAccessLink.clickAndWait()

        val confirmationPage = assertPageIs(page, RemoveOrgUserConfirmationPage::class)
        assertThat(confirmationPage.memberNameParagraph)
            .hasText("Beth Admin will no longer be able to access your organisation on the service.")
        confirmationPage.form.submit()

        val teamMembersPageAfter = assertPageIs(page, TeamMembersPage::class)
        BaseComponent.assertThat(teamMembersPageAfter.successBanner.content.heading).hasText("Access removed")
        BaseComponent.assertThat(teamMembersPageAfter.successBanner.content).containsText("Beth Admin")
        assertTrue(organisationalLandlordUserRepository.findByBaseUser_Id(ADMIN_USER_ID).isEmpty())
    }

    @Test
    fun `cancelling on the remove access confirmation page returns to the team members page without removing the member`(page: Page) {
        val teamMembersPage = navigator.goToTeamMembers()
        teamMembersPage.administratorsSummaryList.getRowByIndex(0).clickFirstActionLinkAndWait()

        val accessLevelPage = assertPageIs(page, AccessLevelPage::class)
        accessLevelPage.removeAccessLink.clickAndWait()

        val confirmationPage = assertPageIs(page, RemoveOrgUserConfirmationPage::class)
        confirmationPage.cancelLink.clickAndWait()

        assertPageIs(page, TeamMembersPage::class)
        assertTrue(organisationalLandlordUserRepository.findByBaseUser_Id(ADMIN_USER_ID).isNotEmpty())
    }
}
