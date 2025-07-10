package uk.gov.communities.prsdb.webapp.forms.steps

import uk.gov.communities.prsdb.webapp.constants.EXEMPTION_OTHER_REASON_MAX_LENGTH
import uk.gov.communities.prsdb.webapp.constants.GAS_SAFE_REGISTER
import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.pages.FileUploadPage
import uk.gov.communities.prsdb.webapp.forms.pages.Page
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasGasSafetyCert
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getHasGasSafetyCertExemption
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getIsGasSafetyCertOutdated
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.getIsGasSafetyExemptionReasonOther
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
        val gasSafetyIssueDateStep
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
                    nextAction = { filteredJourneyData, _ -> gasSafetyIssueDateStepNextAction(filteredJourneyData) },
                )

        val gasSafetyEngineerNumStep
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

        val gasSafetyUploadStep
            get() =
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
                )

        fun gasSafetyUploadConfirmationStep(
            nextActionStepId: PropertyComplianceStepId,
            isCheckingAnswers: Boolean,
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
        )

        fun gasSafetyOutdatedStep(
            nextActionStepId: PropertyComplianceStepId,
            isCheckingAnswers: Boolean,
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
        )

        val gasSafetyExemptionStep
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
                    nextAction = { filteredJourneyData, _ -> gasSafetyExemptionStepNextAction(filteredJourneyData) },
                )

        val gasSafetyExemptionReasonStep
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
                    nextAction = { filteredJourneyData, _ -> gasSafetyExemptionReasonStepNextAction(filteredJourneyData) },
                )

        val gasSafetyExemptionOtherReasonStep
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

        fun gasSafetyExemptionConfirmationStep(
            nextActionStepId: PropertyComplianceStepId,
            isCheckingAnswers: Boolean,
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
        )

        fun gasSafetyExemptionMissingStep(
            nextActionStepId: PropertyComplianceStepId,
            isCheckingAnswers: Boolean,
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
        )

        private fun gasSafetyStepNextAction(filteredJourneyData: JourneyData) =
            if (filteredJourneyData.getHasGasSafetyCert()!!) {
                Pair(PropertyComplianceStepId.GasSafetyIssueDate, null)
            } else {
                Pair(PropertyComplianceStepId.GasSafetyExemption, null)
            }

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
    }
}
