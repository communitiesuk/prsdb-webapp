package uk.gov.communities.prsdb.webapp.forms

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.StepId
import uk.gov.communities.prsdb.webapp.forms.tasks.MultiTaskTransaction
import uk.gov.communities.prsdb.webapp.forms.tasks.MultiTaskTransaction.TransactionSection
import uk.gov.communities.prsdb.webapp.forms.tasks.PropertyRegistrationSectionId
import uk.gov.communities.prsdb.webapp.forms.tasks.TaskList
import uk.gov.communities.prsdb.webapp.models.viewModels.TaskListItemViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.TaskSectionViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.TaskStatusViewModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

class MultiTaskTransactionTests {
    @Mock
    private lateinit var journeyDataService: JourneyDataService

    private val testUrlSegment: String = "any string value"
    private val testJourneyType: JourneyType = JourneyType.PROPERTY_REGISTRATION

    enum class TestStepId(
        override val urlPathSegment: String,
    ) : StepId {
        FirstStep("step-1"),
    }

    class TestMultiTaskTransaction(
        journeyDataService: JourneyDataService,
        override val journeyType: JourneyType,
        override val taskListUrlSegment: String,
        override val taskLists: List<TransactionSection<TestStepId>> = listOf(),
    ) : MultiTaskTransaction<TestStepId>(journeyDataService)

    @BeforeEach
    fun setup() {
        journeyDataService = mock()
        whenever(journeyDataService.getJourneyDataFromSession()).thenReturn(mutableMapOf())
    }

    @Test
    fun `getTaskListPageViewModels returns, in a list, the TaskListViewModels produced by its taskLists `() {
        val principalName = "principalName"
        val firstMock = mock<TaskList<TestStepId>>()
        val firstSectionId = PropertyRegistrationSectionId.PROPERTY_DETAILS
        val secondMock = mock<TaskList<TestStepId>>()
        val secondSectionId = PropertyRegistrationSectionId.CHECK_AND_SUBMIT
        val transaction =
            TestMultiTaskTransaction(
                journeyDataService,
                testJourneyType,
                testUrlSegment,
                listOf(TransactionSection(firstSectionId, firstMock), TransactionSection(secondSectionId, secondMock)),
            )

        val firstList = listOf(TaskListItemViewModel("a string value", TaskStatusViewModel("text for status")))
        val secondList = listOf(TaskListItemViewModel("a different string value", TaskStatusViewModel("status text", "class")))
        whenever(firstMock.getTaskListViewModels()).thenReturn(firstList)
        whenever(secondMock.getTaskListViewModels()).thenReturn(secondList)

        // Act
        val taskSectionViewModelList = transaction.getTaskListSections(principalName)

        // Assert
        assertIterableEquals(
            listOf(
                TaskSectionViewModel("registerProperty.taskList.register.heading", 1, firstList),
                TaskSectionViewModel("registerProperty.taskList.checkAndSubmit.heading", 2, secondList),
            ),
            taskSectionViewModelList,
        )
    }

    @Nested
    inner class GetSectionForStepTests {
        @Test
        fun `getSectionForStep returns the section the step appears in`() {
            val firstSectionId = PropertyRegistrationSectionId.PROPERTY_DETAILS
            val firstTaskListMock = mock<TaskList<TestStepId>>()
            whenever(firstTaskListMock.isStepInTaskList(TestStepId.FirstStep)).thenReturn(false)

            val secondSectionId = PropertyRegistrationSectionId.PROPERTY_DETAILS
            val secondTaskListMock = mock<TaskList<TestStepId>>()
            whenever(secondTaskListMock.isStepInTaskList(TestStepId.FirstStep)).thenReturn(true)

            val transaction =
                TestMultiTaskTransaction(
                    journeyDataService,
                    testJourneyType,
                    testUrlSegment,
                    listOf(TransactionSection(firstSectionId, firstTaskListMock), TransactionSection(secondSectionId, secondTaskListMock)),
                )

            // Act
            val sectionContainingStep = transaction.getSectionForStep(TestStepId.FirstStep)

            // Assert
            assertEquals(secondSectionId, sectionContainingStep)
        }

        @Test
        fun `getSectionForStep returns the first section the step appears in`() {
            val firstSectionId = PropertyRegistrationSectionId.PROPERTY_DETAILS
            val firstTaskListMock = mock<TaskList<TestStepId>>()
            whenever(firstTaskListMock.isStepInTaskList(TestStepId.FirstStep)).thenReturn(true)

            val secondSectionId = PropertyRegistrationSectionId.PROPERTY_DETAILS
            val secondTaskListMock = mock<TaskList<TestStepId>>()
            whenever(secondTaskListMock.isStepInTaskList(TestStepId.FirstStep)).thenReturn(true)

            val transaction =
                TestMultiTaskTransaction(
                    journeyDataService,
                    testJourneyType,
                    testUrlSegment,
                    listOf(TransactionSection(firstSectionId, firstTaskListMock), TransactionSection(secondSectionId, secondTaskListMock)),
                )

            // Act
            val sectionContainingStep = transaction.getSectionForStep(TestStepId.FirstStep)

            // Assert
            assertEquals(firstSectionId, sectionContainingStep)
        }

        @Test
        fun `getSectionForStep returns null if the step is not in any section`() {
            val firstSectionId = PropertyRegistrationSectionId.PROPERTY_DETAILS
            val firstTaskListMock = mock<TaskList<TestStepId>>()
            whenever(firstTaskListMock.isStepInTaskList(TestStepId.FirstStep)).thenReturn(false)

            val secondSectionId = PropertyRegistrationSectionId.PROPERTY_DETAILS
            val secondTaskListMock = mock<TaskList<TestStepId>>()
            whenever(secondTaskListMock.isStepInTaskList(TestStepId.FirstStep)).thenReturn(false)

            val transaction =
                TestMultiTaskTransaction(
                    journeyDataService,
                    testJourneyType,
                    testUrlSegment,
                    listOf(TransactionSection(firstSectionId, firstTaskListMock), TransactionSection(secondSectionId, secondTaskListMock)),
                )

            // Act
            val sectionContainingStep = transaction.getSectionForStep(TestStepId.FirstStep)

            // Assert
            assertNull(sectionContainingStep)
        }
    }

    @Nested
    inner class JourneyDataManipulationTests {
        @Test
        fun `when there is no journey data in the session or the database, the session is updated to complete the task-list step`() {
            val principalName = "principalName"
            val transaction = TestMultiTaskTransaction(journeyDataService, testJourneyType, testUrlSegment)
            whenever(journeyDataService.getJourneyDataFromSession()).thenReturn(mutableMapOf())
            whenever(journeyDataService.getContextId(principalName, testJourneyType)).thenReturn(null)

            // Act
            transaction.getTaskListSections(principalName)

            // Assert
            val captor = argumentCaptor<JourneyData>()
            verify(journeyDataService).setJourneyData(captor.capture())
            assertEquals(
                captor.allValues.single()[testUrlSegment],
                mutableMapOf<String, Any?>(),
            )

            verify(journeyDataService, never()).loadJourneyDataIntoSession(any())
        }

        @Test
        fun `when the journey data is not in the session it will be loaded into the session from the database`() {
            val principalName = "principalName"
            val contextId = 67L
            val transaction = TestMultiTaskTransaction(journeyDataService, testJourneyType, testUrlSegment)
            whenever(journeyDataService.getJourneyDataFromSession()).thenReturn(mutableMapOf())
            whenever(journeyDataService.getContextId(principalName, testJourneyType)).thenReturn(contextId)

            // Act
            transaction.getTaskListSections(principalName)

            // Assert
            val captor = argumentCaptor<Long>()
            verify(journeyDataService).loadJourneyDataIntoSession(captor.capture())
            assertEquals(contextId, captor.allValues.single())

            verify(journeyDataService, never()).setJourneyData(any())
        }

        @Test
        fun `when the journey data is already in the session, there are not changes to the journey data`() {
            val principalName = "principalName"
            val transaction = TestMultiTaskTransaction(journeyDataService, testJourneyType, testUrlSegment)
            whenever(journeyDataService.getJourneyDataFromSession()).thenReturn(mutableMapOf("anything" to "Anything else"))

            // Act
            transaction.getTaskListSections(principalName)

            // Assert
            verify(journeyDataService, never()).setJourneyData(any())
            verify(journeyDataService, never()).loadJourneyDataIntoSession(any())
        }
    }
}
