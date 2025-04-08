package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.constants.GAS_SAFE_REGISTER
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.pages.PageWithContentProvider
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.forms.tasks.JourneySection
import uk.gov.communities.prsdb.webapp.forms.tasks.JourneyTask
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasGasSafetyCert
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getIsGasSafetyCertOutdated
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafeEngineerNumFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.TodayOrPastDateFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

class PropertyComplianceJourney(
    validator: Validator,
    journeyDataService: JourneyDataService,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val propertyOwnershipId: Long,
    principalName: String,
) : JourneyWithTaskList<PropertyComplianceStepId>(
        journeyType = JourneyType.PROPERTY_COMPLIANCE,
        initialStepId = initialStepId,
        validator = validator,
        journeyDataService = journeyDataService,
    ) {
    init {
        loadJourneyDataIfNotLoaded(principalName)
    }

    override val sections =
        listOf(
            JourneySection(uploadTasks, "propertyCompliance.taskList.upload.heading", "upload-certificates"),
            JourneySection(checkAndSubmitTasks, "propertyCompliance.taskList.checkAndSubmit.heading", "check-and-submit"),
        )

    override val taskListFactory =
        getTaskListViewModelFactory(
            "propertyCompliance.title",
            "propertyCompliance.taskList.heading",
            listOf("propertyCompliance.taskList.subtitle.one", "propertyCompliance.taskList.subtitle.two"),
            numberSections = false,
        )

    private val uploadTasks
        get() =
            listOf(
                gasSafetyTask,
                // TODO PRSD-954: Implement EICR upload task
                JourneyTask.withOneStep(
                    placeholderStep(PropertyComplianceStepId.EICR, "TODO PRSD-954: Implement EICR upload task"),
                    "propertyCompliance.taskList.upload.eicr",
                ),
                // TODO PRSD-395: Implement EPC upload task
                JourneyTask.withOneStep(
                    placeholderStep(PropertyComplianceStepId.EPC, "TODO PRSD-395: Implement EPC task"),
                    "propertyCompliance.taskList.upload.epc",
                    "propertyCompliance.taskList.upload.epc.hint",
                ),
            )

    private val checkAndSubmitTasks
        get() =
            listOf(
                // TODO PRSD-962: Implement check and submit task
                JourneyTask.withOneStep(
                    placeholderStep(PropertyComplianceStepId.CheckAndSubmit, "TODO PRSD-962: Implement check and submit task"),
                    "propertyCompliance.taskList.checkAndSubmit.check",
                ),
                // TODO PRSD-963: Implement declaration task
                JourneyTask.withOneStep(
                    placeholderStep(PropertyComplianceStepId.Declaration, "TODO PRSD-963: Implement declaration task"),
                    "propertyCompliance.taskList.checkAndSubmit.declare",
                ),
            )

    private val gasSafetyTask
        get() =
            JourneyTask(
                PropertyComplianceStepId.GasSafety,
                setOf(
                    gasSafetyStep,
                    gasSafetyIssueDateStep,
                    gasSafetyEngineerNumStep,
                    // TODO PRSD-945: Implement gas safety cert upload step
                    placeholderStep(
                        PropertyComplianceStepId.GasSafetyUpload,
                        "TODO PRSD-945: Implement gas safety cert upload step",
                    ),
                    gasSafetyOutdatedStep,
                    // TODO PRSD-949: Implement gas safety cert exemption step
                    placeholderStep(
                        PropertyComplianceStepId.GasSafetyExemption,
                        "TODO PRSD-949: Implement gas safety cert exemption step",
                    ),
                ),
                "propertyCompliance.taskList.upload.gasSafety",
            )

    private val gasSafetyStep
        get() =
            Step(
                id = PropertyComplianceStepId.GasSafety,
                page =
                    PageWithContentProvider(
                        formModel = GasSafetyFormModel::class,
                        templateName = "forms/gasSafetyForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "fieldSetHeading" to "forms.gasSafety.fieldSetHeading",
                                "fieldSetHint" to "forms.gasSafety.fieldSetHint",
                                "radioOptions" to
                                    listOf(
                                        RadiosButtonViewModel(
                                            value = true,
                                            valueStr = "yes",
                                            labelMsgKey = "forms.radios.option.yes.label",
                                        ),
                                        RadiosButtonViewModel(
                                            value = false,
                                            valueStr = "no",
                                            labelMsgKey = "forms.radios.option.no.label",
                                        ),
                                    ),
                                BACK_URL_ATTR_NAME to taskListUrlSegment,
                            ),
                    ) { mapOf("address" to getPropertyAddress()) },
                nextAction = { journeyData, _ -> gasSafetyStepNextAction(journeyData) },
            )

    private val gasSafetyIssueDateStep
        get() =
            Step(
                id = PropertyComplianceStepId.GasSafetyIssueDate,
                page =
                    Page(
                        formModel = TodayOrPastDateFormModel::class,
                        templateName = "forms/dateForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "fieldSetHeading" to "forms.todayOrPastDate.gasSafetyCert.fieldSetHeading",
                                "fieldSetHint" to "forms.todayOrPastDate.gasSafetyCert.fieldSetHint",
                                "submitButtonText" to "forms.buttons.saveAndContinue",
                            ),
                    ),
                nextAction = { journeyData, _ -> gasSafetyIssueDateStepNextAction(journeyData) },
            )

    private val gasSafetyEngineerNumStep
        get() =
            Step(
                id = PropertyComplianceStepId.GasSafetyEngineerNum,
                page =
                    Page(
                        formModel = GasSafeEngineerNumFormModel::class,
                        templateName = "forms/gasSafeEngineerNumForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "fieldSetHeading" to "forms.gasSafeEngineerNum.fieldSetHeading",
                                "fieldSetHint" to "forms.gasSafeEngineerNum.fieldSetHint",
                                "gasSafeRegisterURL" to GAS_SAFE_REGISTER,
                            ),
                    ),
                nextAction = { _, _ -> (PropertyComplianceStepId.GasSafetyUpload to null) },
            )

    private val gasSafetyOutdatedStep
        get() =
            Step(
                id = PropertyComplianceStepId.GasSafetyOutdated,
                page =
                    Page(
                        formModel = NoInputFormModel::class,
                        templateName = "forms/gasSafetyOutdatedForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "taskListUrl" to taskListUrlSegment,
                            ),
                    ),
            )

    private fun placeholderStep(
        stepId: PropertyComplianceStepId,
        todoComment: String,
    ) = Step(
        id = stepId,
        page = Page(formModel = NoInputFormModel::class, templateName = "todo", content = mapOf("todoComment" to todoComment)),
    )

    private fun gasSafetyStepNextAction(journeyData: JourneyData) =
        if (journeyData.getHasGasSafetyCert()!!) {
            Pair(PropertyComplianceStepId.GasSafetyIssueDate, null)
        } else {
            Pair(PropertyComplianceStepId.GasSafetyExemption, null)
        }

    private fun gasSafetyIssueDateStepNextAction(journeyData: JourneyData) =
        if (journeyData.getIsGasSafetyCertOutdated()!!) {
            Pair(PropertyComplianceStepId.GasSafetyOutdated, null)
        } else {
            Pair(PropertyComplianceStepId.GasSafetyEngineerNum, null)
        }

    private fun getPropertyAddress() =
        propertyOwnershipService
            .getPropertyOwnership(propertyOwnershipId)
            .property.address.singleLineAddress

    companion object {
        val initialStepId = PropertyComplianceStepId.GasSafety
    }
}
