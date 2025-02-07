package uk.gov.communities.prsdb.webapp.forms

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
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
import uk.gov.communities.prsdb.webapp.forms.tasks.TaskList
import uk.gov.communities.prsdb.webapp.models.viewModels.TaskListItemViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.TaskStatusViewModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

class MultiTaskTransactionTests {
    @Mock
    private lateinit var journeyDataService: JourneyDataService

    private val testUrlSegment: String = "any string value"
    private val testJourneyType: JourneyType = JourneyType.PROPERTY_REGISTRATION

    enum class TestStepId(
        override val urlPathSegment: String,
    ) : StepId

    class TestMultiTaskTransaction(
        journeyDataService: JourneyDataService,
        override val journeyType: JourneyType,
        override val taskListUrlSegment: String,
        override val taskLists: List<TaskList<TestStepId>> = listOf(),
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
        val secondMock = mock<TaskList<TestStepId>>()
        val transaction = TestMultiTaskTransaction(journeyDataService, testJourneyType, testUrlSegment, listOf(firstMock, secondMock))

        val firstList = listOf(TaskListItemViewModel("a string value", TaskStatusViewModel("text for status")))
        val secondList = listOf(TaskListItemViewModel("a different string value", TaskStatusViewModel("status text", "class")))
        whenever(firstMock.getTaskListViewModels()).thenReturn(firstList)
        whenever(secondMock.getTaskListViewModels()).thenReturn(secondList)

        // Act
        val taskViewModelListList = transaction.getTaskListPageViewModels(principalName)

        // Assert
        assertIterableEquals(listOf(firstList, secondList), taskViewModelListList)
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
            transaction.getTaskListPageViewModels(principalName)

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
            transaction.getTaskListPageViewModels(principalName)

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
            transaction.getTaskListPageViewModels(principalName)

            // Assert
            verify(journeyDataService, never()).setJourneyData(any())
            verify(journeyDataService, never()).loadJourneyDataIntoSession(any())
        }
    }
}
