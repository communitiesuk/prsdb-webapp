package uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.springframework.validation.BeanPropertyBindingResult
import uk.gov.communities.prsdb.webapp.constants.FORM_MODEL_ATTR_NAME
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

    @Test
    fun `resolvePageContent pre-fills the answer when prefill data is provided`() {
        // Arrange
        val formModel = AddressInUkFormModel()
        val content = mapOf(FORM_MODEL_ATTR_NAME to formModel, AddressInUkStepConfig.PREFILL_ADDRESS_IN_UK to false)

        // Act
        val result = AddressInUkStepConfig().resolvePageContent(mock(), content)

        // Assert
        assertEquals(false, (result[FORM_MODEL_ATTR_NAME] as AddressInUkFormModel).addressInUk)
    }

    @Test
    fun `resolvePageContent does not overwrite an existing answer`() {
        // Arrange
        val formModel = AddressInUkFormModel().apply { addressInUk = true }
        val content = mapOf(FORM_MODEL_ATTR_NAME to formModel, AddressInUkStepConfig.PREFILL_ADDRESS_IN_UK to false)

        // Act
        val result = AddressInUkStepConfig().resolvePageContent(mock(), content)

        // Assert
        assertEquals(true, (result[FORM_MODEL_ATTR_NAME] as AddressInUkFormModel).addressInUk)
    }

    @Test
    fun `resolvePageContent returns default content when no prefill data is provided`() {
        // Arrange
        val formModel = AddressInUkFormModel()
        val content = mapOf(FORM_MODEL_ATTR_NAME to formModel)

        // Act
        val result = AddressInUkStepConfig().resolvePageContent(mock(), content)

        // Assert
        assertNull((result[FORM_MODEL_ATTR_NAME] as AddressInUkFormModel).addressInUk)
    }

    private fun bindingResultFor(addressInUk: Boolean?) =
        BeanPropertyBindingResult(AddressInUkFormModel().apply { this.addressInUk = addressInUk }, "formModel")
}
