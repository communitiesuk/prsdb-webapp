package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql

@Sql("/data-local.sql")
class SearchRegisterPageTests : IntegrationTest() {
    @Test
    fun `search for private rented sector information page renders`(page: Page) {
        page.navigate("http://localhost:$port/search")
        assertThat(page.locator("h1.govuk-heading-xl")).containsText("Search for Private Rented Sector information")
    }
}
