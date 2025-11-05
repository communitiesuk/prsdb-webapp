package uk.gov.communities.prsdb.webapp.journeys

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus
import uk.gov.communities.prsdb.webapp.journeys.builders.StepInitialiser
import uk.gov.communities.prsdb.webapp.journeys.example.steps.Complete

class TaskTests {
    class TestTask(
        private val subJourney: List<StepInitialiser<*, JourneyState, *>> = listOf(),
        var taskComplete: Boolean = false,
        val firstStep: JourneyStep.VisitableStep<*, *, JourneyState> = mock(),
    ) : Task<Complete, JourneyState>() {
        override fun makeSubJourney(
            state: JourneyState,
            entryPoint: Parentage,
        ): List<StepInitialiser<*, JourneyState, *>> = subJourney

        override fun taskCompletionParentage(state: JourneyState): Parentage =
            object : Parentage {
                override fun allowsChild(): Boolean = taskComplete

                override val ancestry: List<JourneyStep<*, *, *>> = listOf()
                override val allowingParentSteps: List<JourneyStep<*, *, *>> = listOf()
                override val potentialParents: List<JourneyStep<*, *, *>> = listOf()
            }

        override fun firstStepInTask(state: JourneyState) = firstStep
    }

    @Test
    fun `getTaskSteps returns the tasks sub journey with an additional exit step`() {
        // Arrange
        val stepInitialisers =
            listOf(
                mock<StepInitialiser<*, JourneyState, *>>(),
                mock<StepInitialiser<*, JourneyState, *>>(),
                mock<StepInitialiser<*, JourneyState, *>>(),
            )
        val task = TestTask(stepInitialisers)

        val nextDestinationLambda = { _: NavigationComplete -> Destination.ExternalUrl("example.com") }
        val state = mock<JourneyState>()

        // Act
        val taskSteps = task.getTaskSteps(state, NoParents()) { nextDestination(nextDestinationLambda) }

        // Assert
        assertEquals(taskSteps.size, stepInitialisers.size + 1)
        taskSteps.forEachIndexed { index, step ->
            val initialiser = stepInitialisers.getOrNull(index)
            if (initialiser != null) {
                assertSame(initialiser, step)
            } else {
                assertEquals(null, step.segment)
                val builtStep = step.build(state, { Destination.ExternalUrl("example.com") })
                assertSame(task.notionalExitStep, builtStep)
            }
        }
    }

    @Test
    fun `when the first step of a task is not reachable, the taskStatus is CANNOT_START`() {
        // Arrange
        val firstStep = mock<JourneyStep.VisitableStep<*, *, JourneyState>>()
        whenever(firstStep.isStepReachable).thenReturn(false)

        val state = mock<JourneyState>()
        val task = initialisedTask(firstStep, state)

        // Act
        val status = task.taskStatus(mock())

        // Assert
        assertEquals(TaskStatus.CANNOT_START, status)
    }

    @Test
    fun `when the first step of a task is reachable and the first step's outcome is null, the taskStatus is NOT_STARTED`() {
        // Arrange
        val firstStep = mock<JourneyStep.VisitableStep<*, *, JourneyState>>()
        whenever(firstStep.isStepReachable).thenReturn(true)
        whenever(firstStep.outcome()).thenReturn(null)

        val state = mock<JourneyState>()
        val task = initialisedTask(firstStep, state)

        // Act
        val status = task.taskStatus(mock())

        // Assert
        assertEquals(TaskStatus.NOT_STARTED, status)
    }

    @Test
    fun `when the first step of a task is complete and the task is not complete, the taskStatus is IN_PROGRESS`() {
        // Arrange
        val firstStep = mock<JourneyStep.VisitableStep<Complete, *, JourneyState>>()
        whenever(firstStep.isStepReachable).thenReturn(true)
        whenever(firstStep.outcome()).thenReturn(Complete.COMPLETE)

        val state = mock<JourneyState>()
        val task = initialisedTask(firstStep, state)

        // Act
        val status = task.taskStatus(mock())

        // Assert
        assertEquals(TaskStatus.IN_PROGRESS, status)
    }

    @Test
    fun `when the task is complete, the taskStatus is COMPLETED`() {
        // Arrange
        val firstStep = mock<JourneyStep.VisitableStep<*, *, JourneyState>>()

        val state = mock<JourneyState>()
        val task = initialisedTask(firstStep, state, true)

        // Act
        val status = task.taskStatus(mock())

        // Assert
        assertEquals(TaskStatus.COMPLETED, status)
    }

    @Test
    fun `notionalExitStep return a Navigational Step with Navigational Config`() {
        // Arrange
        val task = TestTask()

        // Act
        val step = task.notionalExitStep

        // Assert
        assertTrue { step is NavigationalStep }
        assertTrue { step.stepConfig is NavigationalStepConfig }
    }

    private fun initialisedTask(
        firstStep: JourneyStep.VisitableStep<*, *, JourneyState>,
        state: JourneyState,
        taskStatus: Boolean = false,
    ): TestTask {
        val task = TestTask(listOf(), taskStatus, firstStep)
        task
            .getTaskSteps(state, mock()) {
                nextUrl { "example.com" }
            }.forEach { it.build(state) { Destination.ExternalUrl("example.com") } }
        return task
    }
}
