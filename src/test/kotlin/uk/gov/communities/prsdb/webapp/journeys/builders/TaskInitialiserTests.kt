package uk.gov.communities.prsdb.webapp.journeys.builders

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.exceptions.JourneyInitialisationException
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.NavigationComplete
import uk.gov.communities.prsdb.webapp.journeys.NavigationalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.NoParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.TestEnum

class TaskInitialiserTests {
    @Test
    fun `once a nextStep is set, the destinationProvider cannot be set again`() {
        // Arrange
        val builder = TaskInitialiser(mockTask(), mock())
        builder.parents { mock() }

        // Act
        builder.nextStep { mock<JourneyStep.RequestableStep<TestEnum, *, JourneyState>>() }

        // Assert
        assertThrows<JourneyInitialisationException> {
            builder.nextStep { mock<JourneyStep.RequestableStep<TestEnum, *, JourneyState>>() }
        }
        assertThrows<JourneyInitialisationException> {
            builder.nextDestination { Destination.ExternalUrl("url") }
        }
    }

    @Test
    fun `once a nextDestination is set, the destinationProvider cannot be set again`() {
        // Arrange
        val builder = TaskInitialiser(mockTask(), mock())
        builder.parents { mock() }

        // Act
        builder.nextDestination { Destination.ExternalUrl("url") }

        // Assert
        assertThrows<JourneyInitialisationException> {
            builder.nextStep { mock<JourneyStep.RequestableStep<TestEnum, *, JourneyState>>() }
        }
        assertThrows<JourneyInitialisationException> {
            builder.nextDestination { Destination.ExternalUrl("url") }
        }
    }

    @Test
    fun `a nextStep is passed to the task's exit when built`() {
        // Arrange
        val taskMock = mockTask()

        val nextStepMock = mock<JourneyStep.RequestableStep<TestEnum, *, JourneyState>>()
        val nextStepSegment = "nextStepSegment"
        whenever(nextStepMock.routeSegment).thenReturn(nextStepSegment)
        whenever(nextStepMock.currentJourneyId).thenReturn("journeyId")

        val builder = TaskInitialiser(taskMock, mock())
        builder.parents { mock() }
        builder.nextStep { _: NavigationComplete -> nextStepMock }

        // Act
        builder.build()

        // Assert
        val initCaptor = argumentCaptor<StepInitialiser<NavigationalStepConfig, *, NavigationComplete>.() -> Unit>()
        verify(taskMock).getTaskSubJourneyBuilder(
            anyOrNull(),
            initCaptor.capture(),
        )

        val initialiser = mock<StepInitialiser<NavigationalStepConfig, JourneyState, NavigationComplete>>()
        initCaptor.firstValue.invoke(initialiser)

        val lambdaCaptor = argumentCaptor<(mode: NavigationComplete) -> Destination>()
        verify(initialiser).nextDestination(lambdaCaptor.capture())

        val destination = lambdaCaptor.firstValue.invoke(NavigationComplete.COMPLETE)
        assertTrue(destination is Destination.VisitableStep)
        with(destination as Destination.VisitableStep) {
            assertEquals(nextStepSegment, step.routeSegment)
            assertEquals("journeyId", step.currentJourneyId)
        }
    }

    @Test
    fun `a nextDestination is passed to the task's exit when built`() {
        // Arrange
        val taskMock = mockTask()

        val nextStepSegment = "nextStepSegment"

        val builder = TaskInitialiser(taskMock, mock())
        builder.parents { mock() }
        val initiationDestination = Destination.ExternalUrl(nextStepSegment)
        builder.nextDestination { _: NavigationComplete -> initiationDestination }

        // Act
        builder.build()

        // Assert
        val initCaptor = argumentCaptor<StepInitialiser<NavigationalStepConfig, *, NavigationComplete>.() -> Unit>()
        verify(taskMock).getTaskSubJourneyBuilder(
            anyOrNull(),
            initCaptor.capture(),
        )

        val initialiser = mock<StepInitialiser<NavigationalStepConfig, JourneyState, NavigationComplete>>()
        initCaptor.firstValue.invoke(initialiser)

        val lambdaCaptor = argumentCaptor<(mode: NavigationComplete) -> Destination>()
        verify(initialiser).nextDestination(lambdaCaptor.capture())

        val finalDestination = lambdaCaptor.firstValue.invoke(NavigationComplete.COMPLETE)
        assertSame(initiationDestination, finalDestination)
    }

    @Test
    fun `if no destinationProvider is set, an exception is thrown when built`() {
        // Arrange
        val taskMock = mockTask()

        val builder = TaskInitialiser(taskMock, mock())
        builder.parents { mock() }

        // Act & Assert
        assertThrows<JourneyInitialisationException> {
            builder.build()
        }
    }

    @Test
    fun `a parentage cannot be set more than once`() {
        // Arrange
        val taskMock = mockTask()
        val builder = TaskInitialiser(taskMock, mock())
        builder.parents { NoParents() }

        // Act & Assert
        assertThrows<JourneyInitialisationException> { builder.parents { NoParents() } }
    }

    @Test
    fun `a parentage is passed to the task when mapped to step initialisers`() {
        // Arrange
        val taskMock = mockTask()
        val builder = TaskInitialiser(taskMock, mock())
        val parentageProvider = { NoParents() }
        builder.nextDestination { mock() }
        builder.parents(parentageProvider)

        val internalBuilder = mock<SubJourneyBuilder<JourneyState>>()
        whenever(taskMock.getTaskSubJourneyBuilder(anyOrNull(), anyOrNull())).thenReturn(internalBuilder)

        // Act
        builder.build()

        // Assert
        val firstStepInitCaptor = argumentCaptor<ConfigurableElement<*>.() -> Unit>()
        verify(internalBuilder).configureFirst(firstStepInitCaptor.capture())

        val mockStep = mock<StepInitialiser<*, *, TestEnum>>()
        firstStepInitCaptor.firstValue.invoke(mockStep)
        verify(mockStep).parents(eq(parentageProvider))
    }

    @Test
    fun `if no parentage is set, buildSteps throws an exception`() {
        // Arrange
        val taskMock = mockTask()
        val builder = TaskInitialiser(taskMock, mock())
        builder.nextDestination { mock() }

        val internalBuilder = mock<SubJourneyBuilder<JourneyState>>()
        whenever(taskMock.getTaskSubJourneyBuilder(anyOrNull(), anyOrNull())).thenReturn(internalBuilder)

        // Act
        builder.build()

        // Assert
        val captor = argumentCaptor<ConfigurableElement<*>.() -> Unit>()
        verify(internalBuilder).configureFirst(captor.capture())
        assertThrows<JourneyInitialisationException> {
            val mockStep = mock<StepInitialiser<*, *, TestEnum>>()
            captor.firstValue.invoke(mockStep)
        }
    }

    @Test
    fun `a single additional content provider is passed to the taskSubJourney when built`() {
        // Arrange
        val taskMock = mockTask()
        val subJourneyBuilderMock = mock<SubJourneyBuilder<JourneyState>>()
        whenever(taskMock.getTaskSubJourneyBuilder(anyOrNull(), anyOrNull())).thenReturn(subJourneyBuilderMock)

        val builder = TaskInitialiser(taskMock, mock())
        val expectedKey = "testKey"
        val expectedValue = "testValue"
        builder.withAdditionalContentProperty { expectedKey to expectedValue }
        builder.nextDestination { mock() }
        builder.parents { NoParents() }

        // Act
        builder.build()

        // Assert
        val configCaptor = argumentCaptor<ConfigurableElement<*>.() -> Unit>()
        verify(subJourneyBuilderMock).configure(configCaptor.capture())

        val mockConfigurable = mock<ConfigurableElement<NavigationComplete>>()
        configCaptor.firstValue.invoke(mockConfigurable)

        val contentCaptor = argumentCaptor<() -> Pair<String, Any>>()
        verify(mockConfigurable).withAdditionalContentProperty(contentCaptor.capture())

        val content = contentCaptor.firstValue()
        assertEquals(expectedKey to expectedValue, content)
    }

    @Test
    fun `multiple additional content providers are passed to the taskSubJourney when built`() {
        // Arrange
        val taskMock = mockTask()
        val subJourneyBuilderMock = mock<SubJourneyBuilder<JourneyState>>()
        whenever(
            taskMock.getTaskSubJourneyBuilder(
                anyOrNull(),
                anyOrNull(),
            ),
        ).thenReturn(subJourneyBuilderMock)

        val builder = TaskInitialiser(taskMock, mock())
        val firstKey = "firstKey"
        val firstValue = "firstValue"
        val secondKey = "secondKey"
        val secondValue = 177
        builder.withAdditionalContentProperty { firstKey to firstValue }
        builder.withAdditionalContentProperty { secondKey to secondValue }
        builder.nextDestination { mock() }
        builder.parents { NoParents() }

        // Act
        builder.build()

        // Assert
        val configCaptor = argumentCaptor<ConfigurableElement<*>.() -> Unit>()
        verify(subJourneyBuilderMock).configure(configCaptor.capture())

        val mockConfigurable = mock<ConfigurableElement<NavigationComplete>>()
        configCaptor.firstValue.invoke(mockConfigurable)

        val contentCaptor = argumentCaptor<() -> Pair<String, Any>>()
        verify(mockConfigurable, times(2)).withAdditionalContentProperty(contentCaptor.capture())

        val allContent = contentCaptor.allValues.map { it() }
        assertTrue(allContent.contains(firstKey to firstValue))
        assertTrue(allContent.contains(secondKey to secondValue))
    }

    private fun mockTask(): Task<JourneyState> =
        mock<Task<JourneyState>>().apply {
            whenever(
                getTaskSubJourneyBuilder(
                    anyOrNull(),
                    anyOrNull(),
                ),
            ).thenReturn(mock<SubJourneyBuilder<JourneyState>>())
        }
}
