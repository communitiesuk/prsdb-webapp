package uk.gov.communities.prsd.webapp.frontend

import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test
import org.openqa.selenium.By
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsd.webapp.controllers.SearchRegisterController

@WebMvcTest(SearchRegisterController::class)
class SearchRegisterControllerTests(
    @Autowired webContext: WebApplicationContext,
) : FrontendTest(webContext) {
    @Test
    fun layoutTest() {
        driver.get("http://localhost:8080/search")
        val header = driver.findElement(By.tagName("h1"))
        assertThat(header.text).isEqualTo("Search for Private Rented Sector information")
    }
}
