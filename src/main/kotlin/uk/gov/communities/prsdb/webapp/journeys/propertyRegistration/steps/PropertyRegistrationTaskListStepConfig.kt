package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.example.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.taskModels.TaskListItemViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.taskModels.TaskListViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.taskModels.TaskSectionViewModel

@JourneyFrameworkComponent
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
                        TaskListItemViewModel.fromTask("registerProperty.taskList.register.addAddress", state.addressTask),
                        TaskListItemViewModel.fromStep("registerProperty.taskList.register.selectType", state.propertyTypeStep),
                        TaskListItemViewModel.fromStep(
                            "registerProperty.taskList.register.selectOwnership",
                            state.ownershipTypeStep,
                            "registerProperty.taskList.register.selectOwnership.hint",
                        ),
                        TaskListItemViewModel.fromTask("registerProperty.taskList.register.addLicensing", state.licensingTask),
                        TaskListItemViewModel.fromTask(
                            "registerProperty.taskList.register.addTenancyInfo",
                            state.occupationTask,
                            "registerProperty.taskList.register.addTenancyInfo.hint",
                        ),
                    ),
                ),
                TaskSectionViewModel(
                    "registerProperty.taskList.checkAndSubmit.heading",
                    "check-and-submit",
                    listOf(
                        TaskListItemViewModel.fromStep("registerProperty.taskList.checkAndSubmit.checkAnswers", state.cyaStep),
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

@JourneyFrameworkComponent
final class PropertyRegistrationTaskListStep(
    stepConfig: PropertyRegistrationTaskListStepConfig,
) : RequestableStep<Complete, NoInputFormModel, PropertyRegistrationJourneyState>(stepConfig)
