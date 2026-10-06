package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps.TokenValidity
import uk.gov.communities.prsdb.webapp.validation.ConstraintDescriptor
import uk.gov.communities.prsdb.webapp.validation.IsValidPrioritised
import uk.gov.communities.prsdb.webapp.validation.NotNullConstraintValidator
import uk.gov.communities.prsdb.webapp.validation.ValidatedBy

// TODO PDJB-1822: Delete this form model when ValidateTokenStep becomes an internal step
@IsValidPrioritised
class TokenValidityFormModel(
    @ValidatedBy(
        constraints = [
            ConstraintDescriptor(
                messageKey = "acceptOrganisationInvitation.validateToken.radios.error.missing",
                validatorType = NotNullConstraintValidator::class,
            ),
        ],
    )
    var tokenValidity: TokenValidity? = null,
) : FormModel
