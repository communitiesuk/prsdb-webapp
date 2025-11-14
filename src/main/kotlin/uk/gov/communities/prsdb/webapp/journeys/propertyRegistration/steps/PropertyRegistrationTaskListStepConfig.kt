package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.example.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.example.steps.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.taskModels.TaskListItemViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.taskModels.TaskListViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.taskModels.TaskSectionViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.taskModels.TaskStatusViewModel

@Scope("prototype")
@PrsdbWebComponent
class PropertyRegistrationTaskListStepConfig : AbstractGenericStepConfig<Complete, NoInputFormModel, PropertyRegistrationJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: PropertyRegistrationJourneyState): Map<String, Any> =
        mapOf("taskListViewModel" to getTaskListViewModel(state))

    fun getTaskListViewModel(state: PropertyRegistrationJourneyState): TaskListViewModel {
        val sectionViewModels =
            listOf(
                TaskSectionViewModel(
                    "registerProperty.taskList.register.heading",
                    "register-property",
                    listOf(
                        TaskListItemViewModel(
                            "registerProperty.taskList.register.addAddress",
                            TaskStatusViewModel.fromStatus(state.addressTask.taskStatus()),
                            url = JourneyStateService.urlToStepIfReachable(state.addressTask.firstStep),
                        ),
                        TaskListItemViewModel(
                            "registerProperty.taskList.register.selectType",
                            TaskStatusViewModel.fromStatus(state.propertyTypeStep.taskStatus()),
                            url = JourneyStateService.urlToStepIfReachable(state.propertyTypeStep),
                        ),
                        TaskListItemViewModel(
                            "registerProperty.taskList.register.selectOwnership",
                            TaskStatusViewModel.fromStatus(state.ownershipTypeStep.taskStatus()),
                            hintKey = "registerProperty.taskList.register.selectOwnership.hint",
                            url = JourneyStateService.urlToStepIfReachable(state.ownershipTypeStep),
                        ),
                        TaskListItemViewModel(
                            "registerProperty.taskList.register.addLicensing",
                            TaskStatusViewModel.fromStatus(state.licensingTask.taskStatus()),
                            url = JourneyStateService.urlToStepIfReachable(state.licensingTask.firstStep),
                        ),
                        TaskListItemViewModel(
                            "registerProperty.taskList.register.addTenancyInfo",
                            TaskStatusViewModel.fromStatus(state.occupationTask.taskStatus()),
                            hintKey = "registerProperty.taskList.register.addTenancyInfo.hint",
                            url = JourneyStateService.urlToStepIfReachable(state.occupationTask.firstStep),
                        ),
                    ),
                ),
                TaskSectionViewModel(
                    "registerProperty.taskList.checkAndSubmit.heading",
                    "check-and-submit",
                    listOf(
                        TaskListItemViewModel(
                            "registerProperty.taskList.checkAndSubmit.checkAnswers",
                            TaskStatusViewModel.fromStatus(state.cyaStep.taskStatus()),
                            url = JourneyStateService.urlToStepIfReachable(state.cyaStep),
                        ),
                    ),
                ),
            )

        return TaskListViewModel(
            "registerProperty.title",
            "registerProperty.taskList.heading",
            listOf("registerProperty.taskList.subtitle.one", "registerProperty.taskList.subtitle.two"),
            sectionViewModels,
        )
    }

    override fun chooseTemplate(state: PropertyRegistrationJourneyState): String = "taskList"

    override fun mode(state: PropertyRegistrationJourneyState): Nothing? = null
}

@Scope("prototype")
@PrsdbWebComponent
final class PropertyRegistrationTaskListStep(
    stepConfig: PropertyRegistrationTaskListStepConfig,
) : RequestableStep<Complete, NoInputFormModel, PropertyRegistrationJourneyState>(stepConfig)

fun RequestableStep<*, *, *>.taskStatus(): TaskStatus =
    when {
        this.outcome() != null -> TaskStatus.COMPLETED
        this.isStepReachable -> TaskStatus.NOT_STARTED
        else -> TaskStatus.CANNOT_START
    }
