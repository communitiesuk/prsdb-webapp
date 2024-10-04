package uk.gov.communities.prsdb.webapp.frontend

import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import java.time.Duration

@WebMvcTest(RegisterLandlordController::class)
class RegisterLandlordPageTests(
    @Autowired val webContext: WebApplicationContext,
) : FrontendTest(webContext) {
    @Test
    fun `registerAsALandlord page renders`() {
        driver.get("http://localhost:8080/register-as-a-landlord")

        val header = driver.findElement(By.tagName("h1"))
        assertThat(header.text).isEqualTo("Private Rented Sector Database")
    }

    @Test
    @WithMockUser(roles = ["LANDLORD"])
    fun `the 'Start Now' button directs an authorized user to the registration page`() {
        driver.get("http://localhost:8080/register-as-a-landlord")

        val startButton = driver.findElement(By.className("govuk-button--start"))
        startButton.click()

        val wait = WebDriverWait(driver, Duration.ofSeconds(5))
        wait.until(ExpectedConditions.urlToBe("http://localhost:8080/registration"))
    }
}
