package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.constants.EPC_GUIDE_URL
import uk.gov.communities.prsdb.webapp.constants.EXEMPTION_OTHER_REASON_MAX_LENGTH
import uk.gov.communities.prsdb.webapp.constants.FIND_EPC_URL
import uk.gov.communities.prsdb.webapp.constants.GAS_SAFE_REGISTER
import uk.gov.communities.prsdb.webapp.constants.GET_NEW_EPC_URL
import uk.gov.communities.prsdb.webapp.constants.RCP_ELECTRICAL_INFO_URL
import uk.gov.communities.prsdb.webapp.constants.RCP_ELECTRICAL_REGISTER_URL
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.HasEpc
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.pages.PageWithContentProvider
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.forms.tasks.JourneySection
import uk.gov.communities.prsdb.webapp.forms.tasks.JourneyTask
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEpcDetails
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEpcLookupCertificateNumber
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasEICR
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasEPC
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasEicrExemption
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasGasSafetyCert
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasGasSafetyCertExemption
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getIsEicrExemptionReasonOther
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getIsEicrOutdated
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getIsGasSafetyCertOutdated
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getIsGasSafetyExemptionReasonOther
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getMatchedEpcIsCorrect
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.resetCheckMatchedEpc
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.resetEpcLookupCertificateNumber
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.withEpcDetails
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckMatchedEpcFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrExemptionFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrExemptionOtherReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrUploadCertificateFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcLookupFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafeEngineerNumFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionOtherReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyUploadCertificateFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.TodayOrPastDateFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosDividerViewModel
import uk.gov.communities.prsdb.webapp.services.EpcLookupService
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

class PropertyComplianceJourney(
    validator: Validator,
    journeyDataService: JourneyDataService,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val propertyOwnershipId: Long,
    private val epcLookupService: EpcLookupService,
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

    // TODO PRSD-1165: Update task list to match new design
    override val sections =
        listOf(
            JourneySection(uploadTasks, "propertyCompliance.taskList.upload.heading", "upload-certificates"),
            JourneySection(
                landlordResponsibilities,
                "propertyCompliance.taskList.landlordResponsibilities.heading",
                "landlord-responsibilities",
            ),
            JourneySection(checkAndSubmitTasks, "propertyCompliance.taskList.checkAndSubmit.heading", "check-and-submit"),
        )

    override val taskListFactory =
        getTaskListViewModelFactory(
            "propertyCompliance.title",
            "propertyCompliance.taskList.heading",
            listOf("propertyCompliance.taskList.subtitle.one", "propertyCompliance.taskList.subtitle.two"),
            numberSections = false,
            backUrl = LANDLORD_DASHBOARD_URL,
        )

    private val uploadTasks
        get() =
            listOf(
                gasSafetyTask,
                eicrTask,
                epcTask,
            )

    private val landlordResponsibilities
        get() =
            listOf(
                fireSafetyTask,
                JourneyTask.withOneStep(
                    placeholderStep(
                        PropertyComplianceStepId.KeepPropertySafe,
                        "TODO PRSD-1152: Compliance (LL resp): H&S Declaration page",
                        PropertyComplianceStepId.CheckAndSubmit,
                    ),
                    "propertyCompliance.taskList.landlordResponsibilities.keepPropertySafe",
                ),
                JourneyTask.withOneStep(
                    placeholderStep(
                        PropertyComplianceStepId.ResponsibilityToTenants,
                        "TODO PRSD-1153: Compliance (LL resp): Legal Responsibilities Declaration page",
                        PropertyComplianceStepId.CheckAndSubmit,
                    ),
                    "propertyCompliance.taskList.landlordResponsibilities.tenants",
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
                JourneyTask.withOneStep(
                    placeholderStep(PropertyComplianceStepId.Declaration, "TODO PRSD-1165: Update task list to match new design"),
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
                    gasSafetyUploadStep,
                    gasSafetyUploadConfirmationStep,
                    gasSafetyOutdatedStep,
                    gasSafetyExemptionStep,
                    gasSafetyExemptionReasonStep,
                    gasSafetyExemptionOtherReasonStep,
                    gasSafetyExemptionConfirmationStep,
                    gasSafetyExemptionMissingStep,
                ),
                "propertyCompliance.taskList.upload.gasSafety",
            )

    private val eicrTask
        get() =
            JourneyTask(
                PropertyComplianceStepId.EICR,
                setOf(
                    eicrStep,
                    eicrIssueDateStep,
                    eicrUploadStep,
                    eicrUploadConfirmationStep,
                    eicrOutdatedStep,
                    eicrExemptionStep,
                    eicrExemptionReasonStep,
                    eicrExemptionOtherReasonStep,
                    eicrExemptionConfirmationStep,
                    eicrExemptionMissingStep,
                ),
                "propertyCompliance.taskList.upload.eicr",
            )

    private val epcTask
        get() =
            JourneyTask(
                PropertyComplianceStepId.EPC,
                setOf(
                    epcStep,
                    checkMatchedEpcStep,
                    epcLookupStep,
                    placeholderStep(
                        PropertyComplianceStepId.EpcNotFound,
                        "TODO PRSD-1139: Implement EPC Not Found step",
                        PropertyComplianceStepId.FireSafetyDeclaration,
                    ),
                    placeholderStep(
                        PropertyComplianceStepId.EpcSuperseded,
                        "TODO PRSD-1140: Implement EPC Superseded step",
                        PropertyComplianceStepId.FireSafetyDeclaration,
                    ),
                    epcMissingStep,
                    epcExemptionReasonStep,
                    epcExemptionConfirmationStep,
                ),
                "propertyCompliance.taskList.upload.epc",
                "propertyCompliance.taskList.upload.epc.hint",
            )

    private val fireSafetyTask
        get() =
            JourneyTask(
                PropertyComplianceStepId.FireSafetyDeclaration,
                setOf(
                    placeholderStep(
                        PropertyComplianceStepId.FireSafetyDeclaration,
                        "TODO PRSD-1150: Compliance (LL resp): Fire Safety Declaration page",
                        PropertyComplianceStepId.CheckAndSubmit,
                    ),
                    placeholderStep(
                        PropertyComplianceStepId.FireSafetyRisk,
                        "TODO PRSD-1151: Compliance (LL resp): Fire Safety Risk Info page",
                        PropertyComplianceStepId.CheckAndSubmit,
                    ),
                ),
                "propertyCompliance.taskList.landlordResponsibilities.fireSafety",
            )

    private val gasSafetyStep
        get() =
            Step(
                id = PropertyComplianceStepId.GasSafety,
                page =
                    PageWithContentProvider(
                        formModel = GasSafetyFormModel::class,
                        templateName = "forms/certificateForm",
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
                nextAction = { _, _ -> Pair(PropertyComplianceStepId.GasSafetyUpload, null) },
            )

    private val gasSafetyUploadStep
        get() =
            Step(
                id = PropertyComplianceStepId.GasSafetyUpload,
                page =
                    Page(
                        formModel = GasSafetyUploadCertificateFormModel::class,
                        templateName = "forms/uploadCertificateForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "fieldSetHeading" to "forms.uploadCertificate.gasSafety.fieldSetHeading",
                                "fieldSetHint" to "forms.uploadCertificate.fieldSetHint",
                            ),
                    ),
                nextAction = { _, _ -> Pair(PropertyComplianceStepId.GasSafetyUploadConfirmation, null) },
            )

    private val gasSafetyUploadConfirmationStep
        get() =
            Step(
                id = PropertyComplianceStepId.GasSafetyUploadConfirmation,
                page =
                    Page(
                        formModel = NoInputFormModel::class,
                        templateName = "forms/uploadCertificateConfirmationForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "submitButtonText" to "forms.buttons.saveAndContinueToEICR",
                            ),
                    ),
                nextAction = { _, _ -> Pair(eicrTask.startingStepId, null) },
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
                            ),
                    ),
                nextAction = { _, _ -> Pair(eicrTask.startingStepId, null) },
            )

    private val gasSafetyExemptionStep
        get() =
            Step(
                id = PropertyComplianceStepId.GasSafetyExemption,
                page =
                    Page(
                        formModel = GasSafetyExemptionFormModel::class,
                        templateName = "forms/exemptionForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "fieldSetHeading" to "forms.gasSafetyExemption.fieldSetHeading",
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
                            ),
                    ),
                nextAction = { journeyData, _ -> gasSafetyExemptionStepNextAction(journeyData) },
            )

    private val gasSafetyExemptionReasonStep
        get() =
            Step(
                id = PropertyComplianceStepId.GasSafetyExemptionReason,
                page =
                    Page(
                        formModel = GasSafetyExemptionReasonFormModel::class,
                        templateName = "forms/exemptionReasonForm.html",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "fieldSetHeading" to "forms.gasSafetyExemptionReason.fieldSetHeading",
                                "radioOptions" to
                                    listOf(
                                        RadiosButtonViewModel(
                                            value = GasSafetyExemptionReason.NO_GAS_SUPPLY,
                                            labelMsgKey = "forms.gasSafetyExemptionReason.radios.noGas.label",
                                        ),
                                        RadiosButtonViewModel(
                                            value = GasSafetyExemptionReason.LONG_LEASE,
                                            labelMsgKey = "forms.gasSafetyExemptionReason.radios.longLease.label",
                                            hintMsgKey = "forms.gasSafetyExemptionReason.radios.longLease.hint",
                                        ),
                                        RadiosButtonViewModel(
                                            value = GasSafetyExemptionReason.OTHER,
                                            labelMsgKey = "forms.gasSafetyExemptionReason.radios.other.label",
                                            hintMsgKey = "forms.gasSafetyExemptionReason.radios.other.hint",
                                        ),
                                    ),
                            ),
                    ),
                nextAction = { journeyData, _ -> gasSafetyExemptionReasonStepNextAction(journeyData) },
            )

    private val gasSafetyExemptionOtherReasonStep
        get() =
            Step(
                id = PropertyComplianceStepId.GasSafetyExemptionOtherReason,
                page =
                    Page(
                        formModel = GasSafetyExemptionOtherReasonFormModel::class,
                        templateName = "forms/exemptionOtherReasonForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "fieldSetHeading" to "forms.gasSafetyExemptionOtherReason.fieldSetHeading",
                                "fieldSetHint" to "forms.gasSafetyExemptionOtherReason.fieldSetHint",
                                "limit" to EXEMPTION_OTHER_REASON_MAX_LENGTH,
                            ),
                    ),
                nextAction = { _, _ -> Pair(PropertyComplianceStepId.GasSafetyExemptionConfirmation, null) },
            )

    private val gasSafetyExemptionConfirmationStep
        get() =
            Step(
                id = PropertyComplianceStepId.GasSafetyExemptionConfirmation,
                page =
                    Page(
                        formModel = NoInputFormModel::class,
                        templateName = "forms/gasSafetyExemptionConfirmationForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                            ),
                    ),
                nextAction = { _, _ -> Pair(eicrTask.startingStepId, null) },
            )

    private val gasSafetyExemptionMissingStep
        get() =
            Step(
                id = PropertyComplianceStepId.GasSafetyExemptionMissing,
                page =
                    Page(
                        formModel = NoInputFormModel::class,
                        templateName = "forms/gasSafetyExemptionMissingForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                            ),
                    ),
                nextAction = { _, _ -> Pair(eicrTask.startingStepId, null) },
            )

    private val eicrStep
        get() =
            Step(
                id = PropertyComplianceStepId.EICR,
                page =
                    PageWithContentProvider(
                        formModel = EicrFormModel::class,
                        templateName = "forms/certificateForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "fieldSetHeading" to "forms.eicr.fieldSetHeading",
                                "fieldSetHint" to "forms.eicr.fieldSetHint",
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
                nextAction = { journeyData, _ -> eicrStepNextAction(journeyData) },
            )

    private val eicrIssueDateStep
        get() =
            Step(
                id = PropertyComplianceStepId.EicrIssueDate,
                page =
                    Page(
                        formModel = TodayOrPastDateFormModel::class,
                        templateName = "forms/dateForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "fieldSetHeading" to "forms.todayOrPastDate.eicr.fieldSetHeading",
                                "fieldSetHint" to "forms.todayOrPastDate.eicr.fieldSetHint",
                                "submitButtonText" to "forms.buttons.saveAndContinue",
                            ),
                    ),
                nextAction = { journeyData, _ -> eicrIssueDateStepNextAction(journeyData) },
            )

    private val eicrUploadStep
        get() =
            Step(
                id = PropertyComplianceStepId.EicrUpload,
                page =
                    Page(
                        formModel = EicrUploadCertificateFormModel::class,
                        templateName = "forms/uploadCertificateForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "fieldSetHeading" to "forms.uploadCertificate.eicr.fieldSetHeading",
                                "fieldSetHint" to "forms.uploadCertificate.fieldSetHint",
                            ),
                    ),
                nextAction = { _, _ -> Pair(PropertyComplianceStepId.EicrUploadConfirmation, null) },
            )

    private val eicrUploadConfirmationStep
        get() =
            Step(
                id = PropertyComplianceStepId.EicrUploadConfirmation,
                page =
                    Page(
                        formModel = NoInputFormModel::class,
                        templateName = "forms/uploadCertificateConfirmationForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "submitButtonText" to "forms.buttons.saveAndContinueToEPC",
                            ),
                    ),
                nextAction = { _, _ -> Pair(epcTask.startingStepId, null) },
            )

    private val eicrOutdatedStep
        get() =
            Step(
                id = PropertyComplianceStepId.EicrOutdated,
                page =
                    Page(
                        formModel = NoInputFormModel::class,
                        templateName = "forms/eicrOutdatedForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "rcpElectricalInfoUrl" to RCP_ELECTRICAL_INFO_URL,
                                "rcpElectricalRegisterUrl" to RCP_ELECTRICAL_REGISTER_URL,
                            ),
                    ),
                nextAction = { _, _ -> Pair(epcTask.startingStepId, null) },
            )

    private val eicrExemptionStep
        get() =
            Step(
                id = PropertyComplianceStepId.EicrExemption,
                page =
                    Page(
                        formModel = EicrExemptionFormModel::class,
                        templateName = "forms/exemptionForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "fieldSetHeading" to "forms.eicrExemption.fieldSetHeading",
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
                            ),
                    ),
                nextAction = { journeyData, _ -> eicrExemptionStepNextAction(journeyData) },
            )

    private val eicrExemptionReasonStep
        get() =
            Step(
                id = PropertyComplianceStepId.EicrExemptionReason,
                page =
                    Page(
                        formModel = EicrExemptionReasonFormModel::class,
                        templateName = "forms/exemptionReasonForm.html",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "fieldSetHeading" to "forms.eicrExemptionReason.fieldSetHeading",
                                "radioOptions" to
                                    listOf(
                                        RadiosButtonViewModel(
                                            value = GasSafetyExemptionReason.LONG_LEASE,
                                            labelMsgKey = "forms.eicrExemptionReason.radios.longLease.label",
                                            hintMsgKey = "forms.eicrExemptionReason.radios.longLease.hint",
                                        ),
                                        RadiosButtonViewModel(
                                            value = EicrExemptionReason.STUDENT_ACCOMMODATION,
                                            labelMsgKey = "forms.eicrExemptionReason.radios.studentAccommodation.label",
                                        ),
                                        RadiosButtonViewModel(
                                            value = EicrExemptionReason.LIVE_IN_LANDLORD,
                                            labelMsgKey = "forms.eicrExemptionReason.radios.liveInLandlord.label",
                                        ),
                                        RadiosButtonViewModel(
                                            value = GasSafetyExemptionReason.OTHER,
                                            labelMsgKey = "forms.eicrExemptionReason.radios.other.label",
                                            hintMsgKey = "forms.eicrExemptionReason.radios.other.hint",
                                        ),
                                    ),
                            ),
                    ),
                nextAction = { journeyData, _ -> eicrExemptionReasonStepNextAction(journeyData) },
            )

    private val eicrExemptionOtherReasonStep
        get() =
            Step(
                id = PropertyComplianceStepId.EicrExemptionOtherReason,
                page =
                    Page(
                        formModel = EicrExemptionOtherReasonFormModel::class,
                        templateName = "forms/exemptionOtherReasonForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "fieldSetHeading" to "forms.eicrExemptionOtherReason.fieldSetHeading",
                                "fieldSetHint" to "forms.eicrExemptionOtherReason.fieldSetHint",
                                "limit" to EXEMPTION_OTHER_REASON_MAX_LENGTH,
                            ),
                    ),
                nextAction = { _, _ -> Pair(PropertyComplianceStepId.EicrExemptionConfirmation, null) },
            )

    private val eicrExemptionConfirmationStep
        get() =
            Step(
                id = PropertyComplianceStepId.EicrExemptionConfirmation,
                page =
                    Page(
                        formModel = NoInputFormModel::class,
                        templateName = "forms/eicrExemptionConfirmationForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                            ),
                    ),
                nextAction = { _, _ -> Pair(epcTask.startingStepId, null) },
            )

    private val eicrExemptionMissingStep
        get() =
            Step(
                id = PropertyComplianceStepId.EicrExemptionMissing,
                page =
                    Page(
                        formModel = NoInputFormModel::class,
                        templateName = "forms/eicrExemptionMissingForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "rcpElectricalInfoUrl" to RCP_ELECTRICAL_INFO_URL,
                                "rcpElectricalRegisterUrl" to RCP_ELECTRICAL_REGISTER_URL,
                            ),
                    ),
                nextAction = { _, _ -> Pair(epcTask.startingStepId, null) },
            )

    private val epcStep
        get() =
            Step(
                id = PropertyComplianceStepId.EPC,
                page =
                    PageWithContentProvider(
                        formModel = EpcFormModel::class,
                        templateName = "forms/certificateForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "fieldSetHeading" to "forms.epc.fieldSetHeading",
                                "fieldSetHint" to "forms.epc.fieldSetHint",
                                "radioOptions" to
                                    listOf(
                                        RadiosButtonViewModel(
                                            value = HasEpc.YES,
                                            labelMsgKey = "forms.radios.option.yes.label",
                                        ),
                                        RadiosButtonViewModel(
                                            value = HasEpc.NO,
                                            labelMsgKey = "forms.radios.option.no.label",
                                        ),
                                        RadiosDividerViewModel(
                                            "forms.radios.dividerText",
                                        ),
                                        RadiosButtonViewModel(
                                            value = HasEpc.NOT_REQUIRED,
                                            labelMsgKey = "forms.epc.radios.option.notRequired.label",
                                        ),
                                    ),
                                BACK_URL_ATTR_NAME to taskListUrlSegment,
                            ),
                    ) { mapOf("address" to getPropertyAddress()) },
                nextAction = { journeyData, _ -> epcStepNextAction(journeyData) },
            )

    private val epcMissingStep
        get() =
            Step(
                id = PropertyComplianceStepId.EpcMissing,
                page =
                    Page(
                        formModel = NoInputFormModel::class,
                        templateName = "forms/epcMissingForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "findEpcUrl" to FIND_EPC_URL,
                                "getNewEpcUrl" to GET_NEW_EPC_URL,
                            ),
                    ),
                nextAction = { _, _ -> Pair(landlordResponsibilities.first().startingStepId, null) },
            )

    private val epcExemptionReasonStep
        get() =
            Step(
                id = PropertyComplianceStepId.EpcExemptionReason,
                page =
                    Page(
                        formModel = EpcExemptionReasonFormModel::class,
                        templateName = "forms/epcExemptionReasonForm.html",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "fieldSetHeading" to "forms.epcExemptionReason.fieldSetHeading",
                                "epcGuideUrl" to EPC_GUIDE_URL,
                                "radioOptions" to
                                    listOf(
                                        RadiosButtonViewModel(
                                            value = EpcExemptionReason.LISTED_BUILDING,
                                            labelMsgKey = "forms.epcExemptionReason.radios.listedBuilding.label",
                                        ),
                                        RadiosButtonViewModel(
                                            value = EpcExemptionReason.ANNUAL_USE_LESS_THAN_4_MONTHS,
                                            labelMsgKey = "forms.epcExemptionReason.radios.annualUseLessThan4Months.label",
                                        ),
                                        RadiosButtonViewModel(
                                            value = EpcExemptionReason.ANNUAL_ENERGY_CONSUMPTION_LESS_THAN_25_PERCENT,
                                            labelMsgKey = "forms.epcExemptionReason.radios.annualEnergyConsumptionLessThan25Percent.label",
                                        ),
                                        RadiosButtonViewModel(
                                            value = EpcExemptionReason.TEMPORARY_BUILDING,
                                            labelMsgKey = "forms.epcExemptionReason.radios.temporaryBuilding.label",
                                        ),
                                        RadiosButtonViewModel(
                                            value = EpcExemptionReason.STANDALONE_SMALL_BUILDING,
                                            labelMsgKey = "forms.epcExemptionReason.radios.standaloneSmallBuilding.label",
                                            hintMsgKey = "forms.epcExemptionReason.radios.standaloneSmallBuilding.hint",
                                        ),
                                        RadiosButtonViewModel(
                                            value = EpcExemptionReason.DUE_FOR_DEMOLITION,
                                            labelMsgKey = "forms.epcExemptionReason.radios.dueForDemolition.label",
                                        ),
                                    ),
                            ),
                    ),
                nextAction = { _, _ -> Pair(PropertyComplianceStepId.EpcExemptionConfirmation, null) },
            )

    private val epcExemptionConfirmationStep
        get() =
            Step(
                id = PropertyComplianceStepId.EpcExemptionConfirmation,
                page =
                    Page(
                        formModel = NoInputFormModel::class,
                        templateName = "forms/epcExemptionConfirmationForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                            ),
                    ),
                nextAction = { _, _ -> Pair(landlordResponsibilities.first().startingStepId, null) },
            )

    private val checkMatchedEpcStep
        get() =
            // TODO PRSD-1132 - implement this properly
            Step(
                id = PropertyComplianceStepId.CheckMatchedEpc,
                page =
                    Page(
                        formModel = CheckMatchedEpcFormModel::class,
                        templateName = "forms/checkMatchedEpcForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "fieldSetHeading" to "forms.checkMatchedEpc.fieldSetHeading",
                                "fieldSetHint" to "forms.checkMatchedEpc.fieldSetHint",
                                "address" to "TEMP",
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
                            ),
                    ),
                nextAction = { journeyData, _ -> checkMatchedEpcStepNextAction(journeyData) },
                handleSubmitAndRedirect = { journeyData, _, _ ->
                    checkMatchedEpcStepHandleSubmitAndRedirect(journeyData)
                },
            )

    private val epcLookupStep
        get() =
            Step(
                id = PropertyComplianceStepId.EpcLookup,
                page =
                    Page(
                        formModel = EpcLookupFormModel::class,
                        templateName = "forms/epcLookupForm",
                        content =
                            mapOf(
                                "title" to "propertyCompliance.title",
                                "fieldSetHeading" to "forms.epcLookup.fieldSetHeading",
                                "fieldSetHint" to "forms.epcLookup.fieldSetHint",
                                "findEpcUrl" to FIND_EPC_URL,
                                "getNewEpcUrl" to GET_NEW_EPC_URL,
                            ),
                    ),
                nextAction = { journeyData, _ -> epcLookupStepNextAction(journeyData) },
                handleSubmitAndRedirect = { journeyData, _, _ ->
                    epcLookupStepHandleSubmitAndRedirect(journeyData)
                },
            )

    private fun placeholderStep(
        stepId: PropertyComplianceStepId,
        todoComment: String,
        nextStepId: PropertyComplianceStepId? = null,
    ) = Step(
        id = stepId,
        page = Page(formModel = NoInputFormModel::class, templateName = "forms/todo", content = mapOf("todoComment" to todoComment)),
        nextAction = { _, _ -> Pair(nextStepId, null) },
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

    private fun gasSafetyExemptionStepNextAction(journeyData: JourneyData) =
        if (journeyData.getHasGasSafetyCertExemption()!!) {
            Pair(PropertyComplianceStepId.GasSafetyExemptionReason, null)
        } else {
            Pair(PropertyComplianceStepId.GasSafetyExemptionMissing, null)
        }

    private fun gasSafetyExemptionReasonStepNextAction(journeyData: JourneyData) =
        if (journeyData.getIsGasSafetyExemptionReasonOther()!!) {
            Pair(PropertyComplianceStepId.GasSafetyExemptionOtherReason, null)
        } else {
            Pair(PropertyComplianceStepId.GasSafetyExemptionConfirmation, null)
        }

    private fun eicrStepNextAction(journeyData: JourneyData) =
        if (journeyData.getHasEICR()!!) {
            Pair(PropertyComplianceStepId.EicrIssueDate, null)
        } else {
            Pair(PropertyComplianceStepId.EicrExemption, null)
        }

    private fun eicrIssueDateStepNextAction(journeyData: JourneyData) =
        if (journeyData.getIsEicrOutdated()!!) {
            Pair(PropertyComplianceStepId.EicrOutdated, null)
        } else {
            Pair(PropertyComplianceStepId.EicrUpload, null)
        }

    private fun eicrExemptionStepNextAction(journeyData: JourneyData) =
        if (journeyData.getHasEicrExemption()!!) {
            Pair(PropertyComplianceStepId.EicrExemptionReason, null)
        } else {
            Pair(PropertyComplianceStepId.EicrExemptionMissing, null)
        }

    private fun eicrExemptionReasonStepNextAction(journeyData: JourneyData) =
        if (journeyData.getIsEicrExemptionReasonOther()!!) {
            Pair(PropertyComplianceStepId.EicrExemptionOtherReason, null)
        } else {
            Pair(PropertyComplianceStepId.EicrExemptionConfirmation, null)
        }

    private fun epcStepNextAction(journeyData: JourneyData) =
        when (journeyData.getHasEPC()!!) {
            HasEpc.YES -> Pair(PropertyComplianceStepId.CheckMatchedEpc, null)
            HasEpc.NO -> Pair(PropertyComplianceStepId.EpcMissing, null)
            HasEpc.NOT_REQUIRED -> Pair(PropertyComplianceStepId.EpcExemptionReason, null)
        }

    private fun checkMatchedEpcStepNextAction(journeyData: JourneyData) =
        if (journeyData.getMatchedEpcIsCorrect() == true) {
            // TODO: PRSD-1132
            Pair(PropertyComplianceStepId.FireSafetyDeclaration, null)
        } else {
            Pair(PropertyComplianceStepId.EpcLookup, null)
        }

    private fun checkMatchedEpcStepHandleSubmitAndRedirect(journeyData: JourneyData): String {
        val newJourneyData = journeyData.resetEpcLookupCertificateNumber()
        journeyDataService.setJourneyDataInSession(newJourneyData)

        val checkMatchedEpcStep = steps.single { it.id == PropertyComplianceStepId.CheckMatchedEpc }
        return getRedirectForNextStep(checkMatchedEpcStep, newJourneyData, null)
    }

    private fun epcLookupStepHandleSubmitAndRedirect(journeyData: JourneyData): String {
        val certificateNumber = journeyData.getEpcLookupCertificateNumber()!!
        val lookedUpEpc = epcLookupService.getEpcByCertificateNumber(certificateNumber)

        var newJourneyData = journeyData.withEpcDetails(lookedUpEpc)
        if (lookedUpEpc != null && lookedUpEpc.isLatestCertificateForThisProperty()) {
            newJourneyData = newJourneyData.resetCheckMatchedEpc()
        }
        journeyDataService.setJourneyDataInSession(newJourneyData)

        val epcLookupStep = steps.single { it.id == PropertyComplianceStepId.EpcLookup }
        return getRedirectForNextStep(epcLookupStep, newJourneyData, null)
    }

    private fun epcLookupStepNextAction(journeyData: JourneyData): Pair<PropertyComplianceStepId, Int?> {
        val lookedUpEpcDetails =
            journeyData.getEpcDetails()
                ?: return Pair(PropertyComplianceStepId.EpcNotFound, null)
        return if (lookedUpEpcDetails.isLatestCertificateForThisProperty()) {
            Pair(PropertyComplianceStepId.CheckMatchedEpc, null)
        } else {
            Pair(PropertyComplianceStepId.EpcSuperseded, null)
        }
    }

    private fun getPropertyAddress() =
        propertyOwnershipService
            .getPropertyOwnership(propertyOwnershipId)
            .property.address.singleLineAddress

    companion object {
        val initialStepId = PropertyComplianceStepId.GasSafety
    }
}
