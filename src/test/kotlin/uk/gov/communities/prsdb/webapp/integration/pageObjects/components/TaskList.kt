package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class TaskList(
    private val page: Page,
    locator: Locator = page.locator(".govuk-task-list"),
) : BaseComponent(locator) {
    fun clickTask(name: String) = getTask(name).click()

    fun getTaskStatus(name: String): String =
        Companion
            .getChildComponent(getTask(name), ".govuk-task-list__status")
            .textContent()

    private fun getTask(name: String) = getChildComponent("li", Locator.LocatorOptions().setHasText(name))
}
