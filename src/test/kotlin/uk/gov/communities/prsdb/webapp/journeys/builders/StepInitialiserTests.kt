package uk.gov.communities.prsdb.webapp.journeys.builders

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.NoParents
import uk.gov.communities.prsdb.webapp.journeys.StepInitialisationStage
import uk.gov.communities.prsdb.webapp.journeys.TestEnum
import kotlin.test.assertEquals

class StepInitialiserTests {
    @Test
    fun `a stepBuilder will not accept a step that has already been initialised`() {
        // Arrange
        val stepMock = mock<JourneyStep.RequestableStep<TestEnum, *, JourneyState>>()
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
        builder.parents { NoParents() }

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
            anyOrNull(),
        )
    }

    @Test
    fun `if no backUrlOverride is set, the step's backUrlOverride remains null`() {
        // Arrange
        val stepMock = mockInitialisableStep()
        val builder = StepInitialiser("test", stepMock)
        builder.nextUrl { "next" }
        builder.parents { NoParents() }

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
            anyOrNull(),
        )
    }

    @Test
    fun `no next destination can be set after a next url`() {
        // Arrange
        val builder = StepInitialiser("test", mockInitialisableStep())
        builder.nextUrl { "url1" }

        // Act & Assert
        assertThrows<JourneyInitialisationException> { builder.nextUrl { "url2" } }
        assertThrows<JourneyInitialisationException> { builder.nextStep { mock() } }
        assertThrows<JourneyInitialisationException> { builder.nextDestination { mock() } }
    }

    @Test
    fun `no next destination can be set after a next step`() {
        // Arrange
        val builder = StepInitialiser("test", mockInitialisableStep())
        builder.nextStep { mock() }

        // Act & Assert
        assertThrows<JourneyInitialisationException> { builder.nextUrl { "url2" } }
        assertThrows<JourneyInitialisationException> { builder.nextStep { mock() } }
        assertThrows<JourneyInitialisationException> { builder.nextDestination { mock() } }
    }

    @Test
    fun `a redirectToUrl is passed to the step when built`() {
        // Arrange
        val stepMock = mockInitialisableStep()
        val builder = StepInitialiser("test", stepMock)
        val redirectLambda = { _: TestEnum -> "expectedRedirect" }
        builder.nextUrl(redirectLambda)
        builder.parents { NoParents() }

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
        val nextStepMock = mock<JourneyStep.RequestableStep<TestEnum, *, JourneyState>>()
        whenever(nextStepMock.routeSegment).thenReturn(nextStepSegment)
        whenever(nextStepMock.currentJourneyId).thenReturn("journeyId")

        val builder = StepInitialiser("test", stepMock)
        builder.nextStep { _: TestEnum -> nextStepMock }
        builder.parents { NoParents() }

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
            anyOrNull(),
        )
        val result = lambdaCaptor.firstValue(TestEnum.ENUM_VALUE)
        with(result as Destination.VisitableStep) {
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
            anyOrNull(),
        )
    }

    @Test
    fun `if no parentage is set, building the step throws an exception`() {
        // Arrange
        val stepMock = mockInitialisableStep()
        val builder = StepInitialiser("test", stepMock)
        builder.nextUrl { "next" }

        // Act & Assert
        assertThrows<JourneyInitialisationException> {
            builder.build(mock(), mock())
        }
    }

    @Test
    fun `initialStep sets a step to have no parents`() {
        // Arrange
        val stepMock = mockInitialisableStep()
        val builder = StepInitialiser("test", stepMock)
        val parentage = NoParents()
        builder.initialStep()
        builder.nextUrl { "next" }

        // Act
        builder.build(mock(), mock())

        // Assert
        verify(stepMock).initialize(
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            any<NoParents>(),
            anyOrNull(),
            anyOrNull(),
        )
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
        builder.parents { NoParents() }

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
        builder.parents { NoParents() }
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
            anyOrNull(),
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
        builder.parents { NoParents() }

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
            anyOrNull(),
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

    @Test
    fun `if no additional content providers are set, an empty Map is passed`() {
        // Arrange
        val stepMock = mockInitialisableStep()
        val builder = StepInitialiser("test", stepMock)
        builder.nextUrl { "next" }
        builder.parents { NoParents() }

        // Act
        builder.build(mock(), mock())

        // Assert
        val mapCaptor = argumentCaptor<() -> Map<String, Any>>()
        verify(stepMock).initialize(
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            mapCaptor.capture(),
        )
        val additionalContent = mapCaptor.firstValue()
        assertEquals(emptyMap(), additionalContent)
    }

    @Test
    fun `a single additional content provider is passed to the step when built`() {
        // Arrange
        val stepMock = mockInitialisableStep()
        val builder = StepInitialiser("test", stepMock)
        val expectedKey = "testKey"
        val expectedValue = "testValue"
        builder.withAdditionalContentProperty { expectedKey to expectedValue }
        builder.nextUrl { "next" }
        builder.parents { NoParents() }

        // Act
        builder.build(mock(), mock())

        // Assert
        val mapCaptor = argumentCaptor<() -> Map<String, Any>>()
        verify(stepMock).initialize(
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            mapCaptor.capture(),
        )
        val additionalContent = mapCaptor.firstValue()
        assertEquals(mapOf(expectedKey to expectedValue), additionalContent)
    }

    @Test
    fun `multiple additional content providers are combined into a single Map`() {
        // Arrange
        val stepMock = mockInitialisableStep()
        val builder = StepInitialiser("test", stepMock)
        val firstKey = "firstKey"
        val firstValue = "firstValue"
        val secondKey = "secondKey"
        val secondValue = 177
        builder.withAdditionalContentProperty { firstKey to firstValue }
        builder.withAdditionalContentProperty { secondKey to secondValue }
        builder.nextUrl { "next" }
        builder.parents { NoParents() }

        // Act
        builder.build(mock(), mock())

        // Assert
        val mapCaptor = argumentCaptor<() -> Map<String, Any>>()
        verify(stepMock).initialize(
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            mapCaptor.capture(),
        )
        val additionalContent = mapCaptor.firstValue()
        assertEquals(mapOf(firstKey to firstValue, secondKey to secondValue), additionalContent)
    }

    private fun mockInitialisableStep() =
        mock<JourneyStep.RequestableStep<TestEnum, *, JourneyState>>().apply {
            whenever(
                this.initialisationStage,
            ).thenReturn(
                StepInitialisationStage.UNINITIALISED,
                StepInitialisationStage.PARTIALLY_INITIALISED,
                StepInitialisationStage.FULLY_INITIALISED,
            )
        }
}
