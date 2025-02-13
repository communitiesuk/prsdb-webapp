package uk.gov.communities.prsdb.webapp.forms

import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
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
        SimpleTaskOne("step1"),
        TwoStepTaskPartOne("step2a"),
        TwoStepTaskPartTwo("step2b"),
        SimpleTaskTwo("step3"),
        MultiPathTaskStart("step4start"),
        MultiPathTaskMainline("step4path1"),
        MultiPathTaskAlternateRoutePartOne("step4path2a"),
        MultiPathTaskAlternateRoutePartTwo("step4path2b"),
    }

    class TestTaskList(
        journey: Journey<TestStepId>,
        journeyDataService: JourneyDataService,
        validator: Validator,
        override val taskList: List<Task<TestStepId>>,
    ) : TaskList<TestStepId>(journey, journeyDataService, validator)

    class TestJourney(
        validator: Validator,
        journeyDataService: JourneyDataService,
        override val initialStepId: TestStepId,
        override val steps: Set<Step<TestStepId>>,
    ) : Journey<TestStepId>(JourneyType.PROPERTY_REGISTRATION, validator, journeyDataService)

    @Mock
    lateinit var mockJourneyDataService: JourneyDataService

    @Mock
    lateinit var validator: Validator

    @BeforeEach
    fun setup() {
        mockJourneyDataService = mock()
        validator = mock()
    }

    @Nested
    inner class MultiPathJourneyTests {
        @BeforeEach
        fun setup() {
            // Ensure form data for each page is never null
            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(
                mutableMapOf(
                    "step1" to mutableMapOf<String, String>(),
                    "step2a" to mutableMapOf<String, String>(),
                    "step2b" to mutableMapOf<String, String>(),
                    "step3" to mutableMapOf<String, String>(),
                    "step4start" to mutableMapOf<String, String>(),
                    "step4path1" to mutableMapOf<String, String>(),
                    "step4path2a" to mutableMapOf<String, String>(),
                    "step4path2b" to mutableMapOf<String, String>(),
                ),
            )
        }

        fun getMultiPathTestTaskListWithMainlineCompleted(useMainline: Boolean): TestTaskList {
            val journey =
                TestJourney(
                    validator,
                    mockJourneyDataService,
                    TestStepId.SimpleTaskOne,
                    setOf(
                        Step(
                            TestStepId.SimpleTaskOne,
                            mock(),
                            isSatisfied = { _, _ -> true },
                            nextAction = { _, _ -> Pair(TestStepId.MultiPathTaskStart, null) },
                        ),
                        Step(
                            TestStepId.MultiPathTaskStart,
                            mock(),
                            isSatisfied = { _, _ -> true },
                            nextAction = {
                                    _,
                                    _,
                                ->
                                Pair(
                                    if (useMainline) TestStepId.MultiPathTaskMainline else TestStepId.MultiPathTaskAlternateRoutePartOne,
                                    null,
                                )
                            },
                        ),
                        Step(
                            TestStepId.MultiPathTaskMainline,
                            mock(),
                            isSatisfied = { _, _ -> true },
                            nextAction = { _, _ -> Pair(TestStepId.TwoStepTaskPartOne, null) },
                        ),
                        Step(
                            TestStepId.MultiPathTaskAlternateRoutePartOne,
                            mock(),
                            isSatisfied = { _, _ -> false },
                            nextAction = { _, _ -> Pair(TestStepId.MultiPathTaskAlternateRoutePartTwo, null) },
                        ),
                        Step(
                            TestStepId.MultiPathTaskAlternateRoutePartTwo,
                            mock(),
                            isSatisfied = { _, _ -> false },
                            nextAction = { _, _ -> Pair(TestStepId.TwoStepTaskPartOne, null) },
                        ),
                        Step(
                            TestStepId.TwoStepTaskPartOne,
                            mock(),
                            isSatisfied = { _, _ -> true },
                            nextAction = { _, _ -> Pair(TestStepId.TwoStepTaskPartTwo, null) },
                        ),
                        Step(
                            TestStepId.TwoStepTaskPartTwo,
                            mock(),
                            isSatisfied = { _, _ -> true },
                            nextAction = { _, _ -> Pair(TestStepId.SimpleTaskTwo, null) },
                        ),
                        Step(
                            TestStepId.SimpleTaskTwo,
                            mock(),
                            isSatisfied = { _, _ -> false },
                        ),
                    ),
                )

            return TestTaskList(
                journey,
                mockJourneyDataService,
                validator,
                listOf(
                    TaskList.Task("task 1", TestStepId.SimpleTaskOne, setOf(TestStepId.SimpleTaskOne)),
                    TaskList.Task(
                        "multi-path task",
                        TestStepId.MultiPathTaskStart,
                        setOf(
                            TestStepId.MultiPathTaskStart,
                            TestStepId.MultiPathTaskMainline,
                            TestStepId.MultiPathTaskAlternateRoutePartOne,
                            TestStepId.MultiPathTaskAlternateRoutePartTwo,
                        ),
                    ),
                    TaskList.Task(
                        "task 2",
                        TestStepId.TwoStepTaskPartOne,
                        setOf(TestStepId.TwoStepTaskPartOne, TestStepId.TwoStepTaskPartTwo),
                    ),
                    TaskList.Task("task 3", TestStepId.SimpleTaskTwo, setOf(TestStepId.SimpleTaskTwo)),
                ),
            )
        }

        @Suppress("ktlint:standard:max-line-length")
        @Test
        fun `when the mainline of a journey is followed and the journey is using the mainline, all filled in tasks tasks show as completed`() {
            // Arrange
            val testTaskList = getMultiPathTestTaskListWithMainlineCompleted(useMainline = true)

            // Act
            val viewModel = testTaskList.getTaskListViewModels()

            // Assert
            assertIterableEquals(
                listOf(
                    "taskList.status.completed",
                    "taskList.status.completed",
                    "taskList.status.completed",
                    "taskList.status.notYetStarted",
                ),
                viewModel.map { it.status.textKey },
            )
        }

        @Suppress("ktlint:standard:max-line-length")
        @Test
        fun `when the mainline of a journey is followed and the journey is using the alternate route, tasks after the bifurcation are unreachable`() {
            // Arrange
            val testTaskList = getMultiPathTestTaskListWithMainlineCompleted(useMainline = false)

            // Act
            val viewModel = testTaskList.getTaskListViewModels()

            // Assert
            assertIterableEquals(
                listOf(
                    "taskList.status.completed",
                    "taskList.status.inProgress",
                    "taskList.status.cannotStartYet",
                    "taskList.status.cannotStartYet",
                ),
                viewModel.map { it.status.textKey },
            )
        }
    }

    @Nested
    inner class SinglePathJourneyTests {
        @BeforeEach
        fun setup() {
            // Ensure form data for each page is never null
            whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(
                mutableMapOf(
                    "step1" to mutableMapOf<String, String>(),
                    "step2a" to mutableMapOf<String, String>(),
                    "step2b" to mutableMapOf<String, String>(),
                    "step3" to mutableMapOf<String, String>(),
                ),
            )
        }

        fun getLinearTestTaskList(
            simpleTaskOneCompleted: Boolean,
            twoStepTaskPartOneCompleted: Boolean,
            twoStepTaskPartTwoCompleted: Boolean,
            simpleTaskTwoCompleted: Boolean,
        ): TestTaskList {
            val journey =
                TestJourney(
                    validator,
                    mockJourneyDataService,
                    TestStepId.SimpleTaskOne,
                    setOf(
                        Step(
                            TestStepId.SimpleTaskOne,
                            mock(),
                            isSatisfied = { _, _ -> simpleTaskOneCompleted },
                            nextAction = { _, _ -> Pair(TestStepId.TwoStepTaskPartOne, null) },
                        ),
                        Step(
                            TestStepId.TwoStepTaskPartOne,
                            mock(),
                            isSatisfied = { _, _ -> twoStepTaskPartOneCompleted },
                            nextAction = { _, _ -> Pair(TestStepId.TwoStepTaskPartTwo, null) },
                        ),
                        Step(
                            TestStepId.TwoStepTaskPartTwo,
                            mock(),
                            isSatisfied = { _, _ -> twoStepTaskPartTwoCompleted },
                            nextAction = { _, _ -> Pair(TestStepId.SimpleTaskTwo, null) },
                        ),
                        Step(
                            TestStepId.SimpleTaskTwo,
                            mock(),
                            isSatisfied = { _, _ -> simpleTaskTwoCompleted },
                        ),
                    ),
                )

            return TestTaskList(
                journey,
                mockJourneyDataService,
                validator,
                listOf(
                    TaskList.Task("task 1", TestStepId.SimpleTaskOne, setOf(TestStepId.SimpleTaskOne)),
                    TaskList.Task(
                        "task 2",
                        TestStepId.TwoStepTaskPartOne,
                        setOf(TestStepId.TwoStepTaskPartOne, TestStepId.TwoStepTaskPartTwo),
                    ),
                    TaskList.Task("task 3", TestStepId.SimpleTaskTwo, setOf(TestStepId.SimpleTaskTwo)),
                ),
            )
        }

        @Test
        fun `when a task is exactly finished, the next task is marked not yet started`() {
            // Arrange
            val testTaskList =
                getLinearTestTaskList(
                    simpleTaskOneCompleted = true,
                    twoStepTaskPartOneCompleted = false,
                    twoStepTaskPartTwoCompleted = false,
                    simpleTaskTwoCompleted = false,
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
            val testTaskList =
                getLinearTestTaskList(
                    simpleTaskOneCompleted = true,
                    twoStepTaskPartOneCompleted = true,
                    twoStepTaskPartTwoCompleted = false,
                    simpleTaskTwoCompleted = false,
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
        fun `when all steps are completed for a task , it is marked as complete`() {
            // Arrange
            val testTaskList =
                getLinearTestTaskList(
                    simpleTaskOneCompleted = true,
                    twoStepTaskPartOneCompleted = true,
                    twoStepTaskPartTwoCompleted = true,
                    simpleTaskTwoCompleted = true,
                )

            // Act
            val viewModel = testTaskList.getTaskListViewModels()

            // Assert
            assertIterableEquals(
                listOf("taskList.status.completed", "taskList.status.completed", "taskList.status.completed"),
                viewModel.map { it.status.textKey },
            )
        }
    }
}
