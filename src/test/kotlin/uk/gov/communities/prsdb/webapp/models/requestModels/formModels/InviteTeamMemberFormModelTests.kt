package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.Validation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole

class InviteTeamMemberFormModelTests {
    private val validator = Validation.buildDefaultValidatorFactory().validator

    private fun validFormModel() =
        InviteTeamMemberFormModel().apply {
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

        assertEquals(listOf("inviteTeamMember.email.error.missing"), violations.map { it.message })
    }

    @Test
    fun `is invalid with the format message when the email address is not valid`() {
        val formModel = validFormModel().apply { emailAddress = "not-an-email" }

        val violations = validator.validate(formModel)

        assertEquals(listOf("inviteTeamMember.email.error.invalidFormat"), violations.map { it.message })
    }

    @Test
    fun `is invalid with the missing access level message when no access level is selected`() {
        val formModel = validFormModel().apply { role = null }

        val violations = validator.validate(formModel)

        assertEquals(listOf("inviteTeamMember.role.error.missing"), violations.map { it.message })
    }
}
