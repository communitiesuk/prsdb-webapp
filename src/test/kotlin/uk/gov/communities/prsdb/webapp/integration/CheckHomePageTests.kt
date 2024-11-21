package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql

@Sql("/data-local.sql")
class CheckHomePageTests : IntegrationTest() {
    @Test
    fun `check a home for rent page renders`(page: Page) {
        page.navigate("http://localhost:$port/check")
        assertThat(page.getByRole(AriaRole.HEADING)).containsText("Check a home to rent")
    }
}
