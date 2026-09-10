package uk.gov.communities.prsdb.webapp.models.viewModels.taskModels

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.constants.TAG_COLOUR_GREY
import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus

class TaskStatusViewModelTests {
    @Test
    fun `fromStatus maps NOT_REQUIRED to a grey tag with the not required message key`() {
        val viewModel = TaskStatusViewModel.fromStatus(TaskStatus.NOT_REQUIRED)

        assertEquals("taskList.status.notRequired", viewModel.textKey)
        assertEquals(TAG_COLOUR_GREY, viewModel.colour)
        assertTrue(viewModel.isTag)
        assertFalse(viewModel.isCannotStart)
    }

    @Test
    fun `fromStatus maps NOT_NEEDED_YET to a grey tag with the not needed yet message key`() {
        val viewModel = TaskStatusViewModel.fromStatus(TaskStatus.NOT_NEEDED_YET)

        assertEquals("taskList.status.notNeededYet", viewModel.textKey)
        assertEquals(TAG_COLOUR_GREY, viewModel.colour)
        assertTrue(viewModel.isTag)
        assertFalse(viewModel.isCannotStart)
    }
}
