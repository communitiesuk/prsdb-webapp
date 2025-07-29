package uk.gov.communities.prsdb.webapp.forms.journeys

import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.forms.steps.StepId
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.ORIGINALLY_NOT_INCLUDED_KEY
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrExemptionOtherReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrUploadCertificateFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafeEngineerNumFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionOtherReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyUploadCertificateFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.TodayOrPastDateFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.UpdateEicrFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.UpdateGasSafetyCertificateFormModel
import kotlin.collections.plus

class PropertyComplianceOriginalJourneyData private constructor(
    private val propertyCompliance: PropertyCompliance,
) {
    companion object {
        fun fromPropertyCompliance(propertyCompliance: PropertyCompliance): JourneyData =
            PropertyComplianceOriginalJourneyData(propertyCompliance).asJourneyData
    }

    private infix fun <T : FormModel?> StepId.toPageData(fromRecordFunc: (PropertyCompliance) -> T): Pair<String, PageData> =
        this.urlPathSegment to (fromRecordFunc(propertyCompliance)?.toPageData() ?: emptyMap())

    // Although the user cannot update their gas safety record to no certificate or exemption, if they have not previously added either then we need
    // to include a route through the journey in case they navigate to the check your answers page without adding a new certificate or exemption.
    private val updateGasCertificateFormModel =
        object : FormModel {
            override fun toPageData() =
                mapOf(
                    UpdateGasSafetyCertificateFormModel::hasNewCertificate.name to (propertyCompliance.gasSafetyCertIssueDate != null),
                    ORIGINALLY_NOT_INCLUDED_KEY to
                        (
                            propertyCompliance.gasSafetyCertIssueDate == null &&
                                propertyCompliance.gasSafetyCertExemptionReason == null
                        ),
                )
        }

    // We need to include a route through the update journey if the user did not previously add an eicr or exemption.
    private val updateEicrFormModel =
        object : FormModel {
            override fun toPageData() =
                mapOf(
                    UpdateEicrFormModel::hasNewCertificate.name to (propertyCompliance.eicrIssueDate != null),
                    ORIGINALLY_NOT_INCLUDED_KEY to
                        (propertyCompliance.eicrIssueDate == null && propertyCompliance.eicrExemptionReason == null),
                )
        }

    private val originalGasSafetyJourneyData: JourneyData =
        mapOf(
            PropertyComplianceStepId.UpdateGasSafety toPageData { _ -> updateGasCertificateFormModel },
            PropertyComplianceStepId.GasSafetyIssueDate toPageData
                { TodayOrPastDateFormModel.fromDateOrNull(it.gasSafetyCertIssueDate) },
            PropertyComplianceStepId.GasSafetyEngineerNum toPageData GasSafeEngineerNumFormModel::fromComplianceRecordOrNull,
            PropertyComplianceStepId.GasSafetyUpload toPageData GasSafetyUploadCertificateFormModel::fromComplianceRecordOrNull,
            PropertyComplianceStepId.GasSafetyUploadConfirmation toPageData { NoInputFormModel() },
            PropertyComplianceStepId.GasSafetyOutdated toPageData { NoInputFormModel() },
            PropertyComplianceStepId.GasSafetyExemptionReason toPageData
                GasSafetyExemptionReasonFormModel::fromComplianceRecordOrNull,
            PropertyComplianceStepId.GasSafetyExemptionOtherReason toPageData
                GasSafetyExemptionOtherReasonFormModel::fromComplianceRecordOrNull,
            PropertyComplianceStepId.GasSafetyExemptionConfirmation toPageData { NoInputFormModel() },
            PropertyComplianceStepId.GasSafetyExemptionMissing toPageData { NoInputFormModel() },
            PropertyComplianceStepId.GasSafetyUpdateCheckYourAnswers toPageData { CheckAnswersFormModel() },
        )

    private val originalEicrJourneyData =
        mapOf(
            PropertyComplianceStepId.UpdateEICR toPageData { _ -> updateEicrFormModel },
            PropertyComplianceStepId.EicrIssueDate toPageData
                { TodayOrPastDateFormModel.fromDateOrNull(it.eicrIssueDate) },
            PropertyComplianceStepId.EicrUpload toPageData EicrUploadCertificateFormModel::fromComplianceRecordOrNull,
            PropertyComplianceStepId.EicrUploadConfirmation toPageData { NoInputFormModel() },
            PropertyComplianceStepId.EicrOutdated toPageData { NoInputFormModel() },
            PropertyComplianceStepId.EicrExemptionReason toPageData EicrExemptionReasonFormModel::fromComplianceRecordOrNull,
            PropertyComplianceStepId.EicrExemptionOtherReason toPageData
                EicrExemptionOtherReasonFormModel::fromComplianceRecordOrNull,
            PropertyComplianceStepId.EicrExemptionConfirmation toPageData { NoInputFormModel() },
            PropertyComplianceStepId.EicrExemptionMissing toPageData { NoInputFormModel() },
            PropertyComplianceStepId.UpdateEicrCheckYourAnswers toPageData { CheckAnswersFormModel() },
        )

    // TODO: PRSD-1312: Add original EPC step data
    private val originalEpcJourneyData = emptyMap<String, PageData>()

    val asJourneyData: JourneyData =
        originalGasSafetyJourneyData +
            originalEicrJourneyData +
            originalEpcJourneyData
}
