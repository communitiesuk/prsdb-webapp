package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordUserRepository
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.changeOrgTeamMemberAccessLevelPages.AccessLevelPage

@WithOrgLandlordProfile
class ChangeOrgTeamMemberAccessLevelSinglePageTests :
    IntegrationTestWithMutableData("data-local.sql") {
    @Autowired
    private lateinit var organisationalLandlordUserRepository: OrganisationalLandlordUserRepository

    @Test
    fun `the page loads and shows the correct heading`(page: Page) {
        val memberId = organisationalLandlordUserRepository.findByBaseUser_Id("urn:fdc:gov.uk:2022:ORG02").single().id
        val accessLevelPage = navigator.goToChangeOrgTeamMemberAccessLevelPage(memberId)

        BaseComponent.assertThat(accessLevelPage.heading).containsText("Change access level")
    }

    @Test
    fun `submitting without selecting an access level shows a validation error`(page: Page) {
        val memberId = organisationalLandlordUserRepository.findByBaseUser_Id("urn:fdc:gov.uk:2022:ORG02").single().id
        val accessLevelPage = navigator.goToChangeOrgTeamMemberAccessLevelPage(memberId)

        accessLevelPage.submitWithoutSelectingAccessLevel()

        val errorPage = assertPageIs(page, AccessLevelPage::class)
        assertThat(errorPage.errorSummary.errorLinks("Select the access level")).hasCount(1)
    }
}
