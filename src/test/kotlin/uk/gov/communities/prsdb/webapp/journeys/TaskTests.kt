package uk.gov.communities.prsdb.webapp.journeys

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.MockedConstruction
import org.mockito.Mockito.mockConstruction
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus
import uk.gov.communities.prsdb.webapp.journeys.builders.StepInitialiser
import uk.gov.communities.prsdb.webapp.journeys.builders.SubJourneyBuilder
import uk.gov.communities.prsdb.webapp.journeys.example.steps.Complete

class TaskTests {
    class TestTask : Task<JourneyState>() {
        override fun makeSubJourney(state: JourneyState): List<StepInitialiser<*, JourneyState, *>> = subJourney(state) { }
    }

    lateinit var subJourneyConstruction: MockedConstruction<SubJourneyBuilder<*>>
    private val firstStepMock = mock<JourneyStep.VisitableStep<*, *, JourneyState>>()
    private val exitStepMock = mock<NavigationalStep>()
    private val stepsMock = listOf<StepInitialiser<*, JourneyState, *>>()

    @BeforeEach
    fun setup() {
        // Mock construction of SubJourneyBuilder to capture the init lambda
        subJourneyConstruction =
            mockConstruction(SubJourneyBuilder::class.java) { mock, context ->
                whenever(mock.firstStep).thenReturn(firstStepMock)
                whenever(mock.exitStep).thenReturn(exitStepMock)
                whenever(mock.getSteps(anyOrNull())).thenReturn(stepsMock)
            }
    }

    @AfterEach
    fun teardown() {
        subJourneyConstruction.close()
    }

    @Test
    fun `getTaskSteps inits the sub journey builder and returns the steps from it`() {
        // Arrange
        val task = TestTask()

        val nextDestinationLambda = { _: NavigationComplete -> Destination.ExternalUrl("example.com") }
        val state = mock<JourneyState>()
        val parent = NoParents()

        // Act
        val taskSteps = task.getTaskSteps(state, parent) { nextDestination(nextDestinationLambda) }

        // Assert
        val subJourneyBuilder = subJourneyConstruction.constructed().first()
        verify(subJourneyBuilder).subJourneyParent(eq(parent))
        verify(subJourneyBuilder).getSteps(anyOrNull())
    }

    @Test
    fun `when the first step of a task is not reachable, the taskStatus is CANNOT_START`() {
        // Arrange
        whenever(firstStepMock.isStepReachable).thenReturn(false)

        val task = initialisedTask()

        // Act
        val status = task.taskStatus()

        // Assert
        assertEquals(TaskStatus.CANNOT_START, status)
    }

    @Test
    fun `when the first step of a task is reachable and the first step's outcome is null, the taskStatus is NOT_STARTED`() {
        // Arrange
        whenever(firstStepMock.isStepReachable).thenReturn(true)
        whenever(firstStepMock.outcome()).thenReturn(null)

        val task = initialisedTask()

        // Act
        val status = task.taskStatus()

        // Assert
        assertEquals(TaskStatus.NOT_STARTED, status)
    }

    @Test
    fun `when the first step of a task is complete and the task is not complete, the taskStatus is IN_PROGRESS`() {
        // Arrange
        whenever(firstStepMock.isStepReachable).thenReturn(true)
        whenever(firstStepMock.outcome()).thenReturn(Complete.COMPLETE)

        val task = initialisedTask()

        // Act
        val status = task.taskStatus()

        // Assert
        assertEquals(TaskStatus.IN_PROGRESS, status)
    }

    @Test
    fun `when the task is complete, the taskStatus is COMPLETED`() {
        // Arrange
        whenever(exitStepMock.isStepReachable).thenReturn(true)
        val task = initialisedTask()

        // Act
        val status = task.taskStatus()

        // Assert
        assertEquals(TaskStatus.COMPLETED, status)
    }

    @Test
    fun `notionalExitStep return a Navigational Step from the internal task builder`() {
        // Arrange
        val task = initialisedTask()

        // Act
        val step = task.notionalExitStep

        // Assert
        assertSame(exitStepMock, step)
    }

    private fun initialisedTask(): TestTask {
        val task = TestTask()
        task
            .getTaskSteps(mock(), mock()) {
                nextUrl { "example.com" }
            }
        return task
    }
}
