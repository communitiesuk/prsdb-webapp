package uk.gov.communities.prsdb.webapp.validation

import jakarta.validation.Validation
import jakarta.validation.ValidationException
import jakarta.validation.Validator
import jakarta.validation.ValidatorFactory
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class IsValidPrioritisedValidatorTest {
    private lateinit var validatorFactory: ValidatorFactory
    private lateinit var validator: Validator

    @BeforeEach
    fun setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory()
        validator = validatorFactory.validator
    }

    @AfterEach
    fun tearDown() {
        validatorFactory.close()
    }

    @IsValidPrioritised
    internal class SimpleProperty(
        @ValidatedBy(
            constraints = [
                ConstraintDescriptor(validatorType = NotBlankConstraintValidator::class, messageKey = "notblank"),
            ],
        )
        val name: String,
    )

    @Test
    fun `no violations for object with a simple property constraint validation`() {
        val instance = SimpleProperty("test name")

        val violations = validator.validate(instance)

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `violation with specified message and property for object with a simple property constraint validation that is not satisfied`() {
        val instance = SimpleProperty("")

        val violations = validator.validate(instance)

        assertEquals(1, violations.size)
        val violation = violations.first()
        assertEquals("notblank", violation.messageTemplate)
        assertEquals("name", violation.propertyPath.toString())
    }

    @IsValidPrioritised
    internal class MultipleConstraintProperty(
        @ValidatedBy(
            constraints = [
                ConstraintDescriptor(validatorType = NotBlankConstraintValidator::class, messageKey = "notblank"),
                ConstraintDescriptor(validatorType = EmailConstraintValidator::class, messageKey = "notemail"),
            ],
        )
        val email: String,
    )

    @Test
    fun `only one violation is given, even if multiple constraints would be violated`() {
        val instance = MultipleConstraintProperty("")

        val violations = validator.validate(instance)

        assertEquals(1, violations.size)
        val violation = violations.first()
        assertEquals("notblank", violation.messageTemplate)
        assertEquals("email", violation.propertyPath.toString())
    }

    @Test
    fun `violation is given, even if earlier constraints were not violated`() {
        val instance = MultipleConstraintProperty("not an email")

        val violations = validator.validate(instance)

        assertEquals(1, violations.size)
        val violation = violations.first()
        assertEquals("notemail", violation.messageTemplate)
        assertEquals("email", violation.propertyPath.toString())
    }

    @IsValidPrioritised
    internal class DelegatedConstraintProperty(
        val email: String,
        @ValidatedBy(
            constraints = [
                ConstraintDescriptor(
                    validatorType = DelegatedPropertyConstraintValidator::class,
                    messageKey = "notsame",
                    targetMethod = "emailAndConfirmEmailMatch",
                ),
            ],
        )
        val confirmEmail: String,
    ) {
        fun emailAndConfirmEmailMatch(): Boolean = email == confirmEmail
    }

    @Test
    fun `no violation given by delegated constraint if it is satisfied`() {
        val instance = DelegatedConstraintProperty("test", "test")

        val violations = validator.validate(instance)

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `violation given by delegated constraint if it is not satisfied`() {
        val instance = DelegatedConstraintProperty("test", "different")

        val violations = validator.validate(instance)

        assertEquals(1, violations.size)
        val violation = violations.first()
        assertEquals("notsame", violation.messageTemplate)
        assertEquals("confirmEmail", violation.propertyPath.toString())
    }

    @IsValidPrioritised
    internal class MissingDelegatedConstraintProperty(
        @ValidatedBy(
            constraints = [
                ConstraintDescriptor(
                    validatorType = DelegatedPropertyConstraintValidator::class,
                    messageKey = "notsame",
                    targetMethod = "missingMethod",
                ),
            ],
        )
        val someProperty: String,
    )

    @Test
    fun `exception raised if delegated validation function does not exist`() {
        val instance = MissingDelegatedConstraintProperty("test")

        val exception =
            assertThrows<ValidationException> {
                validator.validate(instance)
            }
        assertEquals(
            "No function named missingMethod found on MissingDelegatedConstraintProperty",
            exception.cause?.message,
        )
    }

    @IsValidPrioritised
    internal class NonBooleanDelegatedConstraintProperty(
        @ValidatedBy(
            constraints = [
                ConstraintDescriptor(
                    validatorType = DelegatedPropertyConstraintValidator::class,
                    messageKey = "notsame",
                    targetMethod = "nonBooleanFunction",
                ),
            ],
        )
        val someProperty: String,
    ) {
        fun nonBooleanFunction(): Int = 3
    }

    @Test
    fun `exception raised if delegated validation function does not return Boolean`() {
        val instance = NonBooleanDelegatedConstraintProperty("test")

        val exception =
            assertThrows<ValidationException> {
                validator.validate(instance)
            }
        assertEquals(
            "Function named nonBooleanFunction on NonBooleanDelegatedConstraintProperty must return Boolean and take no parameters",
            exception.cause?.message,
        )
    }

    @IsValidPrioritised
    internal class ParameterisedDelegatedConstraintProperty(
        @ValidatedBy(
            constraints = [
                ConstraintDescriptor(
                    validatorType = DelegatedPropertyConstraintValidator::class,
                    messageKey = "notsame",
                    targetMethod = "parameterisedFunction",
                ),
            ],
        )
        val someProperty: String,
    ) {
        fun parameterisedFunction(someParam: String): Boolean = true
    }

    @Test
    fun `exception raised if delegated validation function takes parameters`() {
        val instance = ParameterisedDelegatedConstraintProperty("test")

        val exception =
            assertThrows<ValidationException> {
                validator.validate(instance)
            }
        assertEquals(
            "Function named parameterisedFunction on ParameterisedDelegatedConstraintProperty must return Boolean and take no parameters",
            exception.cause?.message,
        )
    }
}
