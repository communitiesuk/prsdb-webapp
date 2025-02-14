package uk.gov.communities.prsdb.webapp.forms

import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus
import uk.gov.communities.prsdb.webapp.forms.journeys.Journey
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneySection
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyTask
import uk.gov.communities.prsdb.webapp.forms.steps.Step
import uk.gov.communities.prsdb.webapp.forms.steps.StepId
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

    class TestJourney(
        validator: Validator,
        journeyDataService: JourneyDataService,
        override val initialStepId: TestStepId,
        override val sections: List<JourneySection<TestStepId>>,
    ) : Journey<TestStepId>(JourneyType.PROPERTY_REGISTRATION, validator, journeyDataService)

    @Mock
    lateinit var mockJourneyDataService: JourneyDataService

    @Mock
    lateinit var validator: Validator

    @BeforeEach
    fun setup() {
        mockJourneyDataService = mock()
        validator = mock()

        // Ensure form data for each page is never null
        val journeyData = TestStepId.entries.associate { it.urlPathSegment to mutableMapOf<String, String>() }
        whenever(mockJourneyDataService.getJourneyDataFromSession()).thenReturn(journeyData.toMutableMap())
    }

    fun getTwoStepTask(status: TaskStatus = TaskStatus.COMPLETED): JourneyTask<TestStepId> =
        JourneyTask(
            TestStepId.TwoStepTaskPartOne,
            setOf(
                Step(
                    TestStepId.TwoStepTaskPartOne,
                    mock(),
                    isSatisfied = { _, _ -> if (status == TaskStatus.IN_PROGRESS || status == TaskStatus.COMPLETED) true else false },
                    nextAction = { _, _ -> Pair(TestStepId.TwoStepTaskPartTwo, null) },
                ),
                Step(
                    TestStepId.TwoStepTaskPartTwo,
                    mock(),
                    isSatisfied = { _, _ -> if (status == TaskStatus.COMPLETED) true else false },
                    nextAction = { _, _ -> Pair(TestStepId.SimpleTaskTwo, null) },
                ),
            ),
            "two step task",
        )

    fun getMultiPathTask(useMainline: Boolean): JourneyTask<TestStepId> =
        JourneyTask(
            TestStepId.MultiPathTaskStart,
            setOf(
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
            ),
            "multi-path task",
        )

    @Nested
    inner class MultiPathJourneyTests {
        fun getMultiPathJourneyWithMainlineCompleted(useMainline: Boolean): TestJourney =
            TestJourney(
                validator,
                mockJourneyDataService,
                TestStepId.SimpleTaskOne,
                listOf(
                    JourneySection(
                        listOf(
                            JourneyTask.withOneStep(
                                Step(
                                    TestStepId.SimpleTaskOne,
                                    mock(),
                                    isSatisfied = { _, _ -> true },
                                    nextAction = { _, _ -> Pair(TestStepId.MultiPathTaskStart, null) },
                                ),
                                "task 1",
                            ),
                            getMultiPathTask(useMainline),
                            getTwoStepTask(),
                            JourneyTask.withOneStep(
                                Step(
                                    TestStepId.SimpleTaskTwo,
                                    mock(),
                                    isSatisfied = { _, _ -> false },
                                ),
                                "task 4",
                            ),
                        ),
                        "section title key",
                    ),
                ),
            )

        @Suppress("ktlint:standard:max-line-length")
        @Test
        fun `when a multi route task is completed along a one route, that task and subsequent filled in tasks show as completed`() {
            // Arrange
            val testJourney = getMultiPathJourneyWithMainlineCompleted(useMainline = true)

            // Act
            val viewModel = testJourney.getJourneyTaskListViewModel().first().tasks

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
        fun `when a multi route task that was previously completed is instead progressed along an alternate incomplete route, any subsequent tasks become unreachable`() {
            // Arrange
            val testJourney = getMultiPathJourneyWithMainlineCompleted(useMainline = false)

            // Act
            val viewModel = testJourney.getJourneyTaskListViewModel().first().tasks

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

        fun getLinearTestJourney(
            simpleTaskOneCompleted: Boolean,
            twoStepTaskStatus: TaskStatus,
            simpleTaskTwoCompleted: Boolean,
        ) = TestJourney(
            validator,
            mockJourneyDataService,
            TestStepId.SimpleTaskOne,
            listOf(
                JourneySection(
                    listOf(
                        JourneyTask.withOneStep(
                            Step(
                                TestStepId.SimpleTaskOne,
                                mock(),
                                isSatisfied = { _, _ -> simpleTaskOneCompleted },
                                nextAction = { _, _ -> Pair(TestStepId.TwoStepTaskPartOne, null) },
                            ),
                            "task 1",
                        ),
                        getTwoStepTask(twoStepTaskStatus),
                        JourneyTask.withOneStep(
                            Step(
                                TestStepId.SimpleTaskTwo,
                                mock(),
                                isSatisfied = { _, _ -> simpleTaskTwoCompleted },
                            ),
                            "task 3",
                        ),
                    ),
                    "Section key",
                ),
            ),
        )

        @Test
        fun `when a task is exactly finished, the next task is marked not yet started`() {
            // Arrange
            val testJourney =
                getLinearTestJourney(
                    simpleTaskOneCompleted = true,
                    twoStepTaskStatus = TaskStatus.NOT_YET_STARTED,
                    simpleTaskTwoCompleted = false,
                )

            // Act
            val viewModel = testJourney.getJourneyTaskListViewModel().first().tasks

            // Assert
            assertIterableEquals(
                listOf("taskList.status.completed", "taskList.status.notYetStarted", "taskList.status.cannotStartYet"),
                viewModel.map { it.status.textKey },
            )
        }

        @Test
        fun `when a task is partially completed, it is marked in progress`() {
            // Arrange
            val testJourney =
                getLinearTestJourney(
                    simpleTaskOneCompleted = true,
                    twoStepTaskStatus = TaskStatus.IN_PROGRESS,
                    simpleTaskTwoCompleted = false,
                )

            // Act
            val viewModel = testJourney.getJourneyTaskListViewModel().first().tasks

            // Assert
            assertIterableEquals(
                listOf("taskList.status.completed", "taskList.status.inProgress", "taskList.status.cannotStartYet"),
                viewModel.map { it.status.textKey },
            )
        }

        @Test
        fun `when all steps are completed for a task , it is marked as complete`() {
            // Arrange
            val testJourney =
                getLinearTestJourney(
                    simpleTaskOneCompleted = true,
                    twoStepTaskStatus = TaskStatus.COMPLETED,
                    simpleTaskTwoCompleted = true,
                )

            // Act
            val viewModel = testJourney.getJourneyTaskListViewModel().first().tasks

            // Assert
            assertIterableEquals(
                listOf("taskList.status.completed", "taskList.status.completed", "taskList.status.completed"),
                viewModel.map { it.status.textKey },
            )
        }
    }
}
