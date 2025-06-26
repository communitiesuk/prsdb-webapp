package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.FILE_TOO_LARGE_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.CustomErrorController
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

    @Test
    fun `file too large page renders when file too large controller path called`(page: Page) {
        navigator.navigate("/error/file-too-large")
        val errorPage = createValidPage(page, ErrorPage::class)
        BaseComponent.assertThat(errorPage.heading).containsText("The file you selected was too large")
    }

    @Nested
    inner class LandlordCustomErrorPageTests {
        @Test
        fun `500 page renders when error controller path called`(page: Page) {
            navigator.navigate(CustomErrorController.LANDLORD_ERROR_ROUTE)
            val errorPage = createValidPage(page, ErrorPage::class)
            BaseComponent.assertThat(errorPage.heading).containsText("Sorry, there is a problem with the service")
        }

        @Test
        fun `file too large page renders when file too large controller path called`(page: Page) {
            navigator.navigate("${CustomErrorController.LANDLORD_ERROR_ROUTE}/$FILE_TOO_LARGE_PATH_SEGMENT")
            val errorPage = createValidPage(page, ErrorPage::class)
            BaseComponent.assertThat(errorPage.heading).containsText("The file you selected was too large")
        }
    }

    @Nested
    inner class LocalAuthorityCustomErrorPageTests {
        @Test
        fun `500 page renders when error controller path called`(page: Page) {
            navigator.navigate(CustomErrorController.LOCAL_AUTHORITY_ERROR_ROUTE)
            val errorPage = createValidPage(page, ErrorPage::class)
            BaseComponent.assertThat(errorPage.heading).containsText("Sorry, there is a problem with the service")
        }

        @Test
        fun `file too large page renders when file too large controller path called`(page: Page) {
            navigator.navigate("${CustomErrorController.LOCAL_AUTHORITY_ERROR_ROUTE}/$FILE_TOO_LARGE_PATH_SEGMENT")
            val errorPage = createValidPage(page, ErrorPage::class)
            BaseComponent.assertThat(errorPage.heading).containsText("The file you selected was too large")
        }
    }

    @Nested
    inner class PublicCustomErrorControllerPageTests {
        @Test
        fun `500 page renders when error controller path called`(page: Page) {
            navigator.navigate(CustomErrorController.PUBLIC_ERROR_ROUTE)
            val errorPage = createValidPage(page, ErrorPage::class)
            BaseComponent.assertThat(errorPage.heading).containsText("Sorry, there is a problem with the service")
        }

        @Test
        fun `file too large page renders when file too large controller path called`(page: Page) {
            navigator.navigate("${CustomErrorController.PUBLIC_ERROR_ROUTE}/$FILE_TOO_LARGE_PATH_SEGMENT")
            val errorPage = createValidPage(page, ErrorPage::class)
            BaseComponent.assertThat(errorPage.heading).containsText("The file you selected was too large")
        }
    }
}
