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
    fun `the 'Start Now' button directs a user to the landlord registration email page`(page: Page) {
        page.navigate("http://localhost:$port/register-as-a-landlord")
        page.getByRole(AriaRole.BUTTON, Page.GetByRoleOptions().setName("Start Now")).click()
        assertEquals("/register-as-a-landlord/name", URI(page.url()).path)
    }
}
