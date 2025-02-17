package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TaskList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class TaskListPagePropertyRegistration(
    page: Page,
) : BasePage(
        page,
        "/$REGISTER_PROPERTY_JOURNEY_URL/task-list",
    ) {
    private val registerTasks = TaskList.byIndex(page, 0)
    private val checkAndSubmitTasks = TaskList.byIndex(page, 1)

    fun clickRegisterTaskWithName(name: String) = registerTasks.clickTask(name)

    fun taskHasStatus(
        name: String,
        status: String,
    ): Boolean = registerTasks.getTaskStatus(name).contains(status)
}
