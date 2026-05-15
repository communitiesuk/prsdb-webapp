package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import jakarta.servlet.http.HttpServletRequest
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.WITH_BACK_URL_PARAMETER_NAME
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.JointLandlordsPropertyRegistrationStrategy
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.taskModels.TaskListItemViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.taskModels.TaskListViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.taskModels.TaskSectionViewModel
import uk.gov.communities.prsdb.webapp.services.BackUrlStorageService

@JourneyFrameworkComponent
class PropertyRegistrationTaskListStepConfig(
    private val jointLandlordsStrategy: JointLandlordsPropertyRegistrationStrategy,
    private val httpServletRequest: HttpServletRequest,
    private val backUrlStorageService: BackUrlStorageService,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, PropertyRegistrationJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: PropertyRegistrationJourneyState): Map<String, Any> =
        mapOf("taskListViewModel" to getTaskListViewModel(state))

    fun getTaskListViewModel(state: PropertyRegistrationJourneyState): TaskListViewModel {
        val backRequestUrl: Int? = httpServletRequest.getParameter(WITH_BACK_URL_PARAMETER_NAME)?.toIntOrNull()
        if (backRequestUrl != null) {
            state.backUrlKey = backRequestUrl
        }

        val registerTaskItems =
            listOf(
                TaskListItemViewModel.fromTask("registerProperty.taskList.register.addAddress", state.addressTask),
                TaskListItemViewModel.fromStep("registerProperty.taskList.register.selectType", state.propertyTypeStep),
                TaskListItemViewModel.fromStep("registerProperty.taskList.register.selectOwnership", state.ownershipTypeStep),
                TaskListItemViewModel.fromTask("registerProperty.taskList.register.addLicensing", state.licensingTask),
                TaskListItemViewModel.fromTask("registerProperty.taskList.register.addTenancyInfo", state.occupationTask),
            ) + jointLandlordsStrategy.getJointLandlordsTaskListItems(state) +
                listOf(
                    TaskListItemViewModel.fromTask(
                        "registerProperty.taskList.gasSafety",
                        state.gasSafetyTask,
                    ),
                    TaskListItemViewModel.fromTask(
                        "registerProperty.taskList.electricalSafety",
                        state.electricalSafetyTask,
                    ),
                    TaskListItemViewModel.fromTask(
                        "registerProperty.taskList.epc",
                        state.epcTask,
                    ),
                )

        val sectionViewModels =
            listOf(
                TaskSectionViewModel(
                    "registerProperty.taskList.register.heading",
                    "register-property",
                    registerTaskItems,
                ),
                TaskSectionViewModel(
                    "registerProperty.taskList.checkAndSubmit.heading",
                    "check-and-submit",
                    listOf(
                        TaskListItemViewModel.fromStep("registerProperty.taskList.checkAndSubmit.checkAnswers", state.cyaStep),
                    ),
                ),
            )

        val backUrlFromState =
            state
                .backUrlKey
                ?.let { backUrlStorageService.getBackUrl(it) }

        return TaskListViewModel(
            "registerProperty.title",
            "registerProperty.taskList.heading",
            listOf("registerProperty.taskList.subtitle"),
            sectionViewModels,
            backUrl = backUrlFromState,
        )
    }

    override fun chooseTemplate(state: PropertyRegistrationJourneyState): String = "taskList"

    override fun mode(state: PropertyRegistrationJourneyState): Nothing? = null
}

@JourneyFrameworkComponent
final class PropertyRegistrationTaskListStep(
    stepConfig: PropertyRegistrationTaskListStepConfig,
) : RequestableStep<Complete, NoInputFormModel, PropertyRegistrationJourneyState>(stepConfig)
