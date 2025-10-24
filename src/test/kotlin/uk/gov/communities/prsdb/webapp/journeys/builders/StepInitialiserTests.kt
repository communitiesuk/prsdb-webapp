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
import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.NoParents
import uk.gov.communities.prsdb.webapp.journeys.Parentage
import uk.gov.communities.prsdb.webapp.journeys.StepInitialisationStage
import uk.gov.communities.prsdb.webapp.journeys.TestEnum
import uk.gov.communities.prsdb.webapp.journeys.example.Destination
import kotlin.test.assertEquals

class StepInitialiserTests {
    @Test
    fun `a stepBuilder will not accept a step that has already been initialised`() {
        // Arrange
        val stepMock = mock<JourneyStep<TestEnum, *, JourneyState>>()
        whenever(stepMock.initialisationStage).thenReturn(StepInitialisationStage.FULLY_INITIALISED)

        // Act & Assert
        assertThrows<JourneyInitialisationException> { StepInitialiser("test", stepMock) }
    }

    @Test
    fun `a back url override cannot be set more than once`() {
        // Arrange
        val builder = StepInitialiser("test", mockInitialisableStep())

        // Act
        builder.backUrl { "url1" }

        // Assert
        assertThrows<JourneyInitialisationException> { builder.backUrl { "url2" } }
    }

    @Test
    fun `a backUrlOverride is passed to the step when built`() {
        // Arrange
        val expectedBackUrl = "expectedBackUrl"
        val stepMock = mockInitialisableStep()
        val builder = StepInitialiser("test", stepMock)
        val backUrlLambda = { expectedBackUrl }
        builder.backUrl(backUrlLambda)
        builder.nextUrl { "next" }

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
        val builder = StepInitialiser("test", stepMock)
        builder.nextUrl { "next" }

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
        val builder = StepInitialiser("test", mockInitialisableStep())
        builder.nextUrl { "url1" }

        // Act & Assert
        assertThrows<JourneyInitialisationException> { builder.nextUrl { "url2" } }
    }

    @Test
    fun `a redirectToStep cannot be set if a redirect url has been set`() {
        // Arrange
        val builder = StepInitialiser("test", mockInitialisableStep())
        builder.nextUrl { "url1" }

        // Act & Assert
        assertThrows<JourneyInitialisationException> { builder.nextStep { mock() } }
    }

    @Test
    fun `a redirectToUrl cannot be set if a redirect step has been set`() {
        // Arrange
        val builder = StepInitialiser("test", mockInitialisableStep())
        builder.nextStep { mock() }

        // Act & Assert
        assertThrows<JourneyInitialisationException> { builder.nextUrl { "url2" } }
    }

    @Test
    fun `a redirectToUrl is passed to the step when built`() {
        // Arrange
        val stepMock = mockInitialisableStep()
        val builder = StepInitialiser("test", stepMock)
        val redirectLambda = { _: TestEnum -> "expectedRedirect" }
        builder.nextUrl(redirectLambda)

        // Act
        builder.build(mock(), mock())

        // Assert
        val lambdaCaptor = argumentCaptor<(TestEnum) -> Destination>()
        verify(stepMock).initialize(
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            lambdaCaptor.capture(),
            anyOrNull(),
            anyOrNull(),
        )
        val result = lambdaCaptor.firstValue(TestEnum.ENUM_VALUE)
        with(result as Destination.ExternalUrl) {
            assertEquals("expectedRedirect", externalUrl)
        }
    }

    @Test
    fun `a redirectToStep is passed to the step when built`() {
        // Arrange
        val stepMock = mockInitialisableStep()
        val nextStepSegment = "nextStepSegment"
        val nextStepMock = mock<JourneyStep<TestEnum, *, JourneyState>>()
        whenever(nextStepMock.routeSegment).thenReturn(nextStepSegment)

        val builder = StepInitialiser("test", stepMock)
        builder.nextStep { _: TestEnum -> nextStepMock }

        // Act
        builder.build(mock(), mock())

        // Assert
        val lambdaCaptor = argumentCaptor<(TestEnum) -> Destination>()
        verify(stepMock).initialize(
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            lambdaCaptor.capture(),
            anyOrNull(),
            anyOrNull(),
        )
        val result = lambdaCaptor.firstValue(TestEnum.ENUM_VALUE)
        with(result as Destination.Step) {
            assertEquals(nextStepMock, step)
        }
    }

    @Test
    fun `a step must have a redirect destination`() {
        // Arrange
        val builder = StepInitialiser("test", mockInitialisableStep())

        // Act & Assert
        assertThrows<JourneyInitialisationException> { builder.build(mock(), mock()) }
    }

    @Test
    fun `a parentage cannot be set more than once`() {
        // Arrange
        val builder = StepInitialiser("test", mockInitialisableStep())
        builder.parents { NoParents() }

        // Act & Assert
        assertThrows<JourneyInitialisationException> { builder.parents { NoParents() } }
    }

    @Test
    fun `a parentage is passed to the step when built`() {
        // Arrange
        val stepMock = mockInitialisableStep()
        val builder = StepInitialiser("test", stepMock)
        val parentage = NoParents()
        builder.parents { parentage }
        builder.nextUrl { "next" }

        // Act
        builder.build(mock(), mock())

        // Assert
        verify(stepMock).initialize(
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            eq(parentage),
            anyOrNull(),
        )
    }

    @Test
    fun `if no parentage is set, the step's parentage is NoParents`() {
        // Arrange
        val stepMock = mockInitialisableStep()
        val builder = StepInitialiser("test", stepMock)
        builder.nextUrl { "next" }

        // Act
        builder.build(mock(), mock())

        // Assert by capturing the lambda and invoking it
        val parentageCaptor = argumentCaptor<Parentage>()
        verify(stepMock).initialize(
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            parentageCaptor.capture(),
            anyOrNull(),
        )
        val result = parentageCaptor.firstValue
        assertTrue(result is NoParents)
    }

    @Test
    fun `additional configuration cannot be set more than once`() {
        // Arrange
        val builder = StepInitialiser("test", mockInitialisableStep())
        builder.stepSpecificInitialisation {}

        // Act & Assert
        assertThrows<JourneyInitialisationException> { builder.stepSpecificInitialisation {} }
    }

    @Test
    fun `additional configuration is applied to the step when built`() {
        // Arrange
        val stepMock = mockInitialisableStep()
        whenever(stepMock.stepConfig).thenReturn(mock())
        val builder = StepInitialiser("test", stepMock)
        var additionalConfigApplied = false
        builder.stepSpecificInitialisation {
            if (this === stepMock.stepConfig) {
                additionalConfigApplied = true
            }
        }
        builder.nextUrl { "next" }

        // Act
        builder.build(mock(), mock())

        // Assert
        assertTrue(additionalConfigApplied)
    }

    @Test
    fun `a step unreachable url cannot be set more than once`() {
        // Arrange
        val builder = StepInitialiser("test", mockInitialisableStep())
        builder.unreachableStepUrl { "url1" }

        // Act & Assert
        assertThrows<JourneyInitialisationException> { builder.unreachableStepUrl { "url2" } }
    }

    @Test
    fun `a step unreachable url is passed to the step when built, even if there is a default set`() {
        // Arrange
        val stepMock = mockInitialisableStep()
        val builder = StepInitialiser("test", stepMock)
        val stepUnreachableRedirectLambda = { "expectedRedirect" }
        builder.unreachableStepUrl(stepUnreachableRedirectLambda)
        builder.nextUrl { "next" }
        val defaultUnreachableRedirectLambda = { Destination.ExternalUrl("defaultRedirect") }

        // Act
        builder.build(mock(), defaultUnreachableRedirectLambda)

        // Assert
        val captor = argumentCaptor<() -> Destination>()
        verify(stepMock).initialize(
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            captor.capture(),
        )
        val destination = captor.firstValue()
        with(destination as Destination.ExternalUrl) {
            assertEquals("expectedRedirect", externalUrl)
        }
    }

    @Test
    fun `if no step unreachable redirect is set, the default redirect is passed to the step instead`() {
        // Arrange
        val stepMock = mockInitialisableStep()
        val builder = StepInitialiser("test", stepMock)
        builder.nextUrl { "next" }
        val defaultUnreachableRedirectLambda = { Destination.ExternalUrl("expectedRedirect") }

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
        val builder = StepInitialiser("test", mockInitialisableStep())
        builder.nextUrl { "next" }

        // Act & Assert
        assertThrows<JourneyInitialisationException> { builder.build(mock(), null) }
    }

    private fun mockInitialisableStep() =
        mock<JourneyStep<TestEnum, *, JourneyState>>().apply {
            whenever(
                this.initialisationStage,
            ).thenReturn(
                StepInitialisationStage.UNINITIALISED,
                StepInitialisationStage.PARTIALLY_INITIALISED,
                StepInitialisationStage.FULLY_INITIALISED,
            )
        }
}
