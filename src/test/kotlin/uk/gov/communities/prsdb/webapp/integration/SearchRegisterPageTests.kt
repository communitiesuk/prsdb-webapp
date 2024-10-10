package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole
import org.junit.jupiter.api.Test

class SearchRegisterPageTests : IntegrationTest() {
    @Test
    fun `search for private rented sector information page renders`(page: Page) {
        page.navigate("http://localhost:$port/search")
        assertThat(page.getByRole(AriaRole.HEADING)).containsText("Search for Private Rented Sector information")
    }
}
