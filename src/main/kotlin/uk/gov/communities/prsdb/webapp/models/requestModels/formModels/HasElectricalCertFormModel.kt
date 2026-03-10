package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.DelegatedPropertyConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class HasElectricalCertFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "propertyCompliance.electricalSafetyTask.hasElectricalSafetyCertificate.error.missing",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "electricalCertTypeIsValidForAction",
            ),
        ],
    )
    var electricalCertType: String? = null

    var action: String? = null

    fun electricalCertTypeIsValidForAction(): Boolean = action == "provideThisLater" || electricalCertType != null
}
