package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql

@Sql("/data-local.sql")
class ErrorPageTests : IntegrationTest() {
    @Test
    fun `500 page renders when error controller path called`(page: Page) {
        page.navigate("http://localhost:$port/error")
        assertThat(page.getByRole(AriaRole.HEADING)).containsText("Sorry, there is a problem with the service")
    }
}
