package uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.springframework.validation.BeanPropertyBindingResult
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.AddressInUkFormModel

class AddressInUkStepConfigTests {
    @Test
    fun `afterPrimaryValidation rejects a missing answer with the configured message key`() {
        // Arrange
        val stepConfig = AddressInUkStepConfig().withMissingAnswerError("custom.error.missing")
        val bindingResult = bindingResultFor(addressInUk = null)

        // Act
        stepConfig.afterPrimaryValidation(mock(), bindingResult)

        // Assert
        assertEquals(
            "custom.error.missing",
            bindingResult.getFieldError(AddressInUkFormModel::addressInUk.name)?.defaultMessage,
        )
    }

    @Test
    fun `afterPrimaryValidation accepts an answer`() {
        // Arrange
        val stepConfig = AddressInUkStepConfig().withMissingAnswerError("custom.error.missing")
        val bindingResult = bindingResultFor(addressInUk = false)

        // Act
        stepConfig.afterPrimaryValidation(mock(), bindingResult)

        // Assert
        assertFalse(bindingResult.hasErrors())
    }

    @Test
    fun `afterPrimaryValidation throws for a missing answer if no message key has been configured`() {
        val stepConfig = AddressInUkStepConfig()
        val bindingResult = bindingResultFor(addressInUk = null)

        assertThrows(IllegalStateException::class.java) { stepConfig.afterPrimaryValidation(mock(), bindingResult) }
    }

    private fun bindingResultFor(addressInUk: Boolean?) =
        BeanPropertyBindingResult(AddressInUkFormModel().apply { this.addressInUk = addressInUk }, "formModel")
}
