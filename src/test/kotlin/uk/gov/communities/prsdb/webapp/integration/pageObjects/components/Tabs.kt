package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class Tabs(
    page: Page,
    numberOfTabs: Int,
    val locator: Locator = page.locator(".govuk-tabs"),
) : BaseComponent(locator) {
    val tabsList = getChildrenComponents(".govuk-tabs__list >> .govuk-tabs__list-item", numberOfTabs)

    val tabPanels = getChildrenComponents(".govuk-tabs__panel", numberOfTabs)

    var hiddenPanels = tabPanels.filter { it.isHidden }

    var activePanel = tabPanels.find { it.isVisible }

    fun goToTab(tabName: String) {
        tabsList.single { it.textContent().trim() == tabName.trim() }.click()
        hiddenPanels = tabPanels.filter { it.isHidden }
        activePanel = tabPanels.find { it.isVisible }
    }
}
