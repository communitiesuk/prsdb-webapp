package uk.gov.communities.prsdb.webapp.forms.tasks

import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus
import uk.gov.communities.prsdb.webapp.forms.journeys.Journey
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.StepId
import uk.gov.communities.prsdb.webapp.helpers.JourneyDataHelper
import uk.gov.communities.prsdb.webapp.models.viewModels.TaskListItemViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.TaskStatusViewModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

abstract class TaskList<T : StepId>(
    private val journey: Journey<T>,
    private val journeyDataService: JourneyDataService,
    private val validator: Validator,
) {
    private val steps = journey.steps
    protected abstract val taskList: List<TaskListItemDataModel<T>>

    open fun getTaskListViewModels(): List<TaskListItemViewModel> {
        val journeyData = journeyDataService.getJourneyDataFromSession()
        val registerPropertyList =
            taskList.map {
                taskListItemViewModel(journeyData, it)
            }
        return registerPropertyList
    }

    private fun taskListItemViewModel(
        journeyData: JourneyData,
        taskListItem: TaskListItemDataModel<T>,
    ): TaskListItemViewModel {
        val status = getStatusForTask(journeyData, taskListItem.startId, taskListItem.completionId)
        return TaskListItemViewModel(
            taskListItem.nameKey,
            TaskStatusViewModel.fromStatus(status),
            if (status == TaskStatus.CANNOT_START_YET) {
                null
            } else {
                taskListItem.startId.urlPathSegment
            },
        )
    }

    private fun getStatusForTask(
        journeyData: JourneyData,
        firstStepId: T,
        completionStepId: T? = null,
    ): TaskStatus =
        if (isStepWithIdReachable(journeyData, completionStepId)) {
            TaskStatus.COMPLETED
        } else {
            if (isStepWithIdReachable(journeyData, firstStepId)) {
                if (isNextStepReachableFromId(journeyData, firstStepId)) {
                    TaskStatus.IN_PROGRESS
                } else {
                    TaskStatus.NOT_YET_STARTED
                }
            } else {
                TaskStatus.CANNOT_START_YET
            }
        }

    private fun isNextStepReachableFromId(
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
        return journey.isReachable(journeyData, currentStep)
    }

    protected data class TaskListItemDataModel<T : StepId>(
        val nameKey: String,
        val startId: T,
        val completionId: T?,
    )
}
