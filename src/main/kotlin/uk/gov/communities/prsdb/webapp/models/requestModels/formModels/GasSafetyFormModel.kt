package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotNullConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class GasSafetyFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.gasSafety.error.missing",
                validatorType = NotNullConstraintValidator::class,
            ),
        ],
    )
    var hasCert: Boolean? = null

    companion object {
        fun fromComplianceRecordOrNull(record: PropertyCompliance): GasSafetyFormModel =
            GasSafetyFormModel().apply {
                hasCert =
                    record.gasSafetyCertIssueDate != null
            }
    }
}
