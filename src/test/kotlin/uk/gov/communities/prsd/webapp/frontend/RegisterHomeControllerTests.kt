package uk.gov.communities.prsd.webapp.frontend

import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test
import org.openqa.selenium.By
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsd.webapp.controllers.RegisterHomeController

@WebMvcTest(RegisterHomeController::class)
class RegisterHomeControllerTests(
    @Autowired webContext: WebApplicationContext,
) : FrontendTest(webContext) {
    @Test
    fun layoutTest() {
        driver.get("http://localhost:8080/registration")
        val header = driver.findElement(By.tagName("h1"))
        assertThat(header.text).isEqualTo("Register a home to rent")
    }
}
