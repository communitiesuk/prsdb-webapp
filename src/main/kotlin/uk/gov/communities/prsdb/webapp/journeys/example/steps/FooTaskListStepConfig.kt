package uk.gov.communities.prsdb.webapp.journeys.example.steps

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.example.FooJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.taskModels.TaskListItemViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.taskModels.TaskListViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.taskModels.TaskSectionViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.taskModels.TaskStatusViewModel

@Scope("prototype")
@PrsdbWebComponent
class FooTaskListStepConfig : AbstractGenericStepConfig<Complete, NoInputFormModel, FooJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: FooJourneyState): Map<String, Any> =
        mapOf("taskListViewModel" to getTaskListViewModel(state))

    fun getTaskListViewModel(state: FooJourneyState): TaskListViewModel {
        val sectionViewModels =
            listOf(
                TaskSectionViewModel(
                    "propertyCompliance.taskList.upload.heading",
                    "upload-certificates",
                    listOf(
                        TaskListItemViewModel(
                            "OccupationTask",
                            TaskStatusViewModel.fromStatus(state.occupationTask.taskStatus()),
                            url = JourneyStateService.urlWithJourneyState(state.occupied.routeSegment, state.journeyId),
                        ),
                        TaskListItemViewModel(
                            "EpcTask",
                            TaskStatusViewModel.fromStatus(state.epcTask.taskStatus()),
                            url = JourneyStateService.urlWithJourneyState(state.epcQuestion.routeSegment, state.journeyId),
                        ),
                    ),
                ),
            )

        return TaskListViewModel(
            "propertyCompliance.title",
            "propertyCompliance.taskList.heading",
            listOf("propertyCompliance.taskList.subtitle.one"),
            sectionViewModels,
        )
    }

    override fun chooseTemplate(state: FooJourneyState): String = "taskList"

    override fun mode(state: FooJourneyState): Nothing? = null
}

@Scope("prototype")
@PrsdbWebComponent
final class FooTaskListStep(
    stepConfig: FooTaskListStepConfig,
) : RequestableStep<Complete, NoInputFormModel, FooJourneyState>(stepConfig)
