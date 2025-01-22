package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class Tabs(
    page: Page,
    val locator: Locator = page.locator(".govuk-tabs"),
) : BaseComponent(locator) {
    val tabsList = locator.locator(".govuk-tabs__list").locator(".govuk-tabs__list-item").all()

    val tabPanels = locator.locator(".govuk-tabs__panel").all()

    var hiddenPanels = tabPanels.filter { it.isHidden }

    var activePanel = tabPanels.find { it.isVisible }

    fun goToTab(tabName: String) {
        tabsList.single { it.textContent().trim() == tabName.trim() }.click()
        hiddenPanels = tabPanels.filter { it.isHidden }
        activePanel = tabPanels.find { it.isVisible }
    }
}
