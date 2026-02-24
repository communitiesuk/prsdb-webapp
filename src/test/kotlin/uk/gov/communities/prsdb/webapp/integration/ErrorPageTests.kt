package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.ERROR_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.CustomErrorController.Companion.CYA_ERROR_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.CustomErrorController.Companion.FILE_TOO_LARGE_ERROR_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.ErrorPage
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage.Companion.createValidPage

class ErrorPageTests : IntegrationTestWithImmutableData("data-local.sql") {
    @Test
    fun `500 page renders when error controller path called`(page: Page) {
        navigator.navigate("/$ERROR_PATH_SEGMENT")
        val errorPage = createValidPage(page, ErrorPage::class)
        BaseComponent.assertThat(errorPage.heading).containsText("Sorry, there is a problem with the service")
    }

    @Test
    fun `file too large page renders when file too large controller path called`(page: Page) {
        navigator.navigate(FILE_TOO_LARGE_ERROR_ROUTE)
        val errorPage = createValidPage(page, ErrorPage::class)
        BaseComponent.assertThat(errorPage.heading).containsText("The file you selected was too large")
    }

    @Test
    fun `500 error page renders when CYA error controller path called`(page: Page) {
        navigator.navigate(CYA_ERROR_ROUTE)
        val errorPage = createValidPage(page, ErrorPage::class)
        BaseComponent.assertThat(errorPage.heading).containsText("Sorry, there is a problem with the service")
    }
}
