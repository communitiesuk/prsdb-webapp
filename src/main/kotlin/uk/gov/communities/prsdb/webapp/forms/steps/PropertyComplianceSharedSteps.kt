package uk.gov.communities.prsdb.webapp.forms.steps

import uk.gov.communities.prsdb.webapp.constants.EXEMPTION_OTHER_REASON_MAX_LENGTH
import uk.gov.communities.prsdb.webapp.constants.GAS_SAFE_REGISTER
import uk.gov.communities.prsdb.webapp.constants.RCP_ELECTRICAL_INFO_URL
import uk.gov.communities.prsdb.webapp.constants.RCP_ELECTRICAL_REGISTER_URL
import uk.gov.communities.prsdb.webapp.constants.enums.EicrExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.pages.FileUploadPage
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasEicrExemption
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasGasSafetyCertExemption
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getIsEicrExemptionReasonOther
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getIsEicrOutdated
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getIsGasSafetyCertOutdated
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getIsGasSafetyExemptionReasonOther
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrExemptionFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrExemptionOtherReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrUploadCertificateFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafeEngineerNumFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionOtherReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyUploadCertificateFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.TodayOrPastDateFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

class PropertyComplianceSharedSteps {
    companion object {
        fun gasSafetyIssueDateStep(saveAfterSubmit: Boolean = true) =
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
                saveAfterSubmit = saveAfterSubmit,
            )

        fun gasSafetyEngineerNumStep(saveAfterSubmit: Boolean = true) =
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
                saveAfterSubmit = saveAfterSubmit,
            )

        fun gasSafetyUploadStep(saveAfterSubmit: Boolean = true) =
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
                saveAfterSubmit = saveAfterSubmit,
            )

        fun gasSafetyUploadConfirmationStep(
            nextActionStepId: PropertyComplianceStepId,
            isCheckingAnswers: Boolean,
            saveAfterSubmit: Boolean = true,
        ) = Step(
            id = PropertyComplianceStepId.GasSafetyUploadConfirmation,
            page =
                Page(
                    formModel = NoInputFormModel::class,
                    templateName = "forms/uploadCertificateConfirmationForm",
                    content =
                        mapOf(
                            "title" to "propertyCompliance.title",
                            "submitButtonText" to
                                getSubmitButtonTextOrDefaultIfCheckingAnswers(
                                    "forms.buttons.saveAndContinueToEICR",
                                    isCheckingAnswers,
                                ),
                        ),
                ),
            nextAction = { _, _ -> Pair(nextActionStepId, null) },
            saveAfterSubmit = saveAfterSubmit,
        )

        fun gasSafetyOutdatedStep(
            nextActionStepId: PropertyComplianceStepId,
            isCheckingAnswers: Boolean,
            saveAfterSubmit: Boolean = true,
        ) = Step(
            id = PropertyComplianceStepId.GasSafetyOutdated,
            page =
                Page(
                    formModel = NoInputFormModel::class,
                    templateName = "forms/gasSafetyOutdatedForm",
                    content =
                        mapOf(
                            "title" to "propertyCompliance.title",
                            "submitButtonText" to
                                getSubmitButtonTextOrDefaultIfCheckingAnswers(
                                    "forms.buttons.saveAndContinueToEICR",
                                    isCheckingAnswers,
                                ),
                        ),
                ),
            nextAction = { _, _ -> Pair(nextActionStepId, null) },
            saveAfterSubmit = saveAfterSubmit,
        )

        fun gasSafetyExemptionStep(saveAfterSubmit: Boolean = true) =
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
                saveAfterSubmit = saveAfterSubmit,
            )

        fun gasSafetyExemptionReasonStep(saveAfterSubmit: Boolean = true) =
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

        fun gasSafetyExemptionOtherReasonStep(saveAfterSubmit: Boolean = true) =
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
                saveAfterSubmit = saveAfterSubmit,
            )

        fun gasSafetyExemptionConfirmationStep(
            nextActionStepId: PropertyComplianceStepId,
            isCheckingAnswers: Boolean,
            saveAfterSubmit: Boolean = true,
        ) = Step(
            id = PropertyComplianceStepId.GasSafetyExemptionConfirmation,
            page =
                Page(
                    formModel = NoInputFormModel::class,
                    templateName = "forms/gasSafetyExemptionConfirmationForm",
                    content =
                        mapOf(
                            "title" to "propertyCompliance.title",
                            "submitButtonText" to
                                getSubmitButtonTextOrDefaultIfCheckingAnswers(
                                    "forms.buttons.saveAndContinueToEICR",
                                    isCheckingAnswers,
                                ),
                        ),
                ),
            nextAction = { _, _ -> Pair(nextActionStepId, null) },
            saveAfterSubmit = saveAfterSubmit,
        )

        fun gasSafetyExemptionMissingStep(
            nextActionStepId: PropertyComplianceStepId,
            isCheckingAnswers: Boolean,
            saveAfterSubmit: Boolean = true,
        ) = Step(
            id = PropertyComplianceStepId.GasSafetyExemptionMissing,
            page =
                Page(
                    formModel = NoInputFormModel::class,
                    templateName = "forms/gasSafetyExemptionMissingForm",
                    content =
                        mapOf(
                            "title" to "propertyCompliance.title",
                            "submitButtonText" to
                                getSubmitButtonTextOrDefaultIfCheckingAnswers(
                                    "forms.buttons.saveAndContinueToEICR",
                                    isCheckingAnswers,
                                ),
                        ),
                ),
            nextAction = { _, _ -> Pair(nextActionStepId, null) },
            saveAfterSubmit = saveAfterSubmit,
        )

        fun eicrIssueDateStep(saveAfterSubmit: Boolean = true) =
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
                saveAfterSubmit = saveAfterSubmit,
            )

        fun eicrUploadStep(saveAfterSubmit: Boolean = true) =
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
                saveAfterSubmit = saveAfterSubmit,
            )

        fun eicrUploadConfirmationStep(
            nextActionStepId: PropertyComplianceStepId,
            isCheckingAnswers: Boolean,
            saveAfterSubmit: Boolean = true,
        ) = Step(
            id = PropertyComplianceStepId.EicrUploadConfirmation,
            page =
                Page(
                    formModel = NoInputFormModel::class,
                    templateName = "forms/uploadCertificateConfirmationForm",
                    content =
                        mapOf(
                            "title" to "propertyCompliance.title",
                            "submitButtonText" to
                                getSubmitButtonTextOrDefaultIfCheckingAnswers(
                                    "forms.buttons.saveAndContinueToEPC",
                                    isCheckingAnswers,
                                ),
                        ),
                ),
            nextAction = { _, _ -> Pair(nextActionStepId, null) },
            saveAfterSubmit = saveAfterSubmit,
        )

        fun eicrOutdatedStep(
            nextActionStepId: PropertyComplianceStepId,
            isCheckingAnswers: Boolean,
            saveAfterSubmit: Boolean = true,
        ) = Step(
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
                                getSubmitButtonTextOrDefaultIfCheckingAnswers(
                                    "forms.buttons.saveAndContinueToEPC",
                                    isCheckingAnswers,
                                ),
                        ),
                ),
            nextAction = { _, _ -> Pair(nextActionStepId, null) },
            saveAfterSubmit = saveAfterSubmit,
        )

        fun eicrExemptionStep(saveAfterSubmit: Boolean = true) =
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
                saveAfterSubmit = saveAfterSubmit,
            )

        fun eicrExemptionReasonStep(saveAfterSubmit: Boolean = true) =
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
                saveAfterSubmit = saveAfterSubmit,
            )

        fun eicrExemptionOtherReasonStep(saveAfterSubmit: Boolean = true) =
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
                saveAfterSubmit = saveAfterSubmit,
            )

        fun eicrExemptionConfirmationStep(
            nextActionStepId: PropertyComplianceStepId,
            isCheckingAnswers: Boolean,
            saveAfterSubmit: Boolean = true,
        ) = Step(
            id = PropertyComplianceStepId.EicrExemptionConfirmation,
            page =
                Page(
                    formModel = NoInputFormModel::class,
                    templateName = "forms/eicrExemptionConfirmationForm",
                    content =
                        mapOf(
                            "title" to "propertyCompliance.title",
                            "submitButtonText" to
                                getSubmitButtonTextOrDefaultIfCheckingAnswers(
                                    "forms.buttons.saveAndContinueToEPC",
                                    isCheckingAnswers,
                                ),
                        ),
                ),
            nextAction = { _, _ -> Pair(nextActionStepId, null) },
            saveAfterSubmit = saveAfterSubmit,
        )

        fun eicrExemptionMissingStep(
            nextActionStepId: PropertyComplianceStepId,
            isCheckingAnswers: Boolean,
            saveAfterSubmit: Boolean = true,
        ) = Step(
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
                                getSubmitButtonTextOrDefaultIfCheckingAnswers(
                                    "forms.buttons.saveAndContinueToEPC",
                                    isCheckingAnswers,
                                ),
                        ),
                ),
            nextAction = { _, _ -> Pair(nextActionStepId, null) },
            saveAfterSubmit = saveAfterSubmit,
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

        private fun getSubmitButtonTextOrDefaultIfCheckingAnswers(
            submitButtonText: String,
            isCheckingAnswers: Boolean,
        ) = if (isCheckingAnswers) {
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
    }
}
