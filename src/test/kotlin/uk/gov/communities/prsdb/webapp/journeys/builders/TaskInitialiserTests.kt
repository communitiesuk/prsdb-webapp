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
    fun `once a redirectToStep is set, the destinationProvider cannot be set again`() {
        // Arrange
        val builder = TaskInitialiser(mock<Task<JourneyState>>())
        builder.parents { mock() }

        // Act
        builder.redirectToStep { mock<JourneyStep.RoutedStep<TestEnum, *, JourneyState>>() }

        // Assert
        assertThrows<JourneyInitialisationException> {
            builder.redirectToStep { mock<JourneyStep.RoutedStep<TestEnum, *, JourneyState>>() }
        }
        assertThrows<JourneyInitialisationException> {
            builder.redirectToDestination { Destination.ExternalUrl("url") }
        }
    }

    @Test
    fun `once a redirectToDestination is set, the destinationProvider cannot be set again`() {
        // Arrange
        val builder = TaskInitialiser(mock<Task<JourneyState>>())
        builder.parents { mock() }

        // Act
        builder.redirectToDestination { Destination.ExternalUrl("url") }

        // Assert
        assertThrows<JourneyInitialisationException> {
            builder.redirectToStep { mock<JourneyStep.RoutedStep<TestEnum, *, JourneyState>>() }
        }
        assertThrows<JourneyInitialisationException> {
            builder.redirectToDestination { Destination.ExternalUrl("url") }
        }
    }

    @Test
    fun `a redirectToStep is passed to the task's exit when mapped to step initialisers`() {
        // Arrange
        val taskMock = mock<Task<JourneyState>>()

        val nextStepMock = mock<JourneyStep.RoutedStep<TestEnum, *, JourneyState>>()
        val nextStepSegment = "nextStepSegment"
        whenever(nextStepMock.routeSegment).thenReturn(nextStepSegment)
        whenever(nextStepMock.currentJourneyId).thenReturn("journeyId")

        val builder = TaskInitialiser(taskMock)
        builder.parents { mock() }
        builder.redirectToStep { _: NavigationComplete -> nextStepMock }

        // Act
        builder.mapToStepInitialisers(mock())

        // Assert
        val initCaptor = argumentCaptor<StepInitialiser<NavigationalStepConfig, *, NavigationComplete>.() -> Unit>()
        verify(taskMock).getTaskSteps(
            anyOrNull(),
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
    fun `a redirectToDestination is passed to the task's exit when mapped to step initialisers`() {
        // Arrange
        val taskMock = mock<Task<JourneyState>>()

        val nextStepSegment = "nextStepSegment"

        val builder = TaskInitialiser(taskMock)
        builder.parents { mock() }
        val initiationDestination = Destination.ExternalUrl(nextStepSegment)
        builder.redirectToDestination { _: NavigationComplete -> initiationDestination }

        // Act
        builder.mapToStepInitialisers(mock())

        // Assert
        val initCaptor = argumentCaptor<StepInitialiser<NavigationalStepConfig, *, NavigationComplete>.() -> Unit>()
        verify(taskMock).getTaskSteps(
            anyOrNull(),
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
    fun `if no destinationProvider is set, an exception is thrown when mapping to step initialisers`() {
        // Arrange
        val taskMock = mock<Task<JourneyState>>()

        val builder = TaskInitialiser(taskMock)
        builder.parents { mock() }

        // Act & Assert
        assertThrows<JourneyInitialisationException> {
            builder.mapToStepInitialisers(mock())
        }
    }

    @Test
    fun `a parentage cannot be set more than once`() {
        // Arrange
        val taskMock = mock<Task<JourneyState>>()
        val builder = TaskInitialiser(taskMock)
        builder.parents { NoParents() }

        // Act & Assert
        assertThrows<JourneyInitialisationException> { builder.parents { NoParents() } }
    }

    @Test
    fun `a parentage is passed to the task when mapped to step initialisers`() {
        // Arrange
        val taskMock = mock<Task<JourneyState>>()
        val builder = TaskInitialiser(taskMock)
        val parentage = NoParents()
        builder.redirectToDestination { mock() }
        builder.parents { parentage }

        // Act
        builder.mapToStepInitialisers(mock())

        // Assert
        verify(taskMock).getTaskSteps(
            anyOrNull(),
            eq(parentage),
            anyOrNull(),
        )
    }

    @Test
    fun `if no parentage is set, mapToStepInitialisers throws an exception`() {
        // Arrange
        val taskMock = mock<Task<JourneyState>>()
        val builder = TaskInitialiser(taskMock)
        builder.redirectToDestination { mock() }

        // Act & Assert
        assertThrows<JourneyInitialisationException> {
            builder.mapToStepInitialisers(mock())
        }
    }
}
