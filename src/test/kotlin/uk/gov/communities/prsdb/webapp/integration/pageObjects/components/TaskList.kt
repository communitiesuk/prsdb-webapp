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

    fun getTaskByIndex(index: Int) = Task(locator.locator("li").nth(index))

    fun taskNames(): List<String> =
        (0 until locator.locator("li").count()).map { index ->
            getTaskByIndex(index).nameText.trim()
        }

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

        val nameText: String
            get() =
                if (hasLink) {
                    locator.locator("a.govuk-task-list__link").textContent()
                } else {
                    locator.locator(".govuk-task-list__name-and-hint > div").first().textContent()
                }

        val statusText: String
            get() = locator.locator(".govuk-task-list__status").textContent()

        val hintText: String
            get() = locator.locator(".govuk-task-list__hint").textContent()

        val hasLink: Boolean
            get() = locator.locator("a.govuk-task-list__link").count() > 0
    }
}
