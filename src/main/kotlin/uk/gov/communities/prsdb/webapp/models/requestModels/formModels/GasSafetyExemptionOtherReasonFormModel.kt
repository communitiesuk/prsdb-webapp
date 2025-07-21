package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.constants.EXEMPTION_OTHER_REASON_MAX_LENGTH
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.LengthConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.NotBlankConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class GasSafetyExemptionOtherReasonFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.gasSafetyExemptionOtherReason.error.missing",
                validatorType = NotBlankConstraintValidator::class,
            ),
            ConstraintDescriptor(
                messageKey = "forms.gasSafetyExemptionOtherReason.error.tooLong",
                validatorType = LengthConstraintValidator::class,
                validatorArgs = arrayOf("0", EXEMPTION_OTHER_REASON_MAX_LENGTH.toString()),
            ),
        ],
    )
    var otherReason: String = ""

    companion object {
        fun fromComplianceRecordOrNull(record: PropertyCompliance): GasSafetyExemptionOtherReasonFormModel? =
            record.gasSafetyCertExemptionOtherReason?.let {
                GasSafetyExemptionOtherReasonFormModel().apply {
                    this.otherReason = it
                }
            }
    }
}
