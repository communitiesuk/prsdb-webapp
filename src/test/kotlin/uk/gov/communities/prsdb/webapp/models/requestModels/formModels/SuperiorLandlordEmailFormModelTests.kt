package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.Validation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SuperiorLandlordEmailFormModelTests {
    private val validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    fun `blank email returns the missing email message`() {
        // Arrange
        val formModel = SuperiorLandlordEmailFormModel().apply { emailAddress = "" }

        // Act
        val violations = validator.validate(formModel)

        // Assert
        assertEquals(listOf("registerProperty.superiorLandlords.email.error.missing"), violations.map { it.message })
    }

    @Test
    fun `invalid email returns the format message`() {
        // Arrange
        val formModel = SuperiorLandlordEmailFormModel().apply { emailAddress = "not-an-email" }

        // Act
        val violations = validator.validate(formModel)

        // Assert
        assertEquals(listOf("registerProperty.superiorLandlords.email.error.invalidFormat"), violations.map { it.message })
    }

    @Test
    fun `email already used by another superior landlord is rejected`() {
        // Arrange
        val formModel =
            SuperiorLandlordEmailFormModel().apply {
                emailAddress = "superior@example.com"
                existingSuperiorLandlordEmails = listOf("superior@example.com")
            }

        // Act
        val violations = validator.validate(formModel)

        // Assert
        assertEquals(listOf("registerProperty.superiorLandlords.email.error.duplicate"), violations.map { it.message })
    }

    @Test
    fun `email comparison ignores case when checking for another superior landlord`() {
        // Arrange
        val formModel =
            SuperiorLandlordEmailFormModel().apply {
                emailAddress = "Superior@Example.COM"
                existingSuperiorLandlordEmails = listOf("superior@example.com")
            }

        // Act
        val violations = validator.validate(formModel)

        // Assert
        assertEquals(listOf("registerProperty.superiorLandlords.email.error.duplicate"), violations.map { it.message })
    }

    @Test
    fun `valid email not used by another superior landlord passes validation`() {
        // Arrange
        val formModel =
            SuperiorLandlordEmailFormModel().apply {
                emailAddress = "superior@example.com"
                existingSuperiorLandlordEmails = listOf("someone.else@example.com")
            }

        // Act
        val violations = validator.validate(formModel)

        // Assert
        assertTrue(violations.isEmpty())
    }
}
