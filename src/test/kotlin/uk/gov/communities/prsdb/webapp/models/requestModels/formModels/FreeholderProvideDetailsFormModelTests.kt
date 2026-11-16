package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import jakarta.validation.Validation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class FreeholderProvideDetailsFormModelTests {
    private val validator = Validation.buildDefaultValidatorFactory().validator

    @ParameterizedTest
    @CsvSource("true, joint", "false, nonJoint")
    fun `missing answer returns the ownership-specific error`(
        isJointFreeholder: Boolean,
        messageSuffix: String,
    ) {
        val formModel = FreeholderProvideDetailsFormModel().apply { this.isJointFreeholder = isJointFreeholder }

        val violations = validator.validate(formModel)

        assertEquals(
            listOf("registerProperty.freeholders.provideDetails.radios.error.missing.$messageSuffix"),
            violations.map { it.message },
        )
    }

    @ParameterizedTest
    @CsvSource("true, true", "true, false", "false, true", "false, false")
    fun `either answer is valid for both ownership types`(
        isJointFreeholder: Boolean,
        answer: Boolean,
    ) {
        val formModel = FreeholderProvideDetailsFormModel(answer).apply { this.isJointFreeholder = isJointFreeholder }

        val violations = validator.validate(formModel)

        assertTrue(violations.isEmpty())
    }
}
