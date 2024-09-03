package uk.gov.communities.prsd.webapp.frontend

import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test
import org.openqa.selenium.By
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsd.webapp.controllers.CustomErrorController

@WebMvcTest(CustomErrorController::class)
class ErrorPageTests(
    @Autowired val webContext: WebApplicationContext,
) : FrontendTest(webContext) {
    @Test
    fun `500 page renders when error controller path called`() {
        driver.get("http://localhost:8080/error")
        val header = driver.findElement(By.tagName("h1"))
        assertThat(header.text).isEqualTo("Sorry, there is a problem with the service")
    }
}
