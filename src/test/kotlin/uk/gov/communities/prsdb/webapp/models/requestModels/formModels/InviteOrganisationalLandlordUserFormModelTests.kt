package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.Validation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole

class InviteOrganisationalLandlordUserFormModelTests {
    private val validator = Validation.buildDefaultValidatorFactory().validator

    private fun validFormModel() =
        InviteOrganisationalLandlordUserFormModel().apply {
            emailAddress = "invitee@example.com"
            role = OrganisationalLandlordUserRole.ADMIN
        }

    @Test
    fun `is valid when an email address and access level are provided`() {
        val violations = validator.validate(validFormModel())

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `is invalid with the missing email message when the email address is blank`() {
        val formModel = validFormModel().apply { emailAddress = "" }

        val violations = validator.validate(formModel)

        assertEquals(listOf("inviteOrganisationalLandlordUser.email.error.missing"), violations.map { it.message })
    }

    @Test
    fun `is invalid with the format message when the email address is not valid`() {
        val formModel = validFormModel().apply { emailAddress = "not-an-email" }

        val violations = validator.validate(formModel)

        assertEquals(listOf("inviteOrganisationalLandlordUser.email.error.invalidFormat"), violations.map { it.message })
    }

    @Test
    fun `is invalid with the missing access level message when no access level is selected`() {
        val formModel = validFormModel().apply { role = null }

        val violations = validator.validate(formModel)

        assertEquals(listOf("inviteOrganisationalLandlordUser.role.error.missing"), violations.map { it.message })
    }

    @Test
    fun `is invalid with the already invited message when the email address has already been invited`() {
        val formModel = validFormModel().apply { invitedEmailAddresses = mutableListOf("invitee@example.com") }

        val violations = validator.validate(formModel)

        assertEquals(listOf("inviteOrganisationalLandlordUser.email.error.alreadyInvited"), violations.map { it.message })
    }

    @Test
    fun `is invalid with the already invited message when the email address belongs to a team member`() {
        val formModel = validFormModel().apply { memberEmailAddresses = mutableListOf("invitee@example.com") }

        val violations = validator.validate(formModel)

        assertEquals(listOf("inviteOrganisationalLandlordUser.email.error.alreadyInvited"), violations.map { it.message })
    }

    @Test
    fun `is invalid with the already invited message when the email address matches ignoring case`() {
        val formModel =
            validFormModel().apply {
                emailAddress = "Invitee@Example.COM"
                invitedEmailAddresses = mutableListOf("invitee@example.com")
            }

        val violations = validator.validate(formModel)

        assertEquals(listOf("inviteOrganisationalLandlordUser.email.error.alreadyInvited"), violations.map { it.message })
    }

    @Test
    fun `is valid when the email address has not been invited and is not a team member`() {
        val formModel =
            validFormModel().apply {
                invitedEmailAddresses = mutableListOf("someone.else@example.com")
                memberEmailAddresses = mutableListOf("member@example.com")
            }

        val violations = validator.validate(formModel)

        assertTrue(violations.isEmpty())
    }
}
