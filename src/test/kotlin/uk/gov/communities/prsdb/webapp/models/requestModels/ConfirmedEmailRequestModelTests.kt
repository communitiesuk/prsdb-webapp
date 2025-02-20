package uk.gov.communities.prsdb.webapp.models.requestModels

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ConfirmedEmailRequestModelTests {
    @Test
    fun `ConfirmEmail is invalid if the emails do not match`() {
        val confirmedEmail = ConfirmedEmailRequestModel("test", "test2")
        Assertions.assertFalse(confirmedEmail.isConfirmEmailSameAsEmail())
    }

    @Test
    fun `ConfirmEmail is valid if the emails do match`() {
        val confirmedEmail = ConfirmedEmailRequestModel("test", "test")
        Assertions.assertTrue(confirmedEmail.isConfirmEmailSameAsEmail())
    }
}
