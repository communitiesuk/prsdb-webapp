package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AllowLettingAgentEmailFormModelTests {
    @Test
    fun `isEmailNotLandlord returns true when email is different from landlord email`() {
        val formModel =
            AllowLettingAgentEmailFormModel().apply {
                emailAddress = "agent@example.com"
                landlordEmail = "landlord@example.com"
            }

        assertTrue(formModel.isEmailNotLandlord())
    }

    @Test
    fun `isEmailNotLandlord returns false when email matches landlord email`() {
        val formModel =
            AllowLettingAgentEmailFormModel().apply {
                emailAddress = "landlord@example.com"
                landlordEmail = "landlord@example.com"
            }

        assertFalse(formModel.isEmailNotLandlord())
    }

    @Test
    fun `isEmailNotLandlord returns false when email matches landlord email case-insensitively`() {
        val formModel =
            AllowLettingAgentEmailFormModel().apply {
                emailAddress = "Landlord@Example.COM"
                landlordEmail = "landlord@example.com"
            }

        assertFalse(formModel.isEmailNotLandlord())
    }

    @Test
    fun `isEmailNotLandlord returns true when email is null`() {
        val formModel =
            AllowLettingAgentEmailFormModel().apply {
                emailAddress = null
                landlordEmail = "landlord@example.com"
            }

        assertTrue(formModel.isEmailNotLandlord())
    }

    @Test
    fun `isEmailNotLandlord returns true when landlord email is null`() {
        val formModel =
            AllowLettingAgentEmailFormModel().apply {
                emailAddress = "agent@example.com"
                landlordEmail = null
            }

        assertTrue(formModel.isEmailNotLandlord())
    }
}
