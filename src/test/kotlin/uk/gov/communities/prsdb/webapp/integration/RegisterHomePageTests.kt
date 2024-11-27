package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql

@Sql("/data-local.sql")
class RegisterHomePageTests : IntegrationTest() {
    @Test
    fun `register a home to rent page renders`(page: Page) {
        page.navigate("http://localhost:$port/registration")
        assertThat(page.locator("h1.govuk-heading-xl")).containsText("Register a home to rent")
    }
}
