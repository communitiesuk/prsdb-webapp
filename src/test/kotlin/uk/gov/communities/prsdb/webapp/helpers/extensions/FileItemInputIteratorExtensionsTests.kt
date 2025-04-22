package uk.gov.communities.prsdb.webapp.helpers.extensions

import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.helpers.extensions.FileItemInputIteratorExtensions.Companion.getFirstFileField
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockFileItemInput
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockFileItemInputIterator
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FileItemInputIteratorExtensionsTests {
    @Test
    fun `getFirstFileField returns null when the iterator is empty`() {
        val iterator = MockFileItemInputIterator(emptyList())

        assertNull(iterator.getFirstFileField())
    }

    @Test
    fun `getFirstFileField returns null when the iterator has no file fields`() {
        val iterator =
            MockFileItemInputIterator(
                listOf(
                    MockFileItemInput("formField1", isFormField = true),
                    MockFileItemInput("formField2", isFormField = true),
                    MockFileItemInput("formField2", isFormField = true),
                ),
            )

        assertNull(iterator.getFirstFileField())
    }

    @Test
    fun `getFirstFileField returns the file field when the iterator has one file field`() {
        val iterator =
            MockFileItemInputIterator(
                listOf(
                    MockFileItemInput("formField1", isFormField = true),
                    MockFileItemInput("fileField", isFormField = false),
                    MockFileItemInput("formField2", isFormField = true),
                ),
            )

        val firstFileField = iterator.getFirstFileField()!!

        assertEquals(firstFileField.fieldName, "fileField")
    }

    @Test
    fun `getFirstFileField returns the first file field when the iterator has multiple file fields`() {
        val iterator =
            MockFileItemInputIterator(
                listOf(
                    MockFileItemInput("formField1", isFormField = true),
                    MockFileItemInput("fileField1", isFormField = false),
                    MockFileItemInput("fileField2", isFormField = false),
                ),
            )

        val firstFileField = iterator.getFirstFileField()!!

        assertEquals(firstFileField.fieldName, "fileField1")
    }
}
