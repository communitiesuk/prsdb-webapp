package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole
import org.junit.jupiter.api.Test

class RegisterHomePageTests : IntegrationTest() {
    @Test
    fun `register a home to rent page renders`(page: Page) {
        page.navigate("http://localhost:$port/registration")
        assertThat(page.getByRole(AriaRole.HEADING)).containsText("Register a home to rent")
    }
}
