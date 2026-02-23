package uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.states.PropertyAddressSearchState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectFromListFormModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class SelectPropertyStepConfigTests {
    @Mock
    lateinit var mockPropertyAddressSearchState: PropertyAddressSearchState

    @Mock
    lateinit var mockSelectPropertyStep: SelectPropertyStep

    val routeSegment = SelectPropertyStep.ROUTE_SEGMENT

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

    private fun setupStepConfig(): SelectPropertyStepConfig {
        val stepConfig = SelectPropertyStepConfig()
        stepConfig.routeSegment = routeSegment
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
