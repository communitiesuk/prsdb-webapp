package uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.controllers.JoinPropertyController.Companion.JOIN_PROPERTY_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.states.PropertyAddressSearchState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectFromListFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
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

    @Test
    fun `getStepSpecificContent returns expected content`() {
        // Arrange
        val stepConfig = setupStepConfig()

        // Act
        val content = stepConfig.getStepSpecificContent(mockPropertyAddressSearchState)

        // Assert
        assertEquals("joinProperty.selectProperty.heading", content["fieldSetHeading"])
        assertEquals("SW9 0HD", content["postcode"])
        assertEquals("9", content["houseNameOrNumber"])
        assertEquals(3, content["propertyCount"])
        assertEquals("$JOIN_PROPERTY_ROUTE/${FindPropertyStep.ROUTE_SEGMENT}", content["searchAgainUrl"])
        assertEquals("$JOIN_PROPERTY_ROUTE/${FindPropertyByPrnStep.ROUTE_SEGMENT}", content["prnLookupUrl"])

        @Suppress("UNCHECKED_CAST")
        val options = content["options"] as List<RadiosButtonViewModel<String>>
        assertEquals(3, options.size)
        assertTrue(options[0].value.contains("Flat 1"))
        assertTrue(options[1].value.contains("Flat 2"))
        assertTrue(options[2].value.contains("9 Example Street"))
    }

    @Test
    fun `chooseTemplate returns selectPropertyForm`() {
        // Arrange
        val stepConfig = setupStepConfig()

        // Act
        val template = stepConfig.chooseTemplate(mockPropertyAddressSearchState)

        // Assert
        assertEquals("forms/selectPropertyForm", template)
    }

    private fun setupStepConfig(): SelectPropertyStepConfig {
        val stepConfig = SelectPropertyStepConfig()
        stepConfig.routeSegment = routeSegment
        stepConfig.validator = AlwaysTrueValidator()
        return stepConfig
    }
}
