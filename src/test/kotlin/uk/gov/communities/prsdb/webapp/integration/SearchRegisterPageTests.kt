package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test

// TODO: Figure out how to mock roles with Playwright
class SearchRegisterPageTests : IntegrationTest() {
    @Test
    fun `search for private rented sector information page renders`(page: Page) {
        page.navigate("http://localhost:$port/search")
        assertThat(page).hasURL("http://localhost:$port/login")
    }
}
