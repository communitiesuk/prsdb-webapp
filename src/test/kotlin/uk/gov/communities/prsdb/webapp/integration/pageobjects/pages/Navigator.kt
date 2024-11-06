package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages

import com.microsoft.playwright.Page

class Navigator(
    private val page: Page,
    private val port: Int,
) {
    fun goToInviteNewLaUser(authorityId: Int): InviteNewLaUserPage {
        navigate("local-authority/$authorityId/invite-new-user")
        return BasePage.createValid(page, InviteNewLaUserPage::class)
    }

    fun goToManageLaUsers(authorityId: Int): ManageLaUsersPage {
        navigate("local-authority/$authorityId/manage-users")
        return BasePage.createValid(page, ManageLaUsersPage::class)
    }

    private fun navigate(path: String) {
        page.navigate("http://localhost:$port/$path")
    }
}
