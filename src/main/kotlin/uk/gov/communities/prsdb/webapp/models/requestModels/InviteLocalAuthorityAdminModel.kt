package uk.gov.communities.prsdb.webapp.models.requestModels

import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotNullConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class InviteLocalAuthorityAdminModel : ConfirmedEmailRequestModel() {
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "forms.selectLocalAuthority.error.missing",
                validatorType = NotNullConstraintValidator::class,
            ),
        ],
    )
    var localAuthorityId: Int? = null
}
