package uk.gov.communities.prsdb.webapp.forms.tasks

import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus
import uk.gov.communities.prsdb.webapp.forms.journeys.Journey
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.StepId
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.viewModels.TaskListItemViewModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

abstract class TaskList<T : StepId>(
    private val journey: Journey<T>,
    private val journeyDataService: JourneyDataService,
    private val validator: Validator,
) {
    private val steps = journey.steps
    protected abstract val taskList: List<Task<T>>

    open fun getTaskListViewModels(): List<TaskListItemViewModel> {
        val journeyData = journeyDataService.getJourneyDataFromSession()
        return taskList.map { task ->
            taskListItemViewModel(journeyData, task)
        }
    }

    private fun taskListItemViewModel(
        journeyData: JourneyData,
        task: Task<T>,
    ): TaskListItemViewModel {
        val status = getStatusForTask(journeyData, task)
        return TaskListItemViewModel.fromTaskAndStatus(task, status)
    }

    private fun getStatusForTask(
        journeyData: JourneyData,
        task: Task<T>,
    ): TaskStatus =
        if (task.stepIds.all { isStepWithIdComplete(journeyData, it) }) {
            TaskStatus.COMPLETED
        } else if (isStepWithIdComplete(journeyData, task.startId)) {
            TaskStatus.IN_PROGRESS
        } else if (isStepWithIdReachable(journeyData, task.startId)) {
            TaskStatus.NOT_YET_STARTED
        } else {
            TaskStatus.CANNOT_START_YET
        }

    private fun isStepWithIdComplete(
        journeyData: JourneyData,
        id: T,
    ): Boolean {
        val currentStep = steps.single { it.id == id }
        val pageData = JourneyDataHelper.getPageData(journeyData, currentStep.name)
        return pageData != null && currentStep.isSatisfied(validator, pageData)
    }

    private fun isStepWithIdReachable(
        journeyData: JourneyData,
        id: T?,
    ): Boolean {
        if (id == null) return false
        val currentStep = steps.single { it.id == id }
        return journey.isStepReachable(journeyData, currentStep)
    }

    data class Task<T : StepId>(
        val nameKey: String,
        val startId: T,
        val stepIds: Set<T> = setOf(startId),
    )
}
