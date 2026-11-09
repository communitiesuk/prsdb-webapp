package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.Validation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FreeholderEmailFormModelTests {
    private val validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    fun `blank email returns the missing email message`() {
        // Arrange
        val formModel = FreeholderEmailFormModel().apply { emailAddress = "" }

        // Act
        val violations = validator.validate(formModel)

        // Assert
        assertEquals(listOf("registerProperty.freeholders.email.error.missing"), violations.map { it.message })
    }

    @Test
    fun `invalid email returns the freeholder format message`() {
        // Arrange
        val formModel = FreeholderEmailFormModel().apply { emailAddress = "not-an-email" }

        // Act
        val violations = validator.validate(formModel)

        // Assert
        assertEquals(listOf("registerProperty.freeholders.email.error.invalidFormat"), violations.map { it.message })
    }

    @Test
    fun `email already used by another freeholder is rejected`() {
        // Arrange
        val formModel =
            FreeholderEmailFormModel().apply {
                emailAddress = "james@example.com"
                existingFreeholderEmails = listOf("james@example.com")
            }

        // Act
        val violations = validator.validate(formModel)

        // Assert
        assertEquals(listOf("registerProperty.freeholders.email.error.duplicate"), violations.map { it.message })
    }

    @Test
    fun `email comparison ignores case when checking for another freeholder`() {
        // Arrange
        val formModel =
            FreeholderEmailFormModel().apply {
                emailAddress = "James@Example.COM"
                existingFreeholderEmails = listOf("james@example.com")
            }

        // Act
        val violations = validator.validate(formModel)

        // Assert
        assertEquals(listOf("registerProperty.freeholders.email.error.duplicate"), violations.map { it.message })
    }

    @Test
    fun `valid email not used by another freeholder passes validation`() {
        // Arrange
        val formModel =
            FreeholderEmailFormModel().apply {
                emailAddress = "james@example.com"
                existingFreeholderEmails = listOf("someone.else@example.com")
            }

        // Act
        val violations = validator.validate(formModel)

        // Assert
        assertTrue(violations.isEmpty())
    }
}
