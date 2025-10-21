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

class AbstractInnerStepTest {
    class TestFormModel : FormModel {
        var field: String? = null
        var otherField: Long? = null
    }

    class TestStepConfig : AbstractStepConfig<TestEnum, TestFormModel, DynamicJourneyState>() {
        override fun getStepSpecificContent(state: DynamicJourneyState): Map<String, Any?> = mapOf()

        override fun chooseTemplate(state: DynamicJourneyState): String = "template"

        override val formModelClass = TestFormModel::class

        override fun isSubClassInitialised(): Boolean = true

        override fun mode(state: DynamicJourneyState): TestEnum = TestEnum.ENUM_VALUE
    }

    @Test
    fun getFormModelFromState() {
        val step = TestStepConfig()
        val state: DynamicJourneyState = mock()
        val routeSegment = "test-segment"
        step.routeSegment = routeSegment

        // Test when there is data in the state
        whenever(state.getStepData(routeSegment)).thenReturn(mapOf("field" to "value", "otherField" to "123"))

        Validation.buildDefaultValidatorFactory().use { validatorFactory ->
            val validator = SpringValidatorAdapter(validatorFactory.validator)
            step.validator = validator

            val formModel = step.getFormModelFromState(state)
            assertNotNull(formModel)
            assertEquals("value", formModel?.field)
            assertEquals(123L, formModel?.otherField)
        }
    }

    @Test
    fun isRouteSegmentInitialised() {
        val step = TestStepConfig()
        assertFalse(step.isRouteSegmentInitialised())
        step.routeSegment = "testSegment"
        assertTrue(step.isRouteSegmentInitialised())
    }
}
