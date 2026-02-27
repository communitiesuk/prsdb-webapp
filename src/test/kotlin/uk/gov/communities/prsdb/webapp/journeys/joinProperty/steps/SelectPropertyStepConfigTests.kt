package uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.validation.BeanPropertyBindingResult
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.states.JoinPropertyAddressSearchState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectFromListFormModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class SelectPropertyStepConfigTests {
    @Mock
    lateinit var mockPropertyAddressSearchState: JoinPropertyAddressSearchState

    @Mock
    lateinit var mockSelectPropertyStep: SelectPropertyStep

    val routeSegment = SelectPropertyStep.ROUTE_SEGMENT

    @Nested
    inner class Mode {
        @Test
        fun `mode returns null when form model is not present`() {
            // Arrange
            val stepConfig = setupStepConfig()
            whenever(mockPropertyAddressSearchState.selectPropertyStep).thenReturn(mockSelectPropertyStep)
            whenever(mockSelectPropertyStep.formModelOrNull).thenReturn(null)

            // Act
            val result = stepConfig.mode(mockPropertyAddressSearchState)

            // Assert
            assertNull(result)
        }

        @Test
        fun `mode returns null when property is null`() {
            // Arrange
            val stepConfig = setupStepConfig()
            val formModel = SelectFromListFormModel().apply { selectedOption = null }
            whenever(mockPropertyAddressSearchState.selectPropertyStep).thenReturn(mockSelectPropertyStep)
            whenever(mockSelectPropertyStep.formModelOrNull).thenReturn(formModel)

            // Act
            val result = stepConfig.mode(mockPropertyAddressSearchState)

            // Assert
            assertNull(result)
        }

        @Test
        fun `mode returns COMPLETE when property is selected`() {
            // Arrange
            val stepConfig = setupStepConfig()
            val formModel = SelectFromListFormModel().apply { selectedOption = "1" }
            whenever(mockPropertyAddressSearchState.selectPropertyStep).thenReturn(mockSelectPropertyStep)
            whenever(mockSelectPropertyStep.formModelOrNull).thenReturn(formModel)

            // Act
            val result = stepConfig.mode(mockPropertyAddressSearchState)

            // Assert
            assertEquals(Complete.COMPLETE, result)
        }
    }

    @Nested
    inner class AfterPrimaryValidation {
        @Test
        fun `rejects when no option is selected`() {
            // Arrange
            val stepConfig = setupStepConfig()
            val formModel = SelectFromListFormModel().apply { selectedOption = null }
            val bindingResult = BeanPropertyBindingResult(formModel, "formModel")

            // Act
            stepConfig.afterPrimaryValidation(mockPropertyAddressSearchState, bindingResult)

            // Assert
            assertTrue(bindingResult.hasFieldErrors("selectedOption"))
            assertEquals("joinProperty.selectProperty.error.missing", bindingResult.getFieldError("selectedOption")?.defaultMessage)
        }

        @Test
        fun `accepts a valid selection`() {
            // Arrange
            val stepConfig = setupStepConfig()
            val formModel = SelectFromListFormModel().apply { selectedOption = "1 Example Road, EG1 2AB" }
            val bindingResult = BeanPropertyBindingResult(formModel, "formModel")
            whenever(mockPropertyAddressSearchState.cachedAddresses).thenReturn(
                listOf(
                    AddressDataModel("1 Example Road, EG1 2AB"),
                    AddressDataModel("2 Example Road, EG1 2AB"),
                    AddressDataModel("3 Example Road, EG1 2AB"),
                ),
            )

            // Act
            stepConfig.afterPrimaryValidation(mockPropertyAddressSearchState, bindingResult)

            // Assert
            assertFalse(bindingResult.hasErrors())
        }

        @Test
        fun `rejects an invalid selection`() {
            // Arrange
            val stepConfig = setupStepConfig()
            val formModel = SelectFromListFormModel().apply { selectedOption = "99 Nonexistent Road" }
            val bindingResult = BeanPropertyBindingResult(formModel, "formModel")
            whenever(mockPropertyAddressSearchState.cachedAddresses).thenReturn(
                listOf(
                    AddressDataModel("1 Example Road, EG1 2AB"),
                    AddressDataModel("2 Example Road, EG1 2AB"),
                    AddressDataModel("3 Example Road, EG1 2AB"),
                ),
            )

            // Act
            stepConfig.afterPrimaryValidation(mockPropertyAddressSearchState, bindingResult)

            // Assert
            assertTrue(bindingResult.hasFieldErrors("selectedOption"))
            assertEquals(
                "joinProperty.selectProperty.error.invalidSelection",
                bindingResult.getFieldError("selectedOption")?.defaultMessage,
            )
        }
    }

    private fun setupStepConfig(): SelectPropertyStepConfig {
        val stepConfig = SelectPropertyStepConfig()
        stepConfig.routeSegment = routeSegment
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
