package uk.gov.communities.prsdb.webapp.journeys

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.MockedConstruction
import org.mockito.Mockito.mockConstruction
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus
import uk.gov.communities.prsdb.webapp.journeys.builders.SubJourneyBuilder
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete

class TaskTests {
    class TestTask : Task<JourneyState>() {
        override fun makeSubJourney(state: JourneyState) = subJourney(state) { }
    }

    private lateinit var subJourneyConstruction: MockedConstruction<SubJourneyBuilder<*>>
    private val firstStepMock = mock<JourneyStep.RequestableStep<*, *, JourneyState>>()
    private val exitStepMock = mock<SubjourneyExitStep>()

    @BeforeEach
    fun setup() {
        // Mock construction of SubJourneyBuilder to capture the init lambda
        subJourneyConstruction =
            mockConstruction(SubJourneyBuilder::class.java) { mock, _ ->
                whenever(mock.firstStep).thenReturn(firstStepMock)
                whenever(mock.exitStep).thenReturn(exitStepMock)
            }
    }

    @AfterEach
    fun teardown() {
        subJourneyConstruction.close()
    }

    @Test
    fun `getTaskSubJourneyBuilder inits the sub journey builder and returns the steps from it`() {
        // Arrange
        val task = TestTask()

        val nextDestinationLambda = { _: SubjourneyComplete -> Destination.ExternalUrl("example.com") }
        val state = mock<JourneyState>()
        val parent = NoParents()

        // Act
        val subJourneyBuilder =
            task.getTaskSubJourneyBuilder(state) {
                parents { parent }
                nextDestination(nextDestinationLambda)
            }

        // Assert
        assertSame(subJourneyConstruction.constructed().first(), subJourneyBuilder)
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
        whenever(firstStepMock.outcome).thenReturn(null)

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
        whenever(firstStepMock.outcome).thenReturn(Complete.COMPLETE)

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
    fun `exitStep returns a TaskExitStep from the internal task builder`() {
        // Arrange
        val task = initialisedTask()

        // Act
        val step = task.exitStep

        // Assert
        assertSame(exitStepMock, step)
    }

    private fun initialisedTask(): TestTask {
        val task = TestTask()
        task.getTaskSubJourneyBuilder(mock()) {
            nextUrl { "example.com" }
        }
        return task
    }
}
