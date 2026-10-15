package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotNullConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

@IsValidPrioritised
class ChangeOrgTeamMemberAccessLevelFormModel(
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "changeOrgTeamMemberAccessLevel.radios.error.missing",
                validatorType = NotNullConstraintValidator::class,
            ),
        ],
    )
    var role: OrganisationalLandlordUserRole? = null,
) : FormModel
