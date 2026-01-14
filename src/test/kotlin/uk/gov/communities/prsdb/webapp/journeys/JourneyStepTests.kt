package uk.gov.communities.prsdb.webapp.journeys

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.validation.BindingResult
import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysFalseValidator
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import kotlin.test.assertEquals
import kotlin.test.assertSame

class JourneyStepTests {
    class TestFormModel : FormModel {
        var field: String = ""
    }

    companion object {
        @JvmStatic
        fun journeyStepProvider(): List<Arguments?> {
            val stepConfig: AbstractStepConfig<TestEnum, TestFormModel, JourneyState> = mock()
            val internalStepConfig: AbstractInternalStepConfig<TestEnum, JourneyState> = mock()
            return listOf(
                Arguments.argumentSet("Requestable Step", JourneyStep.RequestableStep(stepConfig), "stepId"),
                Arguments.argumentSet("Internal Step", JourneyStep.InternalStep(internalStepConfig), null),
            )
        }
    }

    @ParameterizedTest
    @MethodSource("journeyStepProvider")
    fun `step is reachable if its parentage allows it`(
        step: JourneyStep<TestEnum, TestFormModel, JourneyState>,
        routeSegment: String?,
    ) {
        // Arrange
        val parentage: Parentage = mock()
        whenever(parentage.allowsChild()).thenReturn(true)
        step.initialize(
            routeSegment,
            mock(),
            mock(),
            { Destination.ExternalUrl("redirect") },
            parentage,
            { Destination.ExternalUrl("unreachable") },
            false,
        )

        // Act
        val isReachable = step.isStepReachable

        // Assert
        assertTrue(isReachable)
    }

    @ParameterizedTest
    @MethodSource("journeyStepProvider")
    fun `step is not reachable if its parentage does not allow it`(
        step: JourneyStep<TestEnum, TestFormModel, JourneyState>,
        routeSegment: String?,
    ) {
        // Arrange
        val parentage: Parentage = mock()
        whenever(parentage.allowsChild()).thenReturn(false)
        step.initialize(
            routeSegment,
            mock(),
            mock(),
            { Destination.ExternalUrl("redirect") },
            parentage,
            { Destination.ExternalUrl("unreachable") },
            false,
        )

        // Act
        val isReachable = step.isStepReachable

        // Assert
        assertFalse(isReachable)
    }

    @ParameterizedTest
    @MethodSource("journeyStepProvider")
    fun `validateSubmittedData binds valid data to form model with no errors`(
        step: JourneyStep<TestEnum, TestFormModel, JourneyState>,
        routeSegment: String?,
    ) {
        // Arrange
        whenever(step.stepConfig.formModelClass).thenReturn(TestFormModel::class)
        whenever(step.stepConfig.validator).thenReturn(AlwaysTrueValidator())
        val formData = mapOf("field" to "value")
        step.initialize(
            routeSegment,
            mock(),
            mock(),
            { Destination.ExternalUrl("redirect") },
            mock(),
            { Destination.ExternalUrl("unreachable") },
            false,
        )
        whenever(step.stepConfig.enrichSubmittedDataBeforeValidation(anyOrNull(), any())).thenCallRealMethod()
        whenever(step.stepConfig.afterPrimaryValidation(anyOrNull(), any())).thenCallRealMethod()

        // Act
        val bindingResult = step.validateSubmittedData(formData)

        // Assert
        assertFalse(bindingResult.hasErrors())
        val formModel = bindingResult.target as TestFormModel
        assertEquals("value", formModel.field)
    }

    @ParameterizedTest
    @MethodSource("journeyStepProvider")
    fun `validateSubmittedData binds invalid data to form model with errors`(
        step: JourneyStep<TestEnum, TestFormModel, JourneyState>,
        routeSegment: String?,
    ) {
        // Arrange
        whenever(step.stepConfig.validator).thenReturn(AlwaysFalseValidator())
        whenever(step.stepConfig.formModelClass).thenReturn(TestFormModel::class)
        val formData = mapOf("field" to "value")
        step.initialize(
            routeSegment,
            mock(),
            mock(),
            { Destination.ExternalUrl("redirect") },
            mock(),
            { Destination.ExternalUrl("unreachable") },
            false,
        )

        whenever(step.stepConfig.enrichSubmittedDataBeforeValidation(anyOrNull(), any())).thenCallRealMethod()
        whenever(step.stepConfig.afterPrimaryValidation(anyOrNull(), any())).thenCallRealMethod()

        // Act
        val bindingResult = step.validateSubmittedData(formData)

        // Assert
        assertTrue(bindingResult.hasErrors())
        val formModel = bindingResult.target as TestFormModel
        assertEquals("value", formModel.field)
    }

    @ParameterizedTest
    @MethodSource("journeyStepProvider")
    fun `getPageVisitContent adds back link and an empty form model to the content when there's no submitted data`(
        step: JourneyStep<TestEnum, TestFormModel, JourneyState>,
        routeSegment: String?,
    ) {
        // Arrange
        whenever(step.stepConfig.formModelClass).thenReturn(TestFormModel::class)
        step.initialize(
            routeSegment,
            mock(),
            { Destination.ExternalUrl("backLink") },
            { Destination.ExternalUrl("redirect") },
            mock(),
            { Destination.ExternalUrl("unreachable") },
            false,
        )

        whenever(step.stepConfig.resolvePageContent(anyOrNull(), any())).thenCallRealMethod()

        // Act
        val content = step.getPageVisitContent()

        // Assert
        assertEquals("backLink", content["backUrl"])
        assertTrue(content["formModel"] is TestFormModel)
    }

    @ParameterizedTest
    @MethodSource("journeyStepProvider")
    fun `getPageVisitContent adds back link and existing form model to the content when there's submitted data`(
        step: JourneyStep<TestEnum, TestFormModel, JourneyState>,
        routeSegment: String?,
    ) {
        // Arrange
        val existingFormModel = TestFormModel().apply { field = "existingValue" }
        whenever(step.stepConfig.getFormModelFromStateOrNull(anyOrNull())).thenReturn(existingFormModel)
        step.initialize(
            routeSegment,
            mock(),
            { Destination.ExternalUrl("backLink") },
            { Destination.ExternalUrl("redirect") },
            mock(),
            { Destination.ExternalUrl("unreachable") },
            false,
        )
        whenever(step.stepConfig.resolvePageContent(anyOrNull(), any())).thenCallRealMethod()

        // Act
        val content = step.getPageVisitContent()

        // Assert
        assertEquals("backLink", content["backUrl"])
        val formModel = content["formModel"] as TestFormModel
        assertEquals("existingValue", formModel.field)
    }

    @ParameterizedTest
    @MethodSource("journeyStepProvider")
    fun `getInvalidSubmissionContent adds back link and submitted form model with errors`(
        step: JourneyStep<TestEnum, TestFormModel, JourneyState>,
        routeSegment: String?,
    ) {
        // Arrange
        whenever(step.stepConfig.formModelClass).thenReturn(TestFormModel::class)
        step.initialize(
            routeSegment,
            mock(),
            { Destination.ExternalUrl("backLink") },
            { Destination.ExternalUrl("redirect") },
            mock(),
            { Destination.ExternalUrl("unreachable") },
            false,
        )
        val bindingResult: BindingResult = mock()
        whenever(step.stepConfig.resolvePageContent(anyOrNull(), any())).thenCallRealMethod()

        // Act
        val content = step.getInvalidSubmissionContent(bindingResult)

        // Assert
        assertEquals("backLink", content["backUrl"])
        assertSame(bindingResult, content[BindingResult.MODEL_KEY_PREFIX + "formModel"])
    }

    @Test
    fun `submitFormData saves bindingResult target as form data in journey state for a VisitableStep`() {
        // Arrange
        val stepConfig = mock<AbstractStepConfig<TestEnum, TestFormModel, JourneyState>>()
        val step = JourneyStep.RequestableStep(stepConfig)
        whenever(stepConfig.formModelClass).thenReturn(TestFormModel::class)
        whenever(stepConfig.routeSegment).thenReturn("stepId")
        val state = mock<JourneyState>()
        step.initialize(
            "stepId",
            state,
            mock(),
            { Destination.ExternalUrl("redirect") },
            mock(),
            { Destination.ExternalUrl("unreachable") },
            false,
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
    fun `submitFormData saves does nothing for a NotionalStep`() {
        // Arrange
        val internalStepConfig: AbstractInternalStepConfig<TestEnum, JourneyState> = mock()
        val step = JourneyStep.InternalStep(internalStepConfig)
        whenever(step.stepConfig.formModelClass).thenReturn(NoInputFormModel::class)
        whenever(step.stepConfig.routeSegment).thenReturn("stepId")
        val state = mock<JourneyState>()
        step.initialize(
            null,
            state,
            mock(),
            { Destination.ExternalUrl("redirect") },
            mock(),
            { Destination.ExternalUrl("unreachable") },
            false,
        )
        val formModel = TestFormModel().apply { field = "submittedValue" }
        val bindingResult: BindingResult = mock()
        whenever(bindingResult.target).thenReturn(formModel)

        // Act
        step.submitFormData(bindingResult)

        // Assert
        verify(state, times(0)).addStepData(anyOrNull(), anyOrNull())
    }

    @ParameterizedTest
    @MethodSource("journeyStepProvider")
    fun `if the step is accessible, the outcome is the step config's mode`(
        step: JourneyStep<TestEnum, TestFormModel, JourneyState>,
        routeSegment: String?,
    ) {
        // Arrange
        whenever(step.stepConfig.mode(any())).thenReturn(TestEnum.ENUM_VALUE)
        val parentage: Parentage = mock()
        whenever(parentage.allowsChild()).thenReturn(true)

        step.initialize(
            routeSegment,
            mock(),
            mock(),
            { Destination.ExternalUrl("redirect") },
            parentage,
            { Destination.ExternalUrl("unreachable") },
            false,
        )

        // Act
        val outcome = step.outcome

        // Assert
        assertEquals(TestEnum.ENUM_VALUE, outcome)
    }

    @ParameterizedTest
    @MethodSource("journeyStepProvider")
    fun `if the step is not accessible, the outcome is null`(
        step: JourneyStep<TestEnum, TestFormModel, JourneyState>,
        routeSegment: String?,
    ) {
        // Arrange
        whenever(step.stepConfig.mode(any())).thenReturn(TestEnum.ENUM_VALUE)
        val parentage: Parentage = mock()
        whenever(parentage.allowsChild()).thenReturn(false)

        step.initialize(
            routeSegment,
            mock(),
            mock(),
            { Destination.ExternalUrl("redirect") },
            parentage,
            { Destination.ExternalUrl("unreachable") },
            false,
        )

        // Act
        val outcome = step.outcome

        // Assert
        assertNull(outcome)
    }

    @ParameterizedTest
    @MethodSource("journeyStepProvider")
    fun `determine redirect returns the result of the redirectProvider if the step config's mode is not null`(
        step: JourneyStep<TestEnum, TestFormModel, JourneyState>,
        routeSegment: String?,
    ) {
        // Arrange
        whenever(step.stepConfig.mode(any())).thenReturn(TestEnum.ENUM_VALUE)

        val state: JourneyState = mock()
        whenever(state.journeyId).thenReturn("jid123")

        step.initialize(
            routeSegment,
            state,
            mock(),
            { Destination.ExternalUrl("redirect") },
            mock(),
            { Destination.ExternalUrl("unreachable") },
            false,
        )
        whenever(step.stepConfig.beforeChoosingNextDestination(any())).thenCallRealMethod()
        whenever(step.stepConfig.resolveNextDestination(anyOrNull(), any())).thenCallRealMethod()

        // Act
        val redirectDestination = step.getNextDestination()

        // Assert
        assertTrue(redirectDestination is Destination.ExternalUrl)
        with(redirectDestination as Destination.ExternalUrl) {
            assertEquals("redirect", externalUrl)
        }
    }

    @ParameterizedTest
    @MethodSource("journeyStepProvider")
    fun `determine next destination throws an unrecoverable journey exception if the step config's mode is null`(
        journeyStep: JourneyStep<TestEnum, TestFormModel, JourneyState>,
        routeSegment: String?,
    ) {
        // Arrange
        whenever(journeyStep.stepConfig.mode(any())).thenReturn(null)
        whenever(journeyStep.stepConfig.routeSegment).thenReturn("stepId")

        val state: JourneyState = mock()
        whenever(state.journeyId).thenReturn("jid123")

        journeyStep.initialize(
            routeSegment,
            state,
            mock(),
            { Destination.ExternalUrl("redirect") },
            mock(),
            { Destination.ExternalUrl("unreachable") },
            false,
        )

        // Act & Assert
        val exception =
            assertThrows<UnrecoverableJourneyStateException> {
                journeyStep.getNextDestination()
            }

        // Assert
        assertEquals(exception.journeyId, "jid123")
    }

    @ParameterizedTest
    @MethodSource("journeyStepProvider")
    fun `initialize throws if the journey step has already been initialised`(
        step: JourneyStep<TestEnum, TestFormModel, JourneyState>,
        routeSegment: String?,
    ) {
        // Arrange
        val stepConfig = step.stepConfig

        step.initialize(
            routeSegment,
            mock(),
            mock(),
            { Destination.ExternalUrl("redirect") },
            mock(),
            { Destination.ExternalUrl("unreachable") },
            false,
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
                false,
            )
        }
    }

    @Test
    fun `initialising the route segment for a RequestableStep with a null route segment throws`() {
        // Arrange
        val stepConfig = mock<AbstractStepConfig<TestEnum, TestFormModel, JourneyState>>()
        val step = JourneyStep.RequestableStep(stepConfig)

        // Act & Assert
        assertThrows<JourneyInitialisationException> {
            step.initialize(
                null,
                mock(),
                mock(),
                { Destination.ExternalUrl("redirect") },
                mock(),
                { Destination.ExternalUrl("unreachable") },
                false,
            )
        }
    }

    @Test
    fun `initialising the route segment for a RequestableStep sets it on the config`() {
        // Arrange
        val stepConfig = mock<AbstractStepConfig<TestEnum, TestFormModel, JourneyState>>()
        val step = JourneyStep.RequestableStep(stepConfig)

        // Act
        step.initialize(
            "stepId",
            mock(),
            mock(),
            { Destination.ExternalUrl("redirect") },
            mock(),
            { Destination.ExternalUrl("unreachable") },
            false,
        )

        // Assert
        verify(stepConfig).routeSegment = "stepId"
    }

    @Test
    fun `initialising the route segment for an InternalStep with a non-null route segment throws`() {
        // Arrange
        val internalStepConfig: AbstractInternalStepConfig<TestEnum, JourneyState> = mock()
        val step = JourneyStep.InternalStep(internalStepConfig)

        // Act & Assert
        assertThrows<JourneyInitialisationException> {
            step.initialize(
                "stepId",
                mock(),
                mock(),
                { Destination.ExternalUrl("redirect") },
                mock(),
                { Destination.ExternalUrl("unreachable") },
                false,
            )
        }
    }
}
