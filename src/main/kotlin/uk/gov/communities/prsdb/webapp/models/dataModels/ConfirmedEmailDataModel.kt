package uk.gov.communities.prsdb.webapp.models.dataModels

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import uk.gov.communities.prsdb.webapp.validation.constraints.HasMatchingProperties

@HasMatchingProperties("confirmEmail must match email")
class ConfirmedEmailDataModel(
    @field:NotBlank @field:Email val email: String,
    @property:HasMatchingProperties.Matches("email") @field:NotBlank @field:Email val confirmEmail: String,
)
