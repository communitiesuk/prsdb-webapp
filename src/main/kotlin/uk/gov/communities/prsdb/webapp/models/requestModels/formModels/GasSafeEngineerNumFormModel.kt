package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.GasSafeEngineerNumConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotBlankConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class GasSafeEngineerNumFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.gasSafeEngineerNum.error.missing",
                validatorType = NotBlankConstraintValidator::class,
            ),
            ConstraintDescriptor(
                messageKey = "forms.gasSafeEngineerNum.error.invalidFormat",
                validatorType = GasSafeEngineerNumConstraintValidator::class,
            ),
        ],
    )
    var engineerNumber: String = ""

    companion object {
        fun fromComplianceRecord(record: PropertyCompliance): GasSafeEngineerNumFormModel? =
            record.gasSafetyCertEngineerNum?.let {
                GasSafeEngineerNumFormModel().apply {
                    this.engineerNumber = it
                }
            }
    }
}
