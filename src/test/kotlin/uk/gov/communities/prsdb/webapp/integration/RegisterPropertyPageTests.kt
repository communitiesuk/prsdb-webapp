package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.URI

class RegisterPropertyPageTests : IntegrationTest() {
    @Test
    fun `registerProperty page renders`(page: Page) {
        page.navigate("http://localhost:$port/register-property")
        assertThat(page.locator("h1")).containsText("Enter your property details")
    }

    // TODO: PRSD-490 - update where this link goes to when the address search page is added as the initial step
    @Test
    fun `the 'Start Now' button directs a user to the register property journey`(page: Page) {
        page.navigate("http://localhost:$port/register-property")
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Start Now")).click()
        assertEquals("/register-property/placeholder", URI(page.url()).path)
    }
}
