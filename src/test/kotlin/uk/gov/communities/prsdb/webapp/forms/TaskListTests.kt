package uk.gov.communities.prsdb.webapp.forms

import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.isNull
import org.mockito.kotlin.whenever
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.forms.journeys.Journey
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.forms.steps.StepId
import uk.gov.communities.prsdb.webapp.forms.tasks.TaskList
import uk.gov.communities.prsdb.webapp.services.JourneyDataService
import kotlin.test.Test

class TaskListTests {
    enum class TestStepId(
        override val urlPathSegment: String,
    ) : StepId {
        StepOne("step1"),
        StepTwo("step2"),
        StepThree("step3"),
        StepFour("step4"),
    }

    class TestTaskList(
        journey: Journey<TestStepId>,
        journeyDataService: JourneyDataService,
        validator: Validator,
    ) : TaskList<TestStepId>(journey, journeyDataService, validator) {
        override val taskList: List<Task<TestStepId>>
            get() =
                listOf(
                    Task("task 1", TestStepId.StepOne, TestStepId.StepTwo),
                    Task("task 2", TestStepId.StepTwo, TestStepId.StepFour),
                    Task("task 3", TestStepId.StepFour, null),
                )
    }

    @Mock
    lateinit var mockJourneyDataService: JourneyDataService

    @Mock
    lateinit var mockJourney: Journey<TestStepId>

    @Mock
    lateinit var validator: Validator

    @BeforeEach
    fun setup() {
        mockJourneyDataService = mock()
        mockJourney = mock()
        validator = mock()

        // Ensure form data for each page is never null
        whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(
            mutableMapOf(
                "step1" to mutableMapOf<String, String>(),
                "step2" to mutableMapOf<String, String>(),
                "step3" to mutableMapOf<String, String>(),
                "step4" to mutableMapOf<String, String>(),
            ),
        )
    }

    private fun setUpStepsWithStatus(
        stepOneCompleted: Boolean,
        stepTwoCompleted: Boolean,
        stepThreeCompleted: Boolean,
        stepFourCompleted: Boolean,
    ) {
        whenever(mockJourney.steps).thenReturn(
            setOf(
                Step(
                    TestStepId.StepOne,
                    mock(),
                    isSatisfied = { _, _ -> stepOneCompleted },
                    nextAction = { _, _ -> Pair(TestStepId.StepTwo, null) },
                ),
                Step(
                    TestStepId.StepTwo,
                    mock(),
                    isSatisfied = { _, _ -> stepTwoCompleted },
                    nextAction = { _, _ -> Pair(TestStepId.StepThree, null) },
                ),
                Step(
                    TestStepId.StepThree,
                    mock(),
                    isSatisfied = { _, _ -> stepThreeCompleted },
                    nextAction = { _, _ -> Pair(TestStepId.StepFour, null) },
                ),
                Step(
                    TestStepId.StepFour,
                    mock(),
                    isSatisfied = { _, _ -> stepFourCompleted },
                ),
            ),
        )

        whenever(mockJourney.isStepReachable(any(), any(), isNull())).thenAnswer { invocation ->
            val stepArgument = invocation.arguments[1] as Step<TestStepId>
            when (stepArgument.id) {
                TestStepId.StepOne -> true
                TestStepId.StepTwo -> stepOneCompleted
                TestStepId.StepThree -> stepTwoCompleted
                TestStepId.StepFour -> stepThreeCompleted
            }
        }
    }

    @Nested
    inner class GetStepIdTests {
        @Test
        fun `when a task is exactly finished, the next task is marked not yet started`() {
            // Arrange
            setUpStepsWithStatus(
                stepOneCompleted = true,
                stepTwoCompleted = false,
                stepThreeCompleted = false,
                stepFourCompleted = false,
            )

            val testTaskList =
                TestTaskList(
                    mockJourney,
                    mockJourneyDataService,
                    validator,
                )

            // Act
            val viewModel = testTaskList.getTaskListViewModels()

            // Assert
            assertIterableEquals(
                listOf("taskList.status.completed", "taskList.status.notYetStarted", "taskList.status.cannotStartYet"),
                viewModel.map { it.status.textKey },
            )
        }

        @Test
        fun `when a task is partially completed, it is marked in progress`() {
            // Arrange
            setUpStepsWithStatus(
                stepOneCompleted = true,
                stepTwoCompleted = true,
                stepThreeCompleted = false,
                stepFourCompleted = false,
            )

            val testTaskList =
                TestTaskList(
                    mockJourney,
                    mockJourneyDataService,
                    validator,
                )

            // Act
            val viewModel = testTaskList.getTaskListViewModels()

            // Assert
            assertIterableEquals(
                listOf("taskList.status.completed", "taskList.status.inProgress", "taskList.status.cannotStartYet"),
                viewModel.map { it.status.textKey },
            )
        }

        @Test
        fun `when a task with a null completion step is completed, it is not marked as `() {
            // Arrange
            setUpStepsWithStatus(
                stepOneCompleted = true,
                stepTwoCompleted = true,
                stepThreeCompleted = true,
                stepFourCompleted = true,
            )

            val testTaskList =
                TestTaskList(
                    mockJourney,
                    mockJourneyDataService,
                    validator,
                )

            // Act
            val viewModel = testTaskList.getTaskListViewModels()

            // Assert
            assertIterableEquals(
                listOf("taskList.status.completed", "taskList.status.completed", "taskList.status.notYetStarted"),
                viewModel.map { it.status.textKey },
            )
        }
    }
}
