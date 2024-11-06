package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages

import com.microsoft.playwright.Page

class Navigator(
    private val page: Page,
    private val port: Int,
) {
    fun goToInviteNewLaUser(authorityId: Int): InviteNewLaUserPage {
        navigate("local-authority/$authorityId/manage-users/invite-new-user")
        return BasePage.createValid<InviteNewLaUserPage>(page)
    }

    private fun navigate(path: String) {
        page.navigate("http://localhost:$port/$path")
    }
}
