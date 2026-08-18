package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import jakarta.servlet.http.HttpServletRequest
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING
import uk.gov.communities.prsdb.webapp.constants.WITH_BACK_URL_PARAMETER_NAME
import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.isDelegatedToLettingAgent
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.taskModels.TaskListItemViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.taskModels.TaskListViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.taskModels.TaskSectionViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.taskModels.TaskStatusViewModel
import uk.gov.communities.prsdb.webapp.services.BackUrlStorageService

@JourneyFrameworkComponent
class PropertyRegistrationTaskListStepConfig(
    private val featureFlagManager: FeatureFlagManager,
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

        val isRestructured = featureFlagManager.checkFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)

        val sectionViewModels =
            if (isRestructured) {
                restructuredSectionViewModels(state)
            } else {
                legacySectionViewModels(state)
            }

        val backUrlFromState =
            state
                .backUrlKey
                ?.let { backUrlStorageService.getBackUrl(it) }

        return TaskListViewModel(
            "registerProperty.title",
            "registerProperty.taskList.heading",
            listOf("registerProperty.taskList.subtitle"),
            sectionViewModels,
            numberSections = !isRestructured,
            backUrl = backUrlFromState,
        )
    }

    private fun legacySectionViewModels(state: PropertyRegistrationJourneyState): List<TaskSectionViewModel> {
        val ownershipTypeStep = state.ownershipAndLandlordsTask.ownershipTypeStep
        val registerTaskItems =
            listOf(
                TaskListItemViewModel.fromTask("registerProperty.taskList.register.addAddress", state.propertyDetailsTask.addressTask),
                TaskListItemViewModel.fromStep("registerProperty.taskList.register.selectType", state.propertyDetailsTask.propertyTypeStep),
                TaskListItemViewModel.fromStep("registerProperty.taskList.register.selectOwnership", ownershipTypeStep),
                TaskListItemViewModel.fromTask("registerProperty.taskList.register.addLicensing", state.licensingTask),
                TaskListItemViewModel.fromTask("registerProperty.taskList.register.addTenancyInfo", state.occupationTask),
            ) +
                listOf(
                    TaskListItemViewModel.fromTask(
                        "registerProperty.taskList.register.inviteJointLandlords",
                        state.ownershipAndLandlordsTask.jointLandlordsTask,
                    ),
                ) +
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

        val registerSectionHeading =
            if (featureFlagManager.checkFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)) {
                "registerProperty.taskList.register.restructureAndSkipping.heading"
            } else {
                "registerProperty.taskList.register.heading"
            }

        return listOf(
            TaskSectionViewModel(
                registerSectionHeading,
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
    }

    private fun restructuredSectionViewModels(state: PropertyRegistrationJourneyState): List<TaskSectionViewModel> =
        listOf(
            TaskSectionViewModel(
                "registerProperty.taskList.aboutYourProperty.heading",
                "about-your-property",
                listOf(
                    TaskListItemViewModel.fromTask(
                        "registerProperty.taskList.aboutYourProperty.propertyDetails",
                        state.propertyDetailsTask,
                    ),
                    TaskListItemViewModel.fromTask(
                        "registerProperty.taskList.aboutYourProperty.ownershipAndLandlords",
                        state.ownershipAndLandlordsTask,
                    ),
                    TaskListItemViewModel.fromStep(
                        "registerProperty.taskList.aboutYourProperty.occupied",
                        state.occupied,
                    ),
                ),
            ),
            TaskSectionViewModel(
                "registerProperty.taskList.rentedOut.heading",
                "rented-out",
                buildList {
                    if (featureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)) {
                        add(whoProvidesDetailsItem(state))
                    }
                    add(rentedOutComplianceItem("registerProperty.taskList.rentedOut.licensing", state.licensingTask, state))
                    add(rentedOutComplianceItem("registerProperty.taskList.gasSafety", state.gasSafetyTask, state))
                    add(rentedOutComplianceItem("registerProperty.taskList.electricalSafety", state.electricalSafetyTask, state))
                    add(rentedOutComplianceItem("registerProperty.taskList.epc", state.epcTask, state))
                    add(tenancyDetailsItem(state))
                },
            ),
            TaskSectionViewModel(
                "registerProperty.taskList.submitYourRegistration.heading",
                "submit-your-registration",
                listOf(
                    TaskListItemViewModel.fromStep("registerProperty.taskList.checkAndSubmit.checkAnswers", state.cyaStep),
                ),
            ),
        )

    private fun tenancyDetailsItem(state: PropertyRegistrationJourneyState): TaskListItemViewModel =
        if (state.cachedOccupied == false) {
            TaskListItemViewModel(
                nameKey = "registerProperty.taskList.rentedOut.tenancyDetails",
                status = TaskStatusViewModel.fromStatus(TaskStatus.NOT_REQUIRED),
                hintKey = "registerProperty.taskList.rentedOut.tenancyDetailsNotRequiredHint",
                url = null,
            )
        } else if (isDelegatedToLettingAgent(state)) {
            notRequiredLettingAgentItem("registerProperty.taskList.rentedOut.tenancyDetails")
        } else {
            TaskListItemViewModel.fromTask("registerProperty.taskList.rentedOut.tenancyDetails", state.tenancyDetailsTask)
        }

    private fun whoProvidesDetailsItem(state: PropertyRegistrationJourneyState): TaskListItemViewModel =
        if (state.cachedOccupied == false) {
            // An unoccupied property has no rented-out details to provide, so the task is skipped.
            TaskListItemViewModel(
                nameKey = "registerProperty.taskList.rentedOut.whoProvidesDetails",
                status = TaskStatusViewModel.fromStatus(TaskStatus.NOT_REQUIRED),
                hintKey = "registerProperty.taskList.rentedOut.whoProvidesDetailsNotRequiredHint",
                url = null,
            )
        } else {
            TaskListItemViewModel.fromTask("registerProperty.taskList.rentedOut.whoProvidesDetails", state.whoProvidesDetailsTask)
        }

    // When a letting agent provides the rented-out details, the landlord provides none of the compliance
    // details, so those tasks are skipped in the journey and shown as "not required" on the task list.
    private fun rentedOutComplianceItem(
        nameKey: String,
        task: Task<*, *>,
        state: PropertyRegistrationJourneyState,
    ): TaskListItemViewModel =
        if (isDelegatedToLettingAgent(state)) {
            notRequiredLettingAgentItem(nameKey)
        } else {
            TaskListItemViewModel.fromTask(nameKey, task)
        }

    private fun notRequiredLettingAgentItem(nameKey: String): TaskListItemViewModel =
        TaskListItemViewModel(
            nameKey = nameKey,
            status = TaskStatusViewModel.fromStatus(TaskStatus.NOT_REQUIRED),
            hintKey = "registerProperty.taskList.rentedOut.lettingAgentProvidesHint",
            url = null,
        )

    private fun isDelegatedToLettingAgent(state: PropertyRegistrationJourneyState): Boolean =
        state.isDelegatedToLettingAgent(featureFlagManager)

    override fun chooseTemplate(state: PropertyRegistrationJourneyState): String = "taskList"

    override fun mode(state: PropertyRegistrationJourneyState): Nothing? = null
}

@JourneyFrameworkComponent
final class PropertyRegistrationTaskListStep(
    stepConfig: PropertyRegistrationTaskListStepConfig,
) : RequestableStep<Complete, NoInputFormModel, PropertyRegistrationJourneyState>(stepConfig)
