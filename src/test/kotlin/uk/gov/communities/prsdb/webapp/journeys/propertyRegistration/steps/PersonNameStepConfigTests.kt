package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.validation.BindingResult
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FreeholderNameFormModel
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class PersonNameStepConfigTests {
    private val state = mock<JourneyState>()

    @Test
    fun `getStepSpecificContent returns the single freeholder heading, hint and save and continue button`() {
        // Arrange
        val stepConfig = PersonNameStepConfig()

        // Act
        val content = stepConfig.getStepSpecificContent(state)

        // Assert
        assertEquals(
            mapOf(
                "fieldSetHeading" to "registerProperty.freeholders.name.fieldSetHeading.single",
                "fieldSetHint" to "registerProperty.freeholders.name.fieldSetHint",
                "submitButtonText" to "forms.buttons.saveAndContinue",
            ),
            content,
        )
    }

    @Test
    fun `afterPrimaryValidation rejects a blank name with the single freeholder error`() {
        // Arrange
        val stepConfig = PersonNameStepConfig()
        val bindingResult = mock<BindingResult>()
        whenever(bindingResult.target).thenReturn(FreeholderNameFormModel().apply { name = "  " })

        // Act
        stepConfig.afterPrimaryValidation(state, bindingResult)

        // Assert
        verify(bindingResult).rejectValue(
            "name",
            "RejectValueWithMessageKey",
            "registerProperty.freeholders.name.error.missing",
        )
    }

    @Test
    fun `afterPrimaryValidation does not reject a non-blank name`() {
        // Arrange
        val stepConfig = PersonNameStepConfig()
        val bindingResult = mock<BindingResult>()
        whenever(bindingResult.target).thenReturn(FreeholderNameFormModel().apply { name = "Jane Smith" })

        // Act
        stepConfig.afterPrimaryValidation(state, bindingResult)

        // Assert
        verify(bindingResult, never()).rejectValue(any(), any(), any())
    }
}
