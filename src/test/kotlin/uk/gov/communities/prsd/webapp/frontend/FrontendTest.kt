package uk.gov.communities.prsd.webapp.frontend

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.openqa.selenium.WebDriver
import org.springframework.test.web.servlet.htmlunit.webdriver.MockMvcHtmlUnitDriverBuilder
import org.springframework.web.context.WebApplicationContext

abstract class FrontendTest(
    val context: WebApplicationContext,
) {
    protected lateinit var driver: WebDriver

    @BeforeEach
    fun setup() {
        driver =
            MockMvcHtmlUnitDriverBuilder
                .webAppContextSetup(context)
                .build()
    }

    @AfterEach
    fun destroy() {
        if (driver != null) {
            driver.close()
        }
    }
}
