package uk.gov.communities.prsdb.webapp.models.dataModels

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

class ConfirmedEmailDataModel(
    @field:NotBlank @field:Email val email: String,
    @field:NotBlank val confirmEmail: String,
) {
    @AssertTrue
    fun isConfirmEmailSameAsEmail(): Boolean = email.trim() == confirmEmail.trim()
}
