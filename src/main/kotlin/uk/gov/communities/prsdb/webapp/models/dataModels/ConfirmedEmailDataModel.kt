package uk.gov.communities.prsdb.webapp.models.dataModels

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import kotlinx.serialization.Serializable

@Serializable
class ConfirmedEmailDataModel(
    @field:NotBlank(message = NO_EMAIL_ERROR_MESSAGE) @field:Email(message = NOT_AN_EMAIL_ERROR_MESSAGE) val email: String = "",
    @field:NotBlank(message = NO_CONFIRMATION_ERROR_MESSAGE) val confirmEmail: String = "",
) : ValidatableDataModel {
    @AssertTrue(message = CONFIRMATION_DOES_NOT_MATCH_ERROR_MESSAGE)
    fun isConfirmEmailSameAsEmail(): Boolean = email.trim() == confirmEmail.trim()

    companion object {
        const val NO_EMAIL_ERROR_MESSAGE = "addLAUser.error.missingEmail"
        const val NOT_AN_EMAIL_ERROR_MESSAGE = "addLAUser.error.notAnEmail"
        const val NO_CONFIRMATION_ERROR_MESSAGE = "addLAUser.error.noConfirmation"
        const val CONFIRMATION_DOES_NOT_MATCH_ERROR_MESSAGE = "addLAUser.error.confirmationDoesNotMatch"
    }

    override val errorFieldMap = mapOf(CONFIRMATION_DOES_NOT_MATCH_ERROR_MESSAGE to ::confirmEmail.name)
    override val errorPrecedenceList =
        listOf(
            NO_EMAIL_ERROR_MESSAGE,
            NOT_AN_EMAIL_ERROR_MESSAGE,
            NO_CONFIRMATION_ERROR_MESSAGE,
            CONFIRMATION_DOES_NOT_MATCH_ERROR_MESSAGE,
        )
}
