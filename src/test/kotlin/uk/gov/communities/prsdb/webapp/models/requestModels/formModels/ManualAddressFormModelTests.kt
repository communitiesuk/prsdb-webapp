package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.Validation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.NullAndEmptySource
import org.junit.jupiter.params.provider.NullSource
import org.junit.jupiter.params.provider.ValueSource

class ManualAddressFormModelTests {
    private val validator = Validation.buildDefaultValidatorFactory().validator

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = ["  "])
    fun `blank country returns the missing country error`(country: String?) {
        // Arrange
        val formModel = buildFormModel(country = country, isInternationalAddressesEnabled = true)

        // Act
        val violations = validator.validate(formModel)

        // Assert
        assertEquals(listOf("addressForms.manualAddress.country.error.missing"), violations.map { it.message })
    }

    @Test
    fun `country is valid when it is provided`() {
        // Arrange
        val formModel = buildFormModel(country = "England", isInternationalAddressesEnabled = true)

        // Act
        val violations = validator.validate(formModel)

        // Assert
        assertTrue(violations.isEmpty())
    }

    // TODO: PDJB-2259: Remove this test
    @ParameterizedTest
    @NullSource
    @ValueSource(booleans = [false])
    fun `missing country is valid when international addresses is not enabled`(isInternationalAddressesEnabled: Boolean?) {
        // Arrange
        val formModel = buildFormModel(country = null, isInternationalAddressesEnabled = isInternationalAddressesEnabled)

        // Act
        val violations = validator.validate(formModel)

        // Assert
        assertTrue(violations.isEmpty())
    }

    private fun buildFormModel(
        country: String?,
        isInternationalAddressesEnabled: Boolean?,
    ) = ManualAddressFormModel().apply {
        addressLineOne = "1 Example Road"
        townOrCity = "Townville"
        postcode = "EG1 2AB"
        this.country = country
        this.isInternationalAddressesEnabled = isInternationalAddressesEnabled
    }
}
