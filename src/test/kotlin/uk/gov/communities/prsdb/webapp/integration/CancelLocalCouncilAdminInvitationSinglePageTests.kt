package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ManageLocalCouncilAdminsPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.assertPageIs

class CancelLocalCouncilAdminInvitationSinglePageTests : IntegrationTestWithImmutableData("data-edit-la-admin-users-and-invitations.sql") {
    val invitationId = 1L

    @Test
    fun `back link goes to the edit admin page`(page: Page) {
        val cancelAdminInvitationPage = navigator.goToCancelAdminInvitePage(invitationId)
        cancelAdminInvitationPage.backLink.clickAndWait()
        assertPageIs(page, ManageLocalCouncilAdminsPage::class)
    }
}
