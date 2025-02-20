package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

open class Tabs(
    parentLocator: Locator,
) : BaseComponent(parentLocator.locator(".govuk-tabs")) {
    constructor(page: Page) : this(page.locator("html"))

    val tabsList = locator.locator(".govuk-tabs__list >> .govuk-tabs__list-item")

    val tabPanels = locator.locator(".govuk-tabs__panel")

    val activePanel
        get() = tabPanels.all().find { it.isVisible }

    val activeTabPanelId
        get() = activePanel?.getAttribute("id")

    fun goToTab(tabName: String) {
        tabsList.all().single { it.textContent().trim() == tabName.trim() }.click()
    }
}
