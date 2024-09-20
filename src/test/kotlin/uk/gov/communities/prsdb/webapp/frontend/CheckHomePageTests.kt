package uk.gov.communities.prsdb.webapp.frontend

import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test
import org.openqa.selenium.By
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.controllers.CheckHomeController

// TODO: Replace with Playwright tests
@WebMvcTest(CheckHomeController::class)
class CheckHomePageTests(
    @Autowired val webContext: WebApplicationContext,
) : FrontendTest(webContext) {
    @Test
    fun `check a home for rent page renders`() {
        driver.get("http://localhost:8080/check")
        val header = driver.findElement(By.tagName("h1"))
        assertThat(header.text).isEqualTo("Check a home to rent")
    }
}
