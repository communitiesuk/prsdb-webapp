package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import com.microsoft.playwright.options.AriaRole
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.URI

class RegisterLandlordPageTests : IntegrationTest() {
    @Test
    fun `registerAsALandlord page renders`(page: Page) {
        page.navigate("http://localhost:$port/register-as-a-landlord")
        assertThat(page.locator("h1")).containsText("Private Rented Sector Database") // h1 instead
    }

    @Test
    fun `the 'Start Now' button directs a user to the registration page`(page: Page) {
        page.navigate("http://localhost:$port/register-as-a-landlord")
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Start Now")).click()
        assertEquals("/registration", URI(page.url()).path)
    }
}
