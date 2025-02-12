package uk.gov.communities.prsdb.webapp.helpers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.forms.tasks.MultiTaskTransaction
import uk.gov.communities.prsdb.webapp.forms.tasks.PropertyRegistrationSectionId
import uk.gov.communities.prsdb.webapp.forms.tasks.RegisterPropertyMultiTaskTransaction
import uk.gov.communities.prsdb.webapp.forms.tasks.SectionId
import uk.gov.communities.prsdb.webapp.forms.tasks.TaskList
import uk.gov.communities.prsdb.webapp.helpers.JourneySectionHelper.Companion.getSectionHeaderInfoForStep
import uk.gov.communities.prsdb.webapp.models.viewModels.SectionHeaderViewModel
import kotlin.test.Test

class JourneySectionHelperTests {
    private lateinit var mockTransaction: RegisterPropertyMultiTaskTransaction
    private lateinit var mockTaskList: TaskList<RegisterPropertyStepId>

    enum class TestSectionId(
        override val sectionNumber: Int,
    ) : SectionId {
        SECTION_1(1),
    }

    @BeforeEach
    fun setup() {
        mockTransaction = mock()
        mockTaskList = mock()
    }

    @Test
    fun `getSectionHeaderInfoForStep returns null if the stepId is not found in the transaction`() {
        assertNull(getSectionHeaderInfoForStep(RegisterPropertyStepId.PropertyType, mockTransaction))
    }

    @Test
    fun `getSectionHeaderInfoForStep returns null if the type of SectionId is not implemented on MessageKeyConverter`() {
        whenever(
            mockTransaction.getSectionForStep(RegisterPropertyStepId.AlreadyRegistered),
        ).thenReturn(TestSectionId.SECTION_1)
        whenever(mockTransaction.taskLists).thenReturn(
            listOf(
                MultiTaskTransaction.TransactionSection(TestSectionId.SECTION_1, mockTaskList),
            ),
        )

        assertNull(getSectionHeaderInfoForStep(RegisterPropertyStepId.AlreadyRegistered, mockTransaction))
    }

    @Test
    fun `getSectionHeaderInfoForStep returns a SectionHeaderViewModel if the step is found in a section with a heading`() {
        whenever(
            mockTransaction.getSectionForStep(RegisterPropertyStepId.LookupAddress),
        ).thenReturn(PropertyRegistrationSectionId.PROPERTY_DETAILS)

        whenever(mockTransaction.taskLists).thenReturn(
            listOf(
                MultiTaskTransaction.TransactionSection(PropertyRegistrationSectionId.PROPERTY_DETAILS, mockTaskList),
            ),
        )

        val expectedSectionHeader = SectionHeaderViewModel("registerProperty.taskList.register.heading", 1, 1)

        // Act
        val sectionHeader = getSectionHeaderInfoForStep(RegisterPropertyStepId.LookupAddress, mockTransaction)

        // Assert
        assertEquals(expectedSectionHeader.sectionNameKey, sectionHeader?.sectionNameKey)
        assertEquals(expectedSectionHeader.sectionNumber, sectionHeader?.sectionNumber)
        assertEquals(expectedSectionHeader.totalSections, sectionHeader?.totalSections)
    }
}
