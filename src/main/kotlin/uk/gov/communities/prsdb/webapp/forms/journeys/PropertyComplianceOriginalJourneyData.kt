package uk.gov.communities.prsdb.webapp.forms.journeys

import kotlinx.datetime.toKotlinLocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import uk.gov.communities.prsdb.webapp.constants.enums.NonStepJourneyDataKey
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.PageData
import uk.gov.communities.prsdb.webapp.forms.steps.PropertyComplianceStepId
import uk.gov.communities.prsdb.webapp.forms.steps.StepId
import uk.gov.communities.prsdb.webapp.helpers.extensions.journeyExtensions.PropertyComplianceJourneyDataExtensions.Companion.ORIGINALLY_NOT_INCLUDED_KEY
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckMatchedEpcFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrExemptionOtherReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EicrUploadCertificateFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcExpiryCheckFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcLookupFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafeEngineerNumFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionOtherReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSafetyUploadCertificateFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.MeesExemptionCheckFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.MeesExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.TodayOrPastDateFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.UpdateEicrFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.UpdateEpcFormModel
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

    // Although the user cannot update their epc record to no certificate or exemption, if they have not previously added either then we need
    // to include a route through the journey in case they navigate to the check your answers page without adding a new certificate or exemption.
    private val updateEpcFormModel =
        object : FormModel {
            override fun toPageData() =
                mapOf(
                    UpdateEpcFormModel::hasNewCertificate.name to (propertyCompliance.epcUrl != null),
                    ORIGINALLY_NOT_INCLUDED_KEY to (
                        propertyCompliance.epcUrl == null &&
                            propertyCompliance.epcExemptionReason == null
                    ),
                )
        }

    // This is not stored in the property compliance record, but must be true to progress through the journey.
    private val checkMatchedEpcFormModelAcceptingMatchedEpc =
        object : FormModel {
            override fun toPageData() =
                mapOf(
                    CheckMatchedEpcFormModel::matchedEpcIsCorrect.name to true,
                )
        }

    // This is not stored in the property compliance record, but must be true to progress through the journey if the epc is not automatched.
    private val epcLookupFormModelWithDummyCertificateNumber =
        object : FormModel {
            override fun toPageData() =
                mapOf(
                    EpcLookupFormModel::certificateNumber.name to "0000-0000-0000-0000-0000",
                )
        }

    private fun reconstructEpcModelOrNull(): EpcDataModel? {
        val certificateUrlSegment = propertyCompliance.epcUrl?.split("/")?.lastOrNull()
        val rating = propertyCompliance.epcEnergyRating
        val expiryDate = propertyCompliance.epcExpiryDate
        if (certificateUrlSegment == null || rating == null || expiryDate == null) {
            return null
        }

        val certificateNumber =
            EpcDataModel.parseCertificateNumberOrNull(certificateUrlSegment)
                ?: throw PrsdbWebException("Invalid EPC URL format: $propertyCompliance.epcUrl")

        return EpcDataModel(
            certificateNumber = certificateNumber,
            singleLineAddress = propertyCompliance.propertyOwnership.property.address.singleLineAddress,
            energyRating = rating,
            expiryDate = expiryDate.toKotlinLocalDate(),
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

    private val originalEpcJourneyData: JourneyData =
        mapOf(
            PropertyComplianceStepId.UpdateEpc toPageData { _ -> updateEpcFormModel },
            PropertyComplianceStepId.EpcNotAutoMatched toPageData { NoInputFormModel() },
            PropertyComplianceStepId.CheckAutoMatchedEpc toPageData { checkMatchedEpcFormModelAcceptingMatchedEpc },
            PropertyComplianceStepId.CheckMatchedEpc toPageData { checkMatchedEpcFormModelAcceptingMatchedEpc },
            PropertyComplianceStepId.EpcLookup toPageData { epcLookupFormModelWithDummyCertificateNumber },
            NonStepJourneyDataKey.LookedUpEpc.key to reconstructEpcModelOrNull()?.let { Json.encodeToString(it) },
            PropertyComplianceStepId.EpcNotFound toPageData { NoInputFormModel() },
            PropertyComplianceStepId.EpcSuperseded toPageData { NoInputFormModel() },
            PropertyComplianceStepId.EpcExpiryCheck toPageData EpcExpiryCheckFormModel::fromComplianceRecordOrNull,
            PropertyComplianceStepId.EpcExpired toPageData { NoInputFormModel() },
            PropertyComplianceStepId.EpcExemptionReason toPageData EpcExemptionReasonFormModel::fromComplianceRecordOrNull,
            PropertyComplianceStepId.EpcExemptionConfirmation toPageData { NoInputFormModel() },
            PropertyComplianceStepId.MeesExemptionCheck toPageData MeesExemptionCheckFormModel::fromComplianceRecordOrNull,
            PropertyComplianceStepId.MeesExemptionReason toPageData MeesExemptionReasonFormModel::fromComplianceRecordOrNull,
            PropertyComplianceStepId.MeesExemptionConfirmation toPageData { NoInputFormModel() },
            PropertyComplianceStepId.LowEnergyRating toPageData { NoInputFormModel() },
            PropertyComplianceStepId.UpdateEpcCheckYourAnswers toPageData { CheckAnswersFormModel() },
        )

    val asJourneyData: JourneyData =
        originalGasSafetyJourneyData +
            originalEicrJourneyData +
            originalEpcJourneyData +
            (PropertyComplianceStepId.CheckComplianceExists toPageData { NoInputFormModel() })
}
