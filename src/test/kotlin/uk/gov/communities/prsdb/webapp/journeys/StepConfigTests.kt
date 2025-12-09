package uk.gov.communities.prsdb.webapp.journeys

import jakarta.validation.Validation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.validation.beanvalidation.SpringValidatorAdapter
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel

class StepConfigTests {
    class TestFormModel : FormModel {
        var field: String? = null
        var otherField: Long? = null
    }

    class TestStepConfig : AbstractStepConfig<TestEnum, TestFormModel, JourneyState>() {
        override fun getStepSpecificContent(state: JourneyState): Map<String, Any?> = mapOf()

        override fun chooseTemplate(state: JourneyState): String = "template"

        override val formModelClass = TestFormModel::class

        override fun isSubClassInitialised(): Boolean = true

        override fun mode(state: JourneyState): TestEnum = TestEnum.ENUM_VALUE
    }

    @Test
    fun `getFormModelFromState returns the data stored for that route segment from state as the form model`() {
        val step = TestStepConfig()
        val state: JourneyState = mock()
        val routeSegment = "test-segment"
        step.routeSegment = routeSegment

        // Test when there is data in the state
        whenever(state.getStepData(routeSegment)).thenReturn(mapOf("field" to "value", "otherField" to "123"))

        Validation.buildDefaultValidatorFactory().use { validatorFactory ->
            val validator = SpringValidatorAdapter(validatorFactory.validator)
            step.validator = validator

            val formModel = step.getFormModelFromStateOrNull(state)
            assertNotNull(formModel)
            assertEquals("value", formModel?.field)
            assertEquals(123L, formModel?.otherField)
        }
    }

    @Test
    fun `isRouteSegmentInitialised returns true if and only if routeSegment has been initialised`() {
        val step = TestStepConfig()
        assertFalse(step.isRouteSegmentInitialised())
        step.routeSegment = "testSegment"
        assertTrue(step.isRouteSegmentInitialised())
    }
}
