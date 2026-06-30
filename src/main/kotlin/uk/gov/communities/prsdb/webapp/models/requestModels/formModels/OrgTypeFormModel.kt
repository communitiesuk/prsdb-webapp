package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.constants.enums.OrgType
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.DelegatedPropertyConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class OrgTypeFormModel : FormModel {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "registerAsALandlord.orgType.error.missing",
                validatorType = DelegatedPropertyConstraintValidator::class,
                targetMethod = "isSelectionValid",
            ),
        ],
    )
    var orgTypes: MutableList<String?> = mutableListOf()

    fun isSelectionValid(): Boolean {
        val selected = orgTypes.filterNotNull().filter { it.isNotBlank() }
        if (selected.isEmpty()) return false
        return !(selected.contains(OrgType.NONE.name) && selected.size > 1)
    }
}
