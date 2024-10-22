package uk.gov.communities.prsdb.webapp.models.dataModels

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import uk.gov.communities.prsdb.webapp.validation.constraints.ValidConfirmedEmail

@ValidConfirmedEmail
class ConfirmedEmailDataModel(
    @field:NotBlank @field:Email val email: String,
    @field:NotBlank @field:Email val confirmEmail: String,
)
