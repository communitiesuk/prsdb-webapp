package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ErrorPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.createValidPage

class ErrorPageTests : SinglePageTestWithSeedData("data-local.sql") {
    @Test
    fun `500 page renders when error controller path called`(page: Page) {
        navigator.navigate("/error")
        val errorPage = createValidPage(page, ErrorPage::class)
        BaseComponent.assertThat(errorPage.heading).containsText("Sorry, there is a problem with the service")
    }
}
