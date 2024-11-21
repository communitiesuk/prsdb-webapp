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
import org.junit.jupiter.api.Nested
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

    @Nested
    inner class SimplePropertyTests {
        @IsValidPrioritised
        inner class SimpleProperty(
            @ValidatedBy(
                constraints = [
                    ConstraintDescriptor(validatorType = NotBlankConstraintValidator::class, messageKey = "notblank"),
                ],
            )
            val name: String,
        )

        @Test
        fun `no violations for object with a satisfied simple property constraint validation`() {
            val instance = SimpleProperty("test name")

            val violations = validator.validate(instance)

            assertTrue(violations.isEmpty())
        }

        @Test
        fun `violation with specified message and property for object with an unsatisfied simple property constraint validation`() {
            val instance = SimpleProperty("")

            val violations = validator.validate(instance)

            assertEquals(1, violations.size)
            val violation = violations.first()
            assertEquals("notblank", violation.messageTemplate)
            assertEquals("name", violation.propertyPath.toString())
        }
    }

    @Nested
    inner class MultipleConstraintPropertyTests {
        @IsValidPrioritised
        inner class MultipleConstraintProperty(
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
    }

    @Nested
    inner class ParameterizedConstraintPropertyTests {
        @IsValidPrioritised
        inner class ParameterizedConstraintProperty(
            @ValidatedBy(
                constraints = [
                    ConstraintDescriptor(
                        validatorType = LengthConstraintValidator::class,
                        validatorArgs = arrayOf("0", "1"),
                        messageKey = "notvalidlength",
                    ),
                ],
            )
            val internationalAddress: String,
        )

        @Test
        fun `no violations for object with a satisfied parameterized property constraint validation`() {
            val instance = ParameterizedConstraintProperty("t")

            val violations = validator.validate(instance)

            assertTrue(violations.isEmpty())
        }

        @Test
        fun `violation with specified message and property for object with an unsatisfied parameterized property constraint validation`() {
            val instance = ParameterizedConstraintProperty("tn")

            val violations = validator.validate(instance)

            assertEquals(1, violations.size)
            val violation = violations.first()
            assertEquals("notvalidlength", violation.messageTemplate)
            assertEquals("internationalAddress", violation.propertyPath.toString())
        }

        @IsValidPrioritised
        inner class InvalidParameterizedConstraintProperty(
            @ValidatedBy(
                constraints = [
                    ConstraintDescriptor(
                        validatorType = LengthConstraintValidator::class,
                        validatorArgs = arrayOf("0"),
                        messageKey = "notvalidlength",
                    ),
                ],
            )
            val internationalAddress: String,
        )

        @Test
        fun `exception raised if invalid validator arguments are given`() {
            val instance = InvalidParameterizedConstraintProperty("test")

            val exception =
                assertThrows<ValidationException> {
                    validator.validate(instance)
                }
            assertEquals(
                "LengthConstraintValidator expects 2 validatorArgs, but 1 were provided",
                exception.cause?.message,
            )
        }
    }

    @Nested
    inner class DelegatedConstraintPropertyTests {
        @IsValidPrioritised
        inner class DelegatedConstraintProperty(
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
        inner class MissingDelegatedConstraintProperty(
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
        inner class NonBooleanDelegatedConstraintProperty(
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
        inner class ParameterisedDelegatedConstraintProperty(
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
                "Function named parameterisedFunction on ParameterisedDelegatedConstraintProperty" +
                    " must return Boolean and take no parameters",
                exception.cause?.message,
            )
        }
    }
}
