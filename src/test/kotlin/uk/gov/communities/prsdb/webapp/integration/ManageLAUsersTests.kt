package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import kotlin.test.Test

// @ActiveProfiles("local-no-auth")
class ManageLAUsersTests : IntegrationTest() {
    val localAuthorityId = 1

    @Test
    fun `manageLAUsers page renders`(page: Page) {
        page.navigate("http://localhost:$port/local-authority/$localAuthorityId/manage-users")
        assertThat(page.locator("h1")).containsText("Manage Betelgeuse's users")
    }

    @Test
    fun `table of users renders`(page: Page) {
        page.navigate("http://localhost:$port/local-authority/$localAuthorityId/manage-users")
        assertThat(page.locator("table")).containsText("Username")
        assertThat(page.locator("table")).containsText("Access level")
        assertThat(page.locator("table")).containsText("Account status")
        assertThat(page.locator("table")).containsText("Arthur Dent")
        assertThat(page.locator("table")).containsText("Basic")
        assertThat(page.locator("table")).containsText("ACTIVE")
        assertThat(page.locator("table")).containsText("Admin")
    }

    @Test
    fun `buttons render`(page: Page) {
        page.navigate("http://localhost:$port/local-authority/$localAuthorityId/manage-users")
        assertThat(page.locator("button").getByText("Invite another user")).isVisible()
        assertThat(page.locator("button").getByText("Return to dashboard")).isVisible()
    }

    @Test
    fun `pagination component renders with more than 10 table entries`(page: Page) {
        page.navigate("http://localhost:$port/local-authority/$localAuthorityId/manage-users")
        assertThat(page.locator("nav.govuk-pagination")).isVisible()
        assertThat(page.locator("nav.govuk-pagination")).containsText("Next")
        assertThat(page.locator("li.govuk-pagination__item--current")).containsText("1")

        page.navigate("http://localhost:$port/local-authority/$localAuthorityId/manage-users?page=2")
        assertThat(page.locator("nav.govuk-pagination")).isVisible()
        assertThat(page.locator("nav.govuk-pagination")).containsText("Previous")
        assertThat(page.locator("li.govuk-pagination__item--current")).containsText("2")
    }
}
