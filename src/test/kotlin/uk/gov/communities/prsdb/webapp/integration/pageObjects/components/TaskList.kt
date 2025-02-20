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

    fun getTask(name: String) = Task.byName(locator, name)

    class Task(
        override val locator: Locator,
    ) : BaseComponent(locator),
        ClickAndWaitable {
        companion object {
            fun byName(
                parentLocator: Locator,
                name: String,
            ) = Task(parentLocator.locator("li", Locator.LocatorOptions().setHasText(name)))
        }

        val statusText: String
            get() = locator.locator(".govuk-task-list__status").textContent()
    }
}
