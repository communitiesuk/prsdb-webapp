package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import java.net.URI

@Sql("/data-local.sql")
class RegisterPropertyPageTests : IntegrationTest() {
    @Test
    fun `registerProperty page renders`(page: Page) {
        val registerPropertyStartPage = navigator.goToPropertyRegistrationStartPage()
        assertThat(registerPropertyStartPage.heading).containsText("Enter your property details")
    }

    // TODO: PRSD-490 - update where this link goes to when the address search page is added as the initial step
    @Test
    fun `the 'Start Now' button directs a user to the register property journey`(page: Page) {
        val registerPropertyStartPage = navigator.goToPropertyRegistrationStartPage()
        registerPropertyStartPage.startButton.click()
        assertEquals("/register-property/placeholder", URI(page.url()).path)
    }
}
