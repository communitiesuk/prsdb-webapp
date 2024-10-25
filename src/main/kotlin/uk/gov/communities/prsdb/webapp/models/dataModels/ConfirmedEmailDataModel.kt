package uk.gov.communities.prsdb.webapp.models.dataModels

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

class ConfirmedEmailDataModel(
    @field:NotBlank(message = NO_EMAIL_ERROR_MESSAGE) @field:Email(message = NOT_AN_EMAIL_ERROR_MESSAGE) val email: String,
    @field:NotBlank(message = NO_CONFIRMATION_ERROR_MESSAGE) val confirmEmail: String,
) {
    @AssertTrue(message = CONFIRMATION_DOES_NOT_MATCH_EMAIL_ERROR_MESSAGE)
    fun isConfirmEmailSameAsEmail(): Boolean = email.trim() == confirmEmail.trim()

    companion object {
        const val NO_EMAIL_ERROR_MESSAGE = "No value for 'email' was submitted"
        const val NOT_AN_EMAIL_ERROR_MESSAGE = "The value submitted for 'email' was not a valid email address"
        const val NO_CONFIRMATION_ERROR_MESSAGE = "No value for 'confirmEmail' was submitted"
        const val CONFIRMATION_DOES_NOT_MATCH_EMAIL_ERROR_MESSAGE =
            "The value submitted for 'confirmEmail' did not match the value for 'email'"
    }
}
