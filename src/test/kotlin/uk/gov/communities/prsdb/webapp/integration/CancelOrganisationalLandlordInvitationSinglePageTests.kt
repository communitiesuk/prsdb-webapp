package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.communities.prsdb.webapp.controllers.CancelOrganisationalLandlordInvitationController
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ErrorPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs
import java.util.UUID

@WithOrgLandlordProfile
class CancelOrganisationalLandlordInvitationSinglePageTests :
    IntegrationTestWithImmutableData("data-local.sql") {
    @Autowired
    private lateinit var invitationRepository: OrganisationalLandlordInvitationRepository

    @Test
    fun `an expired invitation cannot be cancelled`(page: Page) {
        // Arrange
        val expiredInvitationId = invitationRepository.findByToken(UUID.fromString("1234abcd-5678-abcd-1234-567abcd2222d"))!!.id

        // Act
        navigator.navigate(CancelOrganisationalLandlordInvitationController.getCancelInvitationPath(expiredInvitationId))

        // Assert
        val errorPage = assertPageIs(page, ErrorPage::class)
        BaseComponent.assertThat(errorPage.heading).containsText("Page not found")
    }
}
