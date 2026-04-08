package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.DelegatedPropertyConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class HasEpcFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "propertyCompliance.epcTask.hasEpc.error.missing",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "hasCertIsValidForAction",
            ),
        ],
    )
    var hasCert: Boolean? = null

    var action: String? = null

    fun hasCertIsValidForAction(): Boolean = action == "provideThisLater" || hasCert != null
}
