package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.EditLaUserPage
import kotlin.test.assertEquals

class EditLAUserTests : IntegrationTest() {
    @Test
    fun `a user's access level can be updated`() {
        var editUserPage = navigator.goToEditLaUser(1, 3) // Arthur Dent, basic user
        assertThat(editUserPage.userName).containsText("Arthur Dent")
        assertThat(editUserPage.email).containsText("Arthur Dent") // TODO: fix when LA users have email addresses
        assertEquals(EditLaUserPage.AccessLevelSelection.BASIC, editUserPage.accessLevel())

        editUserPage.selectAccessLevel(EditLaUserPage.AccessLevelSelection.ADMIN)
        editUserPage.submit()

        editUserPage = navigator.goToEditLaUser(1, 3)
        assertEquals(EditLaUserPage.AccessLevelSelection.ADMIN, editUserPage.accessLevel())
    }
}
