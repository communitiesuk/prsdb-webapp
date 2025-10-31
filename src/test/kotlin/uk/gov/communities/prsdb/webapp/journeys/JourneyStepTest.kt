package uk.gov.communities.prsdb.webapp.journeys

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.validation.BindingResult
import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.journeys.example.Destination
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysFalseValidator
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import kotlin.test.assertEquals
import kotlin.test.assertSame

class JourneyStepTest {
    class TestFormModel : FormModel {
        var field: String = ""
    }

    @Test
    fun `step is reachable if its parentage allows it`() {
        // Arrange
        val step = JourneyStep<TestEnum, TestFormModel, JourneyState>(mock())
        val parentage: Parentage = mock()
        whenever(parentage.allowsChild()).thenReturn(true)
        step.initialize(
            "stepId",
            mock(),
            mock(),
            { Destination.ExternalUrl("redirect") },
            parentage,
            { Destination.ExternalUrl("unreachable") },
        )

        // Act
        val isReachable = step.isStepReachable

        // Assert
        assertTrue(isReachable)
    }

    @Test
    fun `step is not reachable if its parentage does not allow it`() {
        // Arrange
        val step = JourneyStep<TestEnum, TestFormModel, JourneyState>(mock())
        val parentage: Parentage = mock()
        whenever(parentage.allowsChild()).thenReturn(false)
        step.initialize(
            "stepId",
            mock(),
            mock(),
            { Destination.ExternalUrl("redirect") },
            parentage,
            { Destination.ExternalUrl("unreachable") },
        )

        // Act
        val isReachable = step.isStepReachable

        // Assert
        assertFalse(isReachable)
    }

    @Test
    fun `validateSubmittedData binds valid data to form model with no errors`() {
        // Arrange
        val step = JourneyStep<TestEnum, TestFormModel, JourneyState>(mock())
        whenever(step.stepConfig.formModelClass).thenReturn(TestFormModel::class)
        whenever(step.stepConfig.validator).thenReturn(AlwaysTrueValidator())
        val formData = mapOf("field" to "value")
        step.initialize(
            "stepId",
            mock(),
            mock(),
            { Destination.ExternalUrl("redirect") },
            mock(),
            { Destination.ExternalUrl("unreachable") },
        )

        // Act
        val bindingResult = step.validateSubmittedData(formData)

        // Assert
        assertFalse(bindingResult.hasErrors())
        val formModel = bindingResult.target as TestFormModel
        assertEquals("value", formModel.field)
    }

    @Test
    fun `validateSubmittedData binds invalid data to form model with errors`() {
        // Arrange
        val step = JourneyStep<TestEnum, TestFormModel, JourneyState>(mock())
        whenever(step.stepConfig.validator).thenReturn(AlwaysFalseValidator())
        whenever(step.stepConfig.formModelClass).thenReturn(TestFormModel::class)
        val formData = mapOf("field" to "value")
        step.initialize(
            "stepId",
            mock(),
            mock(),
            { Destination.ExternalUrl("redirect") },
            mock(),
            { Destination.ExternalUrl("unreachable") },
        )

        // Act
        val bindingResult = step.validateSubmittedData(formData)

        // Assert
        assertTrue(bindingResult.hasErrors())
        val formModel = bindingResult.target as TestFormModel
        assertEquals("value", formModel.field)
    }

    @Test
    fun `getPageVisitContent adds back link and an empty form model to the content when there's no submitted data`() {
        // Arrange
        val step = JourneyStep<TestEnum, TestFormModel, JourneyState>(mock())
        whenever(step.stepConfig.formModelClass).thenReturn(TestFormModel::class)
        step.initialize(
            "stepId",
            mock(),
            { "backLink" },
            { Destination.ExternalUrl("redirect") },
            mock(),
            { Destination.ExternalUrl("unreachable") },
        )

        // Act
        val content = step.getPageVisitContent()

        // Assert
        assertEquals("backLink", content["backUrl"])
        assertTrue(content["formModel"] is TestFormModel)
    }

    @Test
    fun `getPageVisitContent adds back link and existing form model to the content when there's submitted data`() {
        // Arrange
        val step = JourneyStep<TestEnum, TestFormModel, JourneyState>(mock())
        val existingFormModel = TestFormModel().apply { field = "existingValue" }
        whenever(step.stepConfig.getFormModelFromState(anyOrNull())).thenReturn(existingFormModel)
        step.initialize(
            "stepId",
            mock(),
            { "backLink" },
            { Destination.ExternalUrl("redirect") },
            mock(),
            { Destination.ExternalUrl("unreachable") },
        )

        // Act
        val content = step.getPageVisitContent()

        // Assert
        assertEquals("backLink", content["backUrl"])
        val formModel = content["formModel"] as TestFormModel
        assertEquals("existingValue", formModel.field)
    }

    @Test
    fun `getInvalidSubmissionContent adds back link and submitted form model with errors`() {
        // Arrange
        val step = JourneyStep<TestEnum, TestFormModel, JourneyState>(mock())
        whenever(step.stepConfig.formModelClass).thenReturn(TestFormModel::class)
        step.initialize(
            "stepId",
            mock(),
            { "backLink" },
            { Destination.ExternalUrl("redirect") },
            mock(),
            { Destination.ExternalUrl("unreachable") },
        )
        val bindingResult: BindingResult = mock()

        // Act
        val content = step.getInvalidSubmissionContent(bindingResult)

        // Assert
        assertEquals("backLink", content["backUrl"])
        assertSame(bindingResult, content[BindingResult.MODEL_KEY_PREFIX + "formModel"])
    }

    @Test
    fun `submitFormData saves bindingResult target as form data in journey state`() {
        // Arrange
        val step = JourneyStep<TestEnum, TestFormModel, JourneyState>(mock())
        whenever(step.stepConfig.formModelClass).thenReturn(TestFormModel::class)
        whenever(step.stepConfig.routeSegment).thenReturn("stepId")
        val state = mock<JourneyState>()
        step.initialize(
            "stepId",
            state,
            mock(),
            { Destination.ExternalUrl("redirect") },
            mock(),
            { Destination.ExternalUrl("unreachable") },
        )
        val formModel = TestFormModel().apply { field = "submittedValue" }
        val bindingResult: BindingResult = mock()
        whenever(bindingResult.target).thenReturn(formModel)

        // Act
        step.submitFormData(bindingResult)

        // Assert
        verify(state).addStepData("stepId", formModel.toPageData())
    }

    @Test
    fun `if the step is accessible, the outcome is the step config's mode`() {
        // Arrange
        val step = JourneyStep<TestEnum, TestFormModel, JourneyState>(mock())
        whenever(step.stepConfig.mode(any())).thenReturn(TestEnum.ENUM_VALUE)
        val parentage: Parentage = mock()
        whenever(parentage.allowsChild()).thenReturn(true)

        step.initialize(
            "stepId",
            mock(),
            mock(),
            { Destination.ExternalUrl("redirect") },
            parentage,
            { Destination.ExternalUrl("unreachable") },
        )

        // Act
        val outcome = step.outcome()

        // Assert
        assertEquals(TestEnum.ENUM_VALUE, outcome)
    }

    @Test
    fun `if the step is not accessible, the outcome is null`() {
        // Arrange
        val step = JourneyStep<TestEnum, TestFormModel, JourneyState>(mock())
        whenever(step.stepConfig.mode(any())).thenReturn(TestEnum.ENUM_VALUE)
        val parentage: Parentage = mock()
        whenever(parentage.allowsChild()).thenReturn(false)

        step.initialize(
            "stepId",
            mock(),
            mock(),
            { Destination.ExternalUrl("redirect") },
            parentage,
            { Destination.ExternalUrl("unreachable") },
        )

        // Act
        val outcome = step.outcome()

        // Assert
        assertNull(outcome)
    }

    @Test
    fun `determine redirect returns the result of the redirectProvider if the step config's mode is not null`() {
        // Arrange
        val step = JourneyStep<TestEnum, TestFormModel, JourneyState>(mock())
        whenever(step.stepConfig.mode(any())).thenReturn(TestEnum.ENUM_VALUE)

        val state: JourneyState = mock()
        whenever(state.journeyId).thenReturn("jid123")

        step.initialize(
            "stepId",
            state,
            mock(),
            { Destination.ExternalUrl("redirect") },
            mock(),
            { Destination.ExternalUrl("unreachable") },
        )

        // Act
        val redirectDestination = step.determineNextDestination()

        // Assert
        assertTrue(redirectDestination is Destination.ExternalUrl)
        with(redirectDestination as Destination.ExternalUrl) {
            assertEquals("redirect", externalUrl)
        }
    }

    @Test
    fun `determine redirect returns the route segment if the step config's mode is null`() {
        // Arrange
        val journeyStep = JourneyStep<TestEnum, TestFormModel, JourneyState>(mock())
        whenever(journeyStep.stepConfig.mode(any())).thenReturn(null)
        whenever(journeyStep.stepConfig.routeSegment).thenReturn("stepId")

        val state: JourneyState = mock()
        whenever(state.journeyId).thenReturn("jid123")

        journeyStep.initialize(
            "stepId",
            state,
            mock(),
            { Destination.ExternalUrl("redirect") },
            mock(),
            { Destination.ExternalUrl("unreachable") },
        )

        // Act
        val redirectDestination = journeyStep.determineNextDestination()

        // Assert
        assertTrue(redirectDestination is Destination.Step)
        with(redirectDestination as Destination.Step) {
            assertEquals("stepId", step.routeSegment)
            assertEquals("jid123", journeyId)
        }
    }

    @Test
    fun `initialize throws if the journey step has already been initialised`() {
        // Arrange
        val stepConfig: AbstractStepConfig<TestEnum, TestFormModel, JourneyState> = mock()
        val step = JourneyStep(stepConfig)

        whenever(stepConfig.isRouteSegmentInitialised()).thenReturn(false)

        step.initialize(
            "stepId",
            mock(),
            mock(),
            { Destination.ExternalUrl("redirect") },
            mock(),
            { Destination.ExternalUrl("unreachable") },
        )
        whenever(stepConfig.isRouteSegmentInitialised()).thenReturn(true)

        // Act & Assert
        assertThrows<JourneyInitialisationException> {
            step.initialize(
                "stepId",
                mock(),
                mock(),
                { Destination.ExternalUrl("redirect") },
                mock(),
                { Destination.ExternalUrl("unreachable") },
            )
        }
    }
}
