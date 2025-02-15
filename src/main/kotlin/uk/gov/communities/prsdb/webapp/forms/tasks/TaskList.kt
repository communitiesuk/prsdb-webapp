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
        if (!isStepWithIdReachable(journeyData, task.startingStepId)) {
            TaskStatus.CANNOT_START_YET
        } else if (!isStepWithIdComplete(journeyData, task.startingStepId)) {
            TaskStatus.NOT_YET_STARTED
        } else if (!areAllStepsWithinTaskComplete(journeyData, task)) {
            TaskStatus.IN_PROGRESS
        } else {
            TaskStatus.COMPLETED
        }

    private fun isStepWithIdComplete(
        journeyData: JourneyData,
        id: T,
    ): Boolean {
        val currentStep = steps.single { it.id == id }
        val pageData = JourneyDataHelper.getPageData(journeyData, currentStep.name)
        return pageData != null && currentStep.isSatisfied(validator, pageData)
    }

    private fun areAllStepsWithinTaskComplete(
        journeyData: JourneyData,
        task: Task<T>,
    ): Boolean {
        var currentStepId: T? = task.startingStepId
        while (currentStepId != null && currentStepId in task.stepIds) {
            if (isStepWithIdComplete(journeyData, currentStepId)) {
                currentStepId = nextStepId(currentStepId, journeyData)
            } else {
                return false
            }
        }
        return true
    }

    private fun nextStepId(
        currentStepId: T,
        journeyData: JourneyData,
    ) = steps.single { it.id == currentStepId }.nextAction(journeyData, null).first

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
        val startingStepId: T,
        val stepIds: Set<T>,
    ) {
        companion object {
            fun <T : StepId> withOneStep(
                nameKey: String,
                startId: T,
            ) = Task(nameKey, startId, setOf(startId))
        }
    }
}
