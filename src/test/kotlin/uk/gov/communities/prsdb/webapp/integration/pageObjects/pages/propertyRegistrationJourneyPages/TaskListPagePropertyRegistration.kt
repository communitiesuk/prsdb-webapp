package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.REGISTER_PROPERTY_JOURNEY_URL
import uk.gov.communities.prsdb.webapp.constants.TASK_LIST_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TaskList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class TaskListPagePropertyRegistration(
    page: Page,
) : BasePage(
        page,
        "/$REGISTER_PROPERTY_JOURNEY_URL/$TASK_LIST_PATH_SEGMENT",
    ) {
    private val registerTasks = TaskList.byIndex(page, 0)
    private val checkAndSubmitTasks = TaskList.byIndex(page, 1)

    fun clickRegisterTaskWithName(name: String) = registerTasks.getTask(name).clickAndWait()

    fun taskHasStatus(
        name: String,
        status: String,
    ): Boolean = registerTasks.getTask(name).statusText.contains(status)
}
