package uk.gov.communities.prsdb.webapp.frontend

import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test
import org.openqa.selenium.By
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.controllers.SearchRegisterController

// TODO: Replace with Playwright tests
@WebMvcTest(SearchRegisterController::class)
class SearchRegisterPageTests(
    @Autowired webContext: WebApplicationContext,
) : FrontendTest(webContext) {
    @Test
    fun `search for private rented sector information page renders`() {
        driver.get("http://localhost:8080/search")
        val header = driver.findElement(By.tagName("h1"))
        assertThat(header.text).isEqualTo("Search for Private Rented Sector information")
    }
}
