package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.TASK_LIST_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TaskList
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class TaskListPagePropertyRegistration(
    page: Page,
) : BasePage(
        page,
        "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/$TASK_LIST_PATH_SEGMENT",
    ) {
    private val registerTasks = TaskList.byIndex(page, 0)
    private val checkAndSubmitTasks = TaskList.byIndex(page, 1)
    val backLink = BackLink.default(page)

    fun clickRegisterTaskWithName(name: String) = registerTasks.getTask(name).clickAndWait()

    fun taskHasStatus(
        name: String,
        status: String,
    ): Boolean = registerTasks.getTask(name).statusText.contains(status)
}
