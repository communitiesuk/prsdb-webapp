package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BaseComponent.Companion.getComponent
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class ErrorPage(
    page: Page,
) : BasePage(page) {
    val heading = getComponent(page, "main h1")
}
