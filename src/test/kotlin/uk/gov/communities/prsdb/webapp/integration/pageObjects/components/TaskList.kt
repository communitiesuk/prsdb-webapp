package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class TaskList(
    locator: Locator,
) : BaseComponent(locator) {
    companion object {
        fun byIndex(
            parentLocator: Locator,
            index: Int,
        ) = TaskList(parentLocator.locator(".govuk-task-list").nth(index))

        fun byIndex(
            page: Page,
            index: Int,
        ) = byIndex(page.locator("html"), index)
    }

    fun clickTask(name: String) = getTask(name).click()

    fun getTaskStatus(name: String): String =
        getChildComponent(getTask(name), ".govuk-task-list__status")
            .textContent()

    private fun getTask(name: String) = getChildComponent("li", Locator.LocatorOptions().setHasText(name))
}
