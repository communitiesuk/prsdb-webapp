package uk.gov.communities.prsdb.webapp.validation

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SmsPhoneNumberConstraintValidatorTests {
    private val validator = SmsPhoneNumberConstraintValidator()

    @ParameterizedTest
    @ValueSource(
        strings = [
            "07400123456",
            "+44 7400 123456",
            "07400 123456",
            "02079460123",
            "+33 1 42 68 53 00",
            "+1 202 555 0123",
            "07700900001",
            "+44 7700 900002",
        ],
    )
    fun `telephone numbers and reserved Notify test numbers are accepted`(number: String) {
        assertTrue(validator.isValid(number))
    }

    @ParameterizedTest
    @ValueSource(strings = ["not a number", "", "123", "+44"])
    fun `invalid numbers are rejected`(number: String) {
        assertFalse(validator.isValid(number))
    }
}
