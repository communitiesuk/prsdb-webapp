package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ErrorPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.createValidPage

class ErrorPageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Test
    fun `500 page renders when error controller path called`(page: Page) {
        navigator.navigate("/error")
        val errorPage = createValidPage(page, ErrorPage::class)
        BaseComponent.assertThat(errorPage.heading).containsText("Sorry, there is a problem with the service")
    }

    @Test
    fun `file too large page renders when file too large controller path called`(page: Page) {
        navigator.navigate("/error/file-too-large")
        val errorPage = createValidPage(page, ErrorPage::class)
        BaseComponent.assertThat(errorPage.heading).containsText("The file you selected was too large")
    }
}
