package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.constraints.NotNull
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.DelegatedPropertyConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotNullConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class EpcLookupFormModel : FormModel {
    @NotNull(message = "forms.epcLookup.error.missing")
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.epcLookup.error.missing",
                validatorType = NotNullConstraintValidator::class,
            ),
            ConstraintDescriptor(
                messageKey = "forms.epcLookup.error.invalidFormat",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isEpcCertificateNumberFormatValid",
            ),
        ],
    )
    var certificateNumber: String? = null

    fun isEpcCertificateNumberFormatValid(): Boolean {
        val certNumberNoHyphens =
            certificateNumber?.replace("-", "")
                ?: return true // a null certificate number does not have an invalid format
        return (certNumberNoHyphens.all { it.isDigit() } && certNumberNoHyphens.length == 20)
    }
}
