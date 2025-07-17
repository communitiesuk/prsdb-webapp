package uk.gov.communities.prsdb.webapp.forms.steps.factories

import uk.gov.communities.prsdb.webapp.constants.CONTACT_EPC_ASSESSOR_URL
import uk.gov.communities.prsdb.webapp.constants.EPC_GUIDE_URL
import uk.gov.communities.prsdb.webapp.constants.EPC_IMPROVEMENT_GUIDE_URL
import uk.gov.communities.prsdb.webapp.constants.EXEMPTION_OTHER_REASON_MAX_LENGTH
import uk.gov.communities.prsdb.webapp.constants.FIND_EPC_URL
import uk.gov.communities.prsdb.webapp.constants.GAS_SAFE_REGISTER
import uk.gov.communities.prsdb.webapp.constants.GET_NEW_EPC_URL
import uk.gov.communities.prsdb.webapp.constants.MEES_EXEMPTION_GUIDE_URL
import uk.gov.communities.prsdb.webapp.constants.RCP_ELECTRICAL_INFO_URL
import uk.gov.communities.prsdb.webapp.constants.RCP_ELECTRICAL_REGISTER_URL
import uk.gov.communities.prsdb.webapp.constants.REGISTER_PRS_EXEMPTION_URL
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.pages.FileUploadPage
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getAcceptedEpcDetails
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getAutoMatchedEpcIsCorrect
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getDidTenancyStartBeforeEpcExpiry
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEpcDetails
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getEpcLookupCertificateNumber
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasEicrExemption
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasGasSafetyCertExemption
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getIsEicrExemptionReasonOther
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getIsEicrOutdated
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getIsGasSafetyCertOutdated
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getIsGasSafetyExemptionReasonOther
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getLatestEpcCertificateNumber
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getMatchedEpcIsCorrect
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getPropertyHasMeesExemption
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckMatchedEpcFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrExemptionFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrExemptionOtherReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrUploadCertificateFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcExpiryCheckFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcLookupFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafeEngineerNumFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionOtherReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyUploadCertificateFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.MeesExemptionCheckFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.MeesExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.TodayOrPastDateFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

class PropertyComplianceSharedStepFactory(
    private val defaultSaveAfterSubmit: Boolean,
    private val isCheckingOrUpdatingAnswers: Boolean,
    private val nextActionAfterGasSafetyTask: PropertyComplianceStepId,
    private val nextActionAfterEicrTask: PropertyComplianceStepId,
    private val nextActionAfterEpcTask: PropertyComplianceStepId?,
    private val journeyDataService: JourneyDataService,
    private val epcCertificateUrlProvider: EpcCertificateUrlProvider,
) {
    fun createGasSafetyIssueDateStep() =
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
            nextAction = { filteredJourneyData, _ -> gasSafetyIssueDateStepNextAction(filteredJourneyData) },
            saveAfterSubmit = defaultSaveAfterSubmit,
        )

    fun createGasSafetyEngineerNumStep() =
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
            saveAfterSubmit = defaultSaveAfterSubmit,
        )

    fun createGasSafetyUploadStep() =
        Step(
            id = PropertyComplianceStepId.GasSafetyUpload,
            page =
                FileUploadPage(
                    formModel = GasSafetyUploadCertificateFormModel::class,
                    content =
                        mapOf(
                            "title" to "propertyCompliance.title",
                            "fieldSetHeading" to "forms.uploadCertificate.gasSafety.fieldSetHeading",
                            "fieldSetHint" to "forms.uploadCertificate.fieldSetHint",
                        ),
                ),
            nextAction = { _, _ -> Pair(PropertyComplianceStepId.GasSafetyUploadConfirmation, null) },
            saveAfterSubmit = defaultSaveAfterSubmit,
        )

    fun createGasSafetyUploadConfirmationStep() =
        Step(
            id = PropertyComplianceStepId.GasSafetyUploadConfirmation,
            page =
                Page(
                    formModel = NoInputFormModel::class,
                    templateName = "forms/uploadCertificateConfirmationForm",
                    content =
                        mapOf(
                            "title" to "propertyCompliance.title",
                            "submitButtonText" to
                                getSubmitButtonTextOrDefaultIfCheckingOrUpdatingAnswers(
                                    "forms.buttons.saveAndContinueToEICR",
                                    isCheckingOrUpdatingAnswers,
                                ),
                        ),
                ),
            nextAction = { _, _ -> Pair(nextActionAfterGasSafetyTask, null) },
            saveAfterSubmit = defaultSaveAfterSubmit,
        )

    fun createGasSafetyOutdatedStep() =
        Step(
            id = PropertyComplianceStepId.GasSafetyOutdated,
            page =
                Page(
                    formModel = NoInputFormModel::class,
                    templateName = "forms/gasSafetyOutdatedForm",
                    content =
                        mapOf(
                            "title" to "propertyCompliance.title",
                            "submitButtonText" to
                                getSubmitButtonTextOrDefaultIfCheckingOrUpdatingAnswers(
                                    "forms.buttons.saveAndContinueToEICR",
                                    isCheckingOrUpdatingAnswers,
                                ),
                        ),
                ),
            nextAction = { _, _ -> Pair(nextActionAfterGasSafetyTask, null) },
            saveAfterSubmit = defaultSaveAfterSubmit,
        )

    fun createGasSafetyExemptionStep() =
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
            nextAction = { filteredJourneyData, _ -> gasSafetyExemptionStepNextAction(filteredJourneyData) },
            saveAfterSubmit = defaultSaveAfterSubmit,
        )

    fun createGasSafetyExemptionReasonStep(saveAfterSubmit: Boolean = true) =
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
            nextAction = { filteredJourneyData, _ -> gasSafetyExemptionReasonStepNextAction(filteredJourneyData) },
            saveAfterSubmit = saveAfterSubmit,
        )

    fun createGasSafetyExemptionOtherReasonStep() =
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
            saveAfterSubmit = defaultSaveAfterSubmit,
        )

    fun createGasSafetyExemptionConfirmationStep() =
        Step(
            id = PropertyComplianceStepId.GasSafetyExemptionConfirmation,
            page =
                Page(
                    formModel = NoInputFormModel::class,
                    templateName = "forms/gasSafetyExemptionConfirmationForm",
                    content =
                        mapOf(
                            "title" to "propertyCompliance.title",
                            "submitButtonText" to
                                getSubmitButtonTextOrDefaultIfCheckingOrUpdatingAnswers(
                                    "forms.buttons.saveAndContinueToEICR",
                                    isCheckingOrUpdatingAnswers,
                                ),
                        ),
                ),
            nextAction = { _, _ -> Pair(nextActionAfterGasSafetyTask, null) },
            saveAfterSubmit = defaultSaveAfterSubmit,
        )

    fun createGasSafetyExemptionMissingStep() =
        Step(
            id = PropertyComplianceStepId.GasSafetyExemptionMissing,
            page =
                Page(
                    formModel = NoInputFormModel::class,
                    templateName = "forms/gasSafetyExemptionMissingForm",
                    content =
                        mapOf(
                            "title" to "propertyCompliance.title",
                            "submitButtonText" to
                                getSubmitButtonTextOrDefaultIfCheckingOrUpdatingAnswers(
                                    "forms.buttons.saveAndContinueToEICR",
                                    isCheckingOrUpdatingAnswers,
                                ),
                        ),
                ),
            nextAction = { _, _ -> Pair(nextActionAfterGasSafetyTask, null) },
            saveAfterSubmit = defaultSaveAfterSubmit,
        )

    fun createEicrIssueDateStep() =
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
            nextAction = { filteredJourneyData, _ -> eicrIssueDateStepNextAction(filteredJourneyData) },
            saveAfterSubmit = defaultSaveAfterSubmit,
        )

    fun createEicrUploadStep() =
        Step(
            id = PropertyComplianceStepId.EicrUpload,
            page =
                FileUploadPage(
                    formModel = EicrUploadCertificateFormModel::class,
                    content =
                        mapOf(
                            "title" to "propertyCompliance.title",
                            "fieldSetHeading" to "forms.uploadCertificate.eicr.fieldSetHeading",
                            "fieldSetHint" to "forms.uploadCertificate.fieldSetHint",
                        ),
                ),
            nextAction = { _, _ -> Pair(PropertyComplianceStepId.EicrUploadConfirmation, null) },
            saveAfterSubmit = defaultSaveAfterSubmit,
        )

    fun createEicrUploadConfirmationStep() =
        Step(
            id = PropertyComplianceStepId.EicrUploadConfirmation,
            page =
                Page(
                    formModel = NoInputFormModel::class,
                    templateName = "forms/uploadCertificateConfirmationForm",
                    content =
                        mapOf(
                            "title" to "propertyCompliance.title",
                            "submitButtonText" to
                                getSubmitButtonTextOrDefaultIfCheckingOrUpdatingAnswers(
                                    "forms.buttons.saveAndContinueToEPC",
                                    isCheckingOrUpdatingAnswers,
                                ),
                        ),
                ),
            nextAction = { _, _ -> Pair(nextActionAfterEicrTask, null) },
            saveAfterSubmit = defaultSaveAfterSubmit,
        )

    fun createEicrOutdatedStep() =
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
                            "submitButtonText" to
                                getSubmitButtonTextOrDefaultIfCheckingOrUpdatingAnswers(
                                    "forms.buttons.saveAndContinueToEPC",
                                    isCheckingOrUpdatingAnswers,
                                ),
                        ),
                ),
            nextAction = { _, _ -> Pair(nextActionAfterEicrTask, null) },
            saveAfterSubmit = defaultSaveAfterSubmit,
        )

    fun createEicrExemptionStep() =
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
            nextAction = { filteredJourneyData, _ -> eicrExemptionStepNextAction(filteredJourneyData) },
            saveAfterSubmit = defaultSaveAfterSubmit,
        )

    fun createEicrExemptionReasonStep() =
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
            nextAction = { filteredJourneyData, _ -> eicrExemptionReasonStepNextAction(filteredJourneyData) },
            saveAfterSubmit = defaultSaveAfterSubmit,
        )

    fun createEicrExemptionOtherReasonStep() =
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
            saveAfterSubmit = defaultSaveAfterSubmit,
        )

    fun createEicrExemptionConfirmationStep() =
        Step(
            id = PropertyComplianceStepId.EicrExemptionConfirmation,
            page =
                Page(
                    formModel = NoInputFormModel::class,
                    templateName = "forms/eicrExemptionConfirmationForm",
                    content =
                        mapOf(
                            "title" to "propertyCompliance.title",
                            "submitButtonText" to
                                getSubmitButtonTextOrDefaultIfCheckingOrUpdatingAnswers(
                                    "forms.buttons.saveAndContinueToEPC",
                                    isCheckingOrUpdatingAnswers,
                                ),
                        ),
                ),
            nextAction = { _, _ -> Pair(nextActionAfterEicrTask, null) },
            saveAfterSubmit = defaultSaveAfterSubmit,
        )

    fun createEicrExemptionMissingStep() =
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
                            "submitButtonText" to
                                getSubmitButtonTextOrDefaultIfCheckingOrUpdatingAnswers(
                                    "forms.buttons.saveAndContinueToEPC",
                                    isCheckingOrUpdatingAnswers,
                                ),
                        ),
                ),
            nextAction = { _, _ -> Pair(nextActionAfterEicrTask, null) },
            saveAfterSubmit = defaultSaveAfterSubmit,
        )

    fun createEpcNotAutoMatchedStep() =
        Step(
            id = PropertyComplianceStepId.EpcNotAutoMatched,
            page =
                Page(
                    formModel = NoInputFormModel::class,
                    templateName = "forms/epcNotAutoMatchedForm",
                    content =
                        mapOf(
                            "title" to "propertyCompliance.title",
                        ),
                ),
            nextAction = { _, _ -> Pair(PropertyComplianceStepId.EpcLookup, null) },
            saveAfterSubmit = defaultSaveAfterSubmit,
        )

    fun createCheckAutoMatchedEpcStep() =
        Step(
            id = PropertyComplianceStepId.CheckAutoMatchedEpc,
            page = getCheckMatchedEpcPage(autoMatchedEpc = true),
            nextAction = { filteredJourneyData, _ -> checkAutoMatchedEpcStepNextAction(filteredJourneyData) },
            saveAfterSubmit = defaultSaveAfterSubmit,
        )

    fun createCheckMatchedEpcStep(handleSubmitAndRedirect: ((filteredJourneyData: JourneyData) -> String)) =
        Step(
            id = PropertyComplianceStepId.CheckMatchedEpc,
            page = getCheckMatchedEpcPage(autoMatchedEpc = false),
            nextAction = { filteredJourneyData, _ -> checkMatchedEpcStepNextAction(filteredJourneyData) },
            handleSubmitAndRedirect = { filteredJourneyData, _, _ -> handleSubmitAndRedirect(filteredJourneyData) },
            saveAfterSubmit = defaultSaveAfterSubmit,
        )

    fun createEpcSupersededStep(handleSubmitAndRedirect: ((filteredJourneyData: JourneyData) -> String)) =
        Step(
            id = PropertyComplianceStepId.EpcSuperseded,
            page =
                Page(
                    formModel = NoInputFormModel::class,
                    templateName = "forms/epcSupersededForm",
                    content =
                        mapOf(
                            "title" to "propertyCompliance.title",
                            "certificateNumber" to getLatestEpcCertificateNumberFromSession(),
                        ),
                ),
            nextAction = { _, _ -> Pair(PropertyComplianceStepId.CheckMatchedEpc, null) },
            handleSubmitAndRedirect = { filteredJourneyData, _, _ -> handleSubmitAndRedirect(filteredJourneyData) },
            saveAfterSubmit = defaultSaveAfterSubmit,
        )

    fun createEpcExemptionReasonStep() =
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
            saveAfterSubmit = defaultSaveAfterSubmit,
        )

    fun createEpcExemptionConfirmationStep() =
        Step(
            id = PropertyComplianceStepId.EpcExemptionConfirmation,
            page =
                Page(
                    formModel = NoInputFormModel::class,
                    templateName = "forms/epcExemptionConfirmationForm",
                    content =
                        mapOf(
                            "title" to "propertyCompliance.title",
                            "submitButtonText" to
                                getSubmitButtonTextOrDefaultIfCheckingOrUpdatingAnswers(
                                    "forms.buttons.saveAndContinueToLandlordResponsibilities",
                                    isCheckingOrUpdatingAnswers = isCheckingOrUpdatingAnswers,
                                ),
                        ),
                ),
            nextAction = { _, _ -> Pair(nextActionAfterEpcTask, null) },
            saveAfterSubmit = defaultSaveAfterSubmit,
        )

    fun createEpcLookupStep(handleSubmitAndRedirect: ((filteredJourneyData: JourneyData) -> String)) =
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
            nextAction = { filteredJourneyData, _ -> epcLookupStepNextAction(filteredJourneyData) },
            handleSubmitAndRedirect = { filteredJourneyData, _, _ -> handleSubmitAndRedirect(filteredJourneyData) },
            saveAfterSubmit = defaultSaveAfterSubmit,
        )

    fun createEpcNotFoundStep() =
        Step(
            id = PropertyComplianceStepId.EpcNotFound,
            page =
                Page(
                    formModel = NoInputFormModel::class,
                    templateName = "forms/epcNotFoundForm",
                    content =
                        mapOf(
                            "title" to "propertyCompliance.title",
                            "contactAssessorUrl" to CONTACT_EPC_ASSESSOR_URL,
                            "getNewEpcUrl" to GET_NEW_EPC_URL,
                            "searchAgainUrl" to PropertyComplianceStepId.EpcLookup.urlPathSegment,
                            "certificateNumber" to getEpcLookupCertificateNumberFromSession(),
                        ),
                ),
            nextAction = { _, _ -> Pair(nextActionAfterEpcTask, null) },
            saveAfterSubmit = defaultSaveAfterSubmit,
        )

    fun createEpcMissingStep() =
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
                            "submitButtonText" to
                                getSubmitButtonTextOrDefaultIfCheckingOrUpdatingAnswers(
                                    "forms.buttons.saveAndContinueToLandlordResponsibilities",
                                    isCheckingOrUpdatingAnswers = isCheckingOrUpdatingAnswers,
                                ),
                        ),
                ),
            nextAction = { _, _ -> Pair(nextActionAfterEpcTask, null) },
            saveAfterSubmit = defaultSaveAfterSubmit,
        )

    fun createEpcExpiryCheckStep() =
        Step(
            id = PropertyComplianceStepId.EpcExpiryCheck,
            page =
                Page(
                    formModel = EpcExpiryCheckFormModel::class,
                    templateName = "forms/epcExpiryCheckForm",
                    content =
                        mapOf(
                            "title" to "propertyCompliance.title",
                            "fieldSetHeading" to "forms.epcExpiryCheck.fieldSetHeading",
                            "expiryDate" to (getAcceptedEpcDetailsFromSession()?.expiryDateAsJavaLocalDate ?: ""),
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
            nextAction = { filteredJourneyData, _ -> epcExpiryCheckStepNextAction(filteredJourneyData) },
            saveAfterSubmit = defaultSaveAfterSubmit,
        )

    fun createEpcExpiredStep() =
        Step(
            id = PropertyComplianceStepId.EpcExpired,
            page =
                Page(
                    formModel = NoInputFormModel::class,
                    templateName = getEpcExpiredTemplate(),
                    content =
                        mapOf(
                            "title" to "propertyCompliance.title",
                            "getNewEpcUrl" to GET_NEW_EPC_URL,
                            "meesExemptionGuideUrl" to MEES_EXEMPTION_GUIDE_URL,
                            "registerMeesExemptionUrl" to REGISTER_PRS_EXEMPTION_URL,
                            "epcImprovementGuideUrl" to EPC_IMPROVEMENT_GUIDE_URL,
                            "expiryDateAsJavaLocalDate" to (getAcceptedEpcDetailsFromSession()?.expiryDateAsJavaLocalDate ?: ""),
                            "submitButtonText" to
                                getSubmitButtonTextOrDefaultIfCheckingOrUpdatingAnswers(
                                    "forms.buttons.saveAndContinueToLandlordResponsibilities",
                                    isCheckingOrUpdatingAnswers = isCheckingOrUpdatingAnswers,
                                ),
                        ),
                ),
            nextAction = { _, _ -> Pair(nextActionAfterEpcTask, null) },
            saveAfterSubmit = defaultSaveAfterSubmit,
        )

    fun createMeesExemptionCheckStep() =
        Step(
            id = PropertyComplianceStepId.MeesExemptionCheck,
            page =
                Page(
                    formModel = MeesExemptionCheckFormModel::class,
                    templateName = "forms/meesExemptionCheckForm",
                    content =
                        mapOf(
                            "title" to "propertyCompliance.title",
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
                            "meesExemptionGuideUrl" to MEES_EXEMPTION_GUIDE_URL,
                            "singleLineAddress" to (getAcceptedEpcDetailsFromSession()?.singleLineAddress ?: ""),
                        ),
                ),
            nextAction = { filteredJourneyData, _ -> meesExemptionCheckStepNextAction(filteredJourneyData) },
            saveAfterSubmit = defaultSaveAfterSubmit,
        )

    fun createMeesExemptionReasonStep() =
        Step(
            id = PropertyComplianceStepId.MeesExemptionReason,
            page =
                Page(
                    formModel = MeesExemptionReasonFormModel::class,
                    templateName = "forms/exemptionReasonForm",
                    content =
                        mapOf(
                            "title" to "propertyCompliance.title",
                            "fieldSetHeading" to "forms.meesExemptionReason.fieldSetHeading",
                            "radioOptions" to
                                listOf(
                                    RadiosButtonViewModel(
                                        value = MeesExemptionReason.LISTED_BUILDING,
                                        labelMsgKey = "forms.meesExemptionReason.radios.listedBuilding.label",
                                        hintMsgKey = "forms.meesExemptionReason.radios.listedBuilding.hint",
                                    ),
                                    RadiosButtonViewModel(
                                        value = MeesExemptionReason.SMALL_DETACHED_BUILDING,
                                        labelMsgKey = "forms.meesExemptionReason.radios.smallDetachedBuilding.label",
                                        hintMsgKey = "forms.meesExemptionReason.radios.smallDetachedBuilding.hint",
                                    ),
                                    RadiosButtonViewModel(
                                        value = MeesExemptionReason.HIGH_COST,
                                        labelMsgKey = "forms.meesExemptionReason.radios.highCost.label",
                                        hintMsgKey = "forms.meesExemptionReason.radios.highCost.hint",
                                    ),
                                    RadiosButtonViewModel(
                                        value = MeesExemptionReason.ALL_IMPROVEMENTS_MADE,
                                        labelMsgKey = "forms.meesExemptionReason.radios.allImprovementsMade.label",
                                        hintMsgKey = "forms.meesExemptionReason.radios.allImprovementsMade.hint",
                                    ),
                                    RadiosButtonViewModel(
                                        value = MeesExemptionReason.WALL_INSULATION,
                                        labelMsgKey = "forms.meesExemptionReason.radios.wallInsulation.label",
                                        hintMsgKey = "forms.meesExemptionReason.radios.wallInsulation.hint",
                                    ),
                                    RadiosButtonViewModel(
                                        value = MeesExemptionReason.THIRD_PARTY_CONSENT,
                                        labelMsgKey = "forms.meesExemptionReason.radios.thirdPartyConsent.label",
                                        hintMsgKey = "forms.meesExemptionReason.radios.thirdPartyConsent.hint",
                                    ),
                                    RadiosButtonViewModel(
                                        value = MeesExemptionReason.PROPERTY_DEVALUATION,
                                        labelMsgKey = "forms.meesExemptionReason.radios.propertyDevaluation.label",
                                        hintMsgKey = "forms.meesExemptionReason.radios.propertyDevaluation.hint",
                                    ),
                                    RadiosButtonViewModel(
                                        value = MeesExemptionReason.NEW_LANDLORD,
                                        labelMsgKey = "forms.meesExemptionReason.radios.newLandlord.label",
                                        hintMsgKey = "forms.meesExemptionReason.radios.newLandlord.hint",
                                    ),
                                ),
                        ),
                ),
            nextAction = { _, _ -> Pair(PropertyComplianceStepId.MeesExemptionConfirmation, null) },
            saveAfterSubmit = defaultSaveAfterSubmit,
        )

    fun createMeesExemptionConfirmationStep() =
        Step(
            id = PropertyComplianceStepId.MeesExemptionConfirmation,
            page =
                Page(
                    formModel = NoInputFormModel::class,
                    templateName = "forms/meesExemptionConfirmationForm",
                    content =
                        mapOf(
                            "title" to "propertyCompliance.title",
                            "submitButtonText" to
                                getSubmitButtonTextOrDefaultIfCheckingOrUpdatingAnswers(
                                    "forms.buttons.saveAndContinueToLandlordResponsibilities",
                                    isCheckingOrUpdatingAnswers = isCheckingOrUpdatingAnswers,
                                ),
                        ),
                ),
            nextAction = { _, _ -> Pair(nextActionAfterEpcTask, null) },
            saveAfterSubmit = defaultSaveAfterSubmit,
        )

    fun createLowEnergyRatingStep() =
        Step(
            id = PropertyComplianceStepId.LowEnergyRating,
            page =
                Page(
                    formModel = NoInputFormModel::class,
                    templateName = "forms/lowEnergyRatingForm",
                    content =
                        mapOf(
                            "title" to "propertyCompliance.title",
                            "epcImprovementGuideUrl" to EPC_IMPROVEMENT_GUIDE_URL,
                            "registerPrsExemptionUrl" to REGISTER_PRS_EXEMPTION_URL,
                            "submitButtonText" to
                                getSubmitButtonTextOrDefaultIfCheckingOrUpdatingAnswers(
                                    "forms.buttons.saveAndContinueToLandlordResponsibilities",
                                    isCheckingOrUpdatingAnswers = isCheckingOrUpdatingAnswers,
                                ),
                        ),
                ),
            nextAction = { _, _ -> Pair(nextActionAfterEpcTask, null) },
            saveAfterSubmit = defaultSaveAfterSubmit,
        )

    private fun gasSafetyIssueDateStepNextAction(filteredJourneyData: JourneyData) =
        if (filteredJourneyData.getIsGasSafetyCertOutdated()!!) {
            Pair(PropertyComplianceStepId.GasSafetyOutdated, null)
        } else {
            Pair(PropertyComplianceStepId.GasSafetyEngineerNum, null)
        }

    private fun gasSafetyExemptionStepNextAction(filteredJourneyData: JourneyData) =
        if (filteredJourneyData.getHasGasSafetyCertExemption()!!) {
            Pair(PropertyComplianceStepId.GasSafetyExemptionReason, null)
        } else {
            Pair(PropertyComplianceStepId.GasSafetyExemptionMissing, null)
        }

    private fun gasSafetyExemptionReasonStepNextAction(filteredJourneyData: JourneyData) =
        if (filteredJourneyData.getIsGasSafetyExemptionReasonOther()!!) {
            Pair(PropertyComplianceStepId.GasSafetyExemptionOtherReason, null)
        } else {
            Pair(PropertyComplianceStepId.GasSafetyExemptionConfirmation, null)
        }

    private fun getSubmitButtonTextOrDefaultIfCheckingOrUpdatingAnswers(
        submitButtonText: String,
        isCheckingOrUpdatingAnswers: Boolean,
    ) = if (isCheckingOrUpdatingAnswers) {
        "forms.buttons.saveAndContinue"
    } else {
        submitButtonText
    }

    private fun eicrIssueDateStepNextAction(filteredJourneyData: JourneyData) =
        if (filteredJourneyData.getIsEicrOutdated()!!) {
            Pair(PropertyComplianceStepId.EicrOutdated, null)
        } else {
            Pair(PropertyComplianceStepId.EicrUpload, null)
        }

    private fun eicrExemptionStepNextAction(filteredJourneyData: JourneyData) =
        if (filteredJourneyData.getHasEicrExemption()!!) {
            Pair(PropertyComplianceStepId.EicrExemptionReason, null)
        } else {
            Pair(PropertyComplianceStepId.EicrExemptionMissing, null)
        }

    private fun eicrExemptionReasonStepNextAction(filteredJourneyData: JourneyData) =
        if (filteredJourneyData.getIsEicrExemptionReasonOther()!!) {
            Pair(PropertyComplianceStepId.EicrExemptionOtherReason, null)
        } else {
            Pair(PropertyComplianceStepId.EicrExemptionConfirmation, null)
        }

    private fun getCheckMatchedEpcPage(autoMatchedEpc: Boolean): Page {
        val epcDetails = getEpcDetailsFromSession(autoMatched = autoMatchedEpc)
        return Page(
            formModel = CheckMatchedEpcFormModel::class,
            templateName = "forms/checkMatchedEpcForm",
            content =
                mapOf(
                    "title" to "propertyCompliance.title",
                    "fieldSetHeading" to "forms.checkMatchedEpc.fieldSetHeading",
                    "epcDetails" to (epcDetails ?: ""),
                    "epcCertificateUrl" to epcCertificateUrlProvider.getEpcCertificateUrl(epcDetails?.certificateNumber ?: ""),
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
                                labelMsgKey = "forms.checkMatchedEpc.radios.no.label",
                            ),
                        ),
                ),
        )
    }

    private fun checkAutoMatchedEpcStepNextAction(filteredJourneyData: JourneyData): Pair<PropertyComplianceStepId?, Int?> =
        if (filteredJourneyData.getAutoMatchedEpcIsCorrect()!!) {
            matchedEpcIsCorrectNextAction(filteredJourneyData, autoMatched = true)
        } else {
            Pair(PropertyComplianceStepId.EpcLookup, null)
        }

    fun checkMatchedEpcStepNextAction(filteredJourneyData: JourneyData): Pair<PropertyComplianceStepId?, Int?> =
        if (filteredJourneyData.getMatchedEpcIsCorrect()!!) {
            matchedEpcIsCorrectNextAction(filteredJourneyData, autoMatched = false)
        } else {
            // The user will be redirected to the lookup step in handleSubmitAndRedirect
            // When they are redirected, the nextAction of lookupStep is this step (checkMatchedEpc)
            // Here we set checkMatchedEpc's nextAction to null to avoid an infinite loop of previous steps when checking if a step is reachable
            Pair(null, null)
        }

    private fun getAcceptedEpcDetailsFromSession(): EpcDataModel? =
        journeyDataService
            .getJourneyDataFromSession()
            .getAcceptedEpcDetails()

    private fun matchedEpcIsCorrectNextAction(
        filteredJourneyData: JourneyData,
        autoMatched: Boolean,
    ): Pair<PropertyComplianceStepId?, Int?> {
        val epcDetails = filteredJourneyData.getEpcDetails(autoMatched)!!
        if (epcDetails.isPastExpiryDate()) {
            return Pair(PropertyComplianceStepId.EpcExpiryCheck, null)
        }
        if (!epcDetails.isEnergyRatingEOrBetter()) {
            return Pair(PropertyComplianceStepId.MeesExemptionCheck, null)
        }
        return Pair(nextActionAfterEpcTask, null)
    }

    private fun epcLookupStepNextAction(filteredJourneyData: JourneyData): Pair<PropertyComplianceStepId?, Int?> {
        val lookedUpEpcDetails =
            filteredJourneyData.getEpcDetails(autoMatched = false)
                ?: return Pair(PropertyComplianceStepId.EpcNotFound, null)
        return if (lookedUpEpcDetails.isLatestCertificateForThisProperty()) {
            Pair(PropertyComplianceStepId.CheckMatchedEpc, null)
        } else {
            Pair(PropertyComplianceStepId.EpcSuperseded, null)
        }
    }

    private fun epcExpiryCheckStepNextAction(filteredJourneyData: JourneyData): Pair<PropertyComplianceStepId?, Int?> =
        if (filteredJourneyData.getDidTenancyStartBeforeEpcExpiry() == true) {
            if (filteredJourneyData.getAcceptedEpcDetails()?.isEnergyRatingEOrBetter() == true) {
                Pair(nextActionAfterEpcTask, null)
            } else {
                Pair(PropertyComplianceStepId.MeesExemptionCheck, null)
            }
        } else {
            Pair(PropertyComplianceStepId.EpcExpired, null)
        }

    private fun meesExemptionCheckStepNextAction(filteredJourneyData: JourneyData): Pair<PropertyComplianceStepId?, Int?> =
        if (filteredJourneyData.getPropertyHasMeesExemption()!!) {
            Pair(PropertyComplianceStepId.MeesExemptionReason, null)
        } else {
            Pair(PropertyComplianceStepId.LowEnergyRating, null)
        }

    private fun getEpcExpiredTemplate(): String {
        val epcDetails = getAcceptedEpcDetailsFromSession() ?: return ""
        return if (epcDetails.isEnergyRatingEOrBetter()) {
            "forms/epcExpiredForm"
        } else {
            "forms/epcExpiredLowRatingForm"
        }
    }

    private fun getEpcLookupCertificateNumberFromSession(): String {
        val submittedCertificateNumber =
            journeyDataService
                .getJourneyDataFromSession()
                .getEpcLookupCertificateNumber()
                ?: return ""
        return EpcDataModel.parseCertificateNumberOrNull(submittedCertificateNumber)!! // Only valid EPC numbers will be in journeyData
    }

    private fun getEpcDetailsFromSession(autoMatched: Boolean): EpcDataModel? =
        journeyDataService
            .getJourneyDataFromSession()
            .getEpcDetails(autoMatched)

    private fun getLatestEpcCertificateNumberFromSession(): String {
        return journeyDataService.getJourneyDataFromSession().getLatestEpcCertificateNumber()
            ?: return ""
    }
}
