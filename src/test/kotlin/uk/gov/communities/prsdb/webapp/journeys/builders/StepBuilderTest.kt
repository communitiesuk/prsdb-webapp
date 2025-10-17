package uk.gov.communities.prsdb.webapp.journeys.builders

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.exceptions.JourneyBuilderException
import uk.gov.communities.prsdb.webapp.journeys.DynamicJourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.NoParents
import uk.gov.communities.prsdb.webapp.journeys.Parentage
import uk.gov.communities.prsdb.webapp.journeys.StepInitialisationStage
import uk.gov.communities.prsdb.webapp.journeys.TestEnum
import kotlin.test.assertEquals

class StepBuilderTest {
    @Test
    fun `a stepBuilder will not accept a step that has already been initialised`() {
        // Arrange
        val stepMock = mock<JourneyStep<TestEnum, *, DynamicJourneyState>>()
        whenever(stepMock.initialisationStage).thenReturn(StepInitialisationStage.FULLY_INITIALISED)

        // Act & Assert
        assertThrows<JourneyBuilderException> { StepBuilder("test", stepMock) }
    }

    @Test
    fun `a back url override cannot be set more than once`() {
        // Arrange
        val builder = StepBuilder("test", mockInitialisableStep())

        // Act
        builder.backUrl { "url1" }

        // Assert
        assertThrows<JourneyBuilderException> { builder.backUrl { "url2" } }
    }

    @Test
    fun `a backUrlOverride is passed to the step when built`() {
        // Arrange
        val expectedBackUrl = "expectedBackUrl"
        val stepMock = mockInitialisableStep()
        val builder = StepBuilder("test", stepMock)
        val backUrlLambda = { expectedBackUrl }
        builder.backUrl(backUrlLambda)
        builder.redirectToUrl { "next" }

        // Act
        builder.build(mock(), mock())

        // Assert
        verify(stepMock).initialize(
            anyOrNull(),
            anyOrNull(),
            eq(backUrlLambda),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
        )
    }

    @Test
    fun `if no backUrlOverride is set, the step's backUrlOverride remains null`() {
        // Arrange
        val stepMock = mockInitialisableStep()
        val builder = StepBuilder("test", stepMock)
        builder.redirectToUrl { "next" }

        // Act
        builder.build(mock(), mock())

        // Assert
        verify(stepMock).initialize(
            anyOrNull(),
            anyOrNull(),
            eq(null),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
        )
    }

    @Test
    fun `a redirect destination cannot be set more than once`() {
        // Arrange
        val builder = StepBuilder("test", mockInitialisableStep())
        builder.redirectToUrl { "url1" }

        // Act & Assert
        assertThrows<JourneyBuilderException> { builder.redirectToUrl { "url2" } }
    }

    @Test
    fun `a redirectToStep cannot be set if a redirect url has been set`() {
        // Arrange
        val builder = StepBuilder("test", mockInitialisableStep())
        builder.redirectToUrl { "url1" }

        // Act & Assert
        assertThrows<JourneyBuilderException> { builder.redirectToStep { mock() } }
    }

    @Test
    fun `a redirectToUrl cannot be set if a redirect step has been set`() {
        // Arrange
        val builder = StepBuilder("test", mockInitialisableStep())
        builder.redirectToStep { mock() }

        // Act & Assert
        assertThrows<JourneyBuilderException> { builder.redirectToUrl { "url2" } }
    }

    @Test
    fun `a redirectToUrl is passed to the step when built`() {
        // Arrange
        val stepMock = mockInitialisableStep()
        val builder = StepBuilder("test", stepMock)
        val redirectLambda = { _: TestEnum -> "expectedRedirect" }
        builder.redirectToUrl(redirectLambda)

        // Act
        builder.build(mock(), mock())

        // Assert
        verify(stepMock).initialize(
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            eq(redirectLambda),
            anyOrNull(),
            anyOrNull(),
        )
    }

    @Test
    fun `a redirectToStep is passed to the step when built`() {
        // Arrange
        val stepMock = mockInitialisableStep()
        val nextStepSegment = "nextStepSegment"
        val nextStepMock = mock<JourneyStep<TestEnum, *, DynamicJourneyState>>()
        whenever(nextStepMock.routeSegment).thenReturn(nextStepSegment)

        val builder = StepBuilder("test", stepMock)
        builder.redirectToStep { _: TestEnum -> nextStepMock }

        // Act
        builder.build(mock(), mock())

        // Assert
        val lambdaCaptor = argumentCaptor<(TestEnum) -> String>()
        verify(stepMock).initialize(
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            lambdaCaptor.capture(),
            anyOrNull(),
            anyOrNull(),
        )
        val result = lambdaCaptor.firstValue(TestEnum.ENUM_VALUE)
        assertEquals(nextStepSegment, result)
    }

    @Test
    fun `a step must have a redirect destination`() {
        // Arrange
        val builder = StepBuilder("test", mockInitialisableStep())

        // Act & Assert
        assertThrows<JourneyBuilderException> { builder.build(mock(), mock()) }
    }

    @Test
    fun `a parentage cannot be set more than once`() {
        // Arrange
        val builder = StepBuilder("test", mockInitialisableStep())
        builder.parents { NoParents() }

        // Act & Assert
        assertThrows<JourneyBuilderException> { builder.parents { NoParents() } }
    }

    @Test
    fun `a parentage is passed to the step when built`() {
        // Arrange
        val stepMock = mockInitialisableStep()
        val builder = StepBuilder("test", stepMock)
        val parentage = NoParents()
        val parentageLambda = { parentage }
        builder.parents(parentageLambda)
        builder.redirectToUrl { "next" }

        // Act
        builder.build(mock(), mock())

        // Assert
        verify(stepMock).initialize(
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            eq(parentageLambda),
            anyOrNull(),
        )
    }

    @Test
    fun `if no parentage is set, the step's parentage is NoParents`() {
        // Arrange
        val stepMock = mockInitialisableStep()
        val builder = StepBuilder("test", stepMock)
        builder.redirectToUrl { "next" }

        // Act
        builder.build(mock(), mock())

        // Assert by capturing the lambda and invoking it
        val parentageCaptor = argumentCaptor<() -> Parentage>()
        verify(stepMock).initialize(
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            parentageCaptor.capture(),
            anyOrNull(),
        )
        val result = parentageCaptor.firstValue()
        assertTrue(result is NoParents)
    }

    @Test
    fun `additional configuration cannot be set more than once`() {
        // Arrange
        val builder = StepBuilder("test", mockInitialisableStep())
        builder.stepSpecificInitialisation {}

        // Act & Assert
        assertThrows<JourneyBuilderException> { builder.stepSpecificInitialisation {} }
    }

    @Test
    fun `additional configuration is applied to the step when built`() {
        // Arrange
        val stepMock = mockInitialisableStep()
        whenever(stepMock.innerStep).thenReturn(mock())
        val builder = StepBuilder("test", stepMock)
        var additionalConfigApplied = false
        builder.stepSpecificInitialisation {
            if (this === stepMock.innerStep) {
                additionalConfigApplied = true
            }
        }
        builder.redirectToUrl { "next" }

        // Act
        builder.build(mock(), mock())

        // Assert
        assertTrue(additionalConfigApplied)
    }

    @Test
    fun `a step unreachable redirect cannot be set more than once`() {
        // Arrange
        val builder = StepBuilder("test", mockInitialisableStep())
        builder.unreachableStepRedirect { "url1" }

        // Act & Assert
        assertThrows<JourneyBuilderException> { builder.unreachableStepRedirect { "url2" } }
    }

    @Test
    fun `a step unreachable redirect is passed to the step when built, even if there is a default set`() {
        // Arrange
        val stepMock = mockInitialisableStep()
        val builder = StepBuilder("test", stepMock)
        val stepUnreachableRedirectLambda = { "expectedRedirect" }
        builder.unreachableStepRedirect(stepUnreachableRedirectLambda)
        builder.redirectToUrl { "next" }
        val defaultUnreachableRedirectLambda = { "defaultRedirect" }

        // Act
        builder.build(mock(), defaultUnreachableRedirectLambda)

        // Assert
        verify(stepMock).initialize(
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            eq(stepUnreachableRedirectLambda),
        )
    }

    @Test
    fun `if no step unreachable redirect is set, the default redirect is passed to the step instead`() {
        // Arrange
        val stepMock = mockInitialisableStep()
        val builder = StepBuilder("test", stepMock)
        builder.redirectToUrl { "next" }
        val defaultUnreachableRedirectLambda = { "expectedRedirect" }

        // Act
        builder.build(mock(), defaultUnreachableRedirectLambda)

        // Assert
        verify(stepMock).initialize(
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            eq(defaultUnreachableRedirectLambda),
        )
    }

    @Test
    fun `either a step unreachable redirect or a default unreachable step must be set`() {
        // Arrange
        val builder = StepBuilder("test", mockInitialisableStep())
        builder.redirectToUrl { "next" }

        // Act & Assert
        assertThrows<JourneyBuilderException> { builder.build(mock(), null) }
    }

    private fun mockInitialisableStep() =
        mock<JourneyStep<TestEnum, *, DynamicJourneyState>>().apply {
            whenever(
                this.initialisationStage,
            ).thenReturn(
                StepInitialisationStage.UNINITIALISED,
                StepInitialisationStage.PARTIALLY_INITIALISED,
                StepInitialisationStage.FULLY_INITIALISED,
            )
        }
}
