package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.constants.enums.GasSafetyExemptionReason
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotNullConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class GasSafetyExemptionReasonFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.gasSafetyExemptionReason.missing",
                validatorType = NotNullConstraintValidator::class,
            ),
        ],
    )
    var exemptionReason: GasSafetyExemptionReason? = null

    companion object {
        fun fromComplianceRecordOrNull(record: PropertyCompliance): GasSafetyExemptionReasonFormModel? =
            record.gasSafetyCertExemptionReason?.let {
                GasSafetyExemptionReasonFormModel().apply {
                    this.exemptionReason = it
                }
            }
    }
}
