package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class TaskList(
    private val page: Page,
    val locator: Locator = page.locator(".govuk-task-list"),
) : BaseComponent(locator) {
    fun clickTask(name: String) = getChildComponent("li", Locator.LocatorOptions().setHasText(name)).click()

    fun clickFirstActionableTask() =
        locator
            .locator("li")
            .getByText("In progress")
            .or(
                locator
                    .locator("li")
                    .getByText("Not yet started"),
            ).nth(0)
            .click()

    fun getTaskStatus(name: String): String =
        getChildComponent("li", Locator.LocatorOptions().setHasText(name)).locator(".govuk-task-list__status").textContent()
}
