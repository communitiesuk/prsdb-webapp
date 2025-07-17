package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Length
import uk.gov.communities.prsdb.webapp.constants.EXEMPTION_OTHER_REASON_MAX_LENGTH
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance

class EicrExemptionOtherReasonFormModel : FormModel {
    @NotBlank(message = "forms.eicrExemptionOtherReason.error.missing")
    @Length(message = "forms.eicrExemptionOtherReason.error.tooLong", min = 0, max = EXEMPTION_OTHER_REASON_MAX_LENGTH)
    var otherReason: String = ""

    companion object {
        fun fromComplianceRecordOrNull(record: PropertyCompliance) =
            record.eicrExemptionOtherReason?.let {
                EicrExemptionOtherReasonFormModel().apply {
                    this.otherReason = it
                }
            }
    }
}
