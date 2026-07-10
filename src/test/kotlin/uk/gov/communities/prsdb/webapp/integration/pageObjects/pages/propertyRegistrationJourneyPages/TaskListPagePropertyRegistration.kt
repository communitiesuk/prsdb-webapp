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
    val aboutYourPropertyTasks = TaskList.byIndex(page, 0)
    val rentedOutTasks = TaskList.byIndex(page, 1)
    val submitYourRegistrationTasks = TaskList.byIndex(page, 2)
    val backLink = BackLink.default(page)

    fun clickRegisterTaskWithName(name: String) = registerTasks.getTask(name).clickAndWait()

    fun clickCheckAndSubmitTaskWithName(name: String) = checkAndSubmitTasks.getTask(name).clickAndWait()

    fun clickAboutYourPropertyTaskWithName(name: String) = aboutYourPropertyTasks.getTask(name).clickAndWait()

    fun clickRentedOutTaskWithName(name: String) = rentedOutTasks.getTask(name).clickAndWait()

    fun clickSubmitYourRegistrationTaskWithName(name: String) = submitYourRegistrationTasks.getTask(name).clickAndWait()

    fun getRegisterTask(name: String): TaskList.Task = registerTasks.getTask(name)

    fun getAboutYourPropertyTask(name: String): TaskList.Task = aboutYourPropertyTasks.getTask(name)

    fun getRentedOutTask(name: String): TaskList.Task = rentedOutTasks.getTask(name)

    fun getSubmitYourRegistrationTask(name: String): TaskList.Task = submitYourRegistrationTasks.getTask(name)

    fun getSectionCount(): Int = page.locator(".govuk-task-list").count()

    fun getSectionHeading(index: Int) = page.locator("h2.govuk-heading-m").nth(index)

    fun getAboutYourPropertyTaskNames(): List<String> = aboutYourPropertyTasks.taskNames()

    fun getRentedOutTaskNames(): List<String> = rentedOutTasks.taskNames()

    fun getSubmitYourRegistrationTaskNames(): List<String> = submitYourRegistrationTasks.taskNames()

    fun taskHasStatus(
        name: String,
        status: String,
    ): Boolean = registerTasks.getTask(name).statusText.contains(status)
}
