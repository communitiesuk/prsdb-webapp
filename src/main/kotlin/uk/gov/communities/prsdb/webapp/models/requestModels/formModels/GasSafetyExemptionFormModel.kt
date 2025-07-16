package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotNullConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class GasSafetyExemptionFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.gasSafetyExemption.missing",
                validatorType = NotNullConstraintValidator::class,
            ),
        ],
    )
    var hasExemption: Boolean? = null

    companion object {
        fun fromComplianceRecord(record: PropertyCompliance): GasSafetyExemptionFormModel? =
            if (record.gasSafetyCertIssueDate == null) {
                GasSafetyExemptionFormModel().apply {
                    this.hasExemption = record.hasGasSafetyExemption
                }
            } else {
                null
            }
    }
}
