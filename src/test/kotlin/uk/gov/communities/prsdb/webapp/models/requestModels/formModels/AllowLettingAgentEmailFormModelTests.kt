package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AllowLettingAgentEmailFormModelTests {
    @Test
    fun `isEmailNotCurrentUser returns true when email is different from landlord email`() {
        val formModel =
            AllowLettingAgentEmailFormModel().apply {
                emailAddress = "agent@example.com"
                currentUserEmail = "landlord@example.com"
            }

        assertTrue(formModel.isEmailNotCurrentUser())
    }

    @Test
    fun `isEmailNotCurrentUser returns false when email matches landlord email`() {
        val formModel =
            AllowLettingAgentEmailFormModel().apply {
                emailAddress = "landlord@example.com"
                currentUserEmail = "landlord@example.com"
            }

        assertFalse(formModel.isEmailNotCurrentUser())
    }

    @Test
    fun `isEmailNotCurrentUser returns false when email matches landlord email case-insensitively`() {
        val formModel =
            AllowLettingAgentEmailFormModel().apply {
                emailAddress = "Landlord@Example.COM"
                currentUserEmail = "landlord@example.com"
            }

        assertFalse(formModel.isEmailNotCurrentUser())
    }

    @Test
    fun `isEmailNotCurrentUser returns true when email is null`() {
        val formModel =
            AllowLettingAgentEmailFormModel().apply {
                emailAddress = null
                currentUserEmail = "landlord@example.com"
            }

        assertTrue(formModel.isEmailNotCurrentUser())
    }

    @Test
    fun `isEmailNotCurrentUser returns true when landlord email is null`() {
        val formModel =
            AllowLettingAgentEmailFormModel().apply {
                emailAddress = "agent@example.com"
                currentUserEmail = null
            }

        assertTrue(formModel.isEmailNotCurrentUser())
    }
}
