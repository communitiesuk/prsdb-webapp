package uk.gov.communities.prsdb.webapp.testHelpers.mockObjects

import org.apache.commons.fileupload2.core.FileItemHeaders
import org.apache.commons.fileupload2.core.FileItemInput
import org.apache.commons.fileupload2.core.FileItemInputIterator
import java.io.InputStream

class MockFileItemInput(
    private val fieldName: String,
    private val isFormField: Boolean,
) : FileItemInput {
    override fun getFieldName(): String = fieldName

    override fun isFormField(): Boolean = isFormField

    override fun getHeaders(): FileItemHeaders? = null

    override fun setHeaders(p0: FileItemHeaders?): FileItemInput? = null

    override fun getContentType(): String? = null

    override fun getInputStream(): InputStream? = null

    override fun getName(): String? = null
}

class MockFileItemInputIterator(
    private val fileItemInputs: List<FileItemInput>,
) : FileItemInputIterator {
    private var index = 0

    override fun hasNext(): Boolean = index < fileItemInputs.size

    override fun next(): FileItemInput = fileItemInputs[index++]

    override fun unwrap(): MutableIterator<FileItemInput>? = null

    override fun getFileSizeMax(): Long = 0

    override fun getSizeMax(): Long = 0

    override fun setFileSizeMax(p0: Long) {}

    override fun setSizeMax(p0: Long) {}
}
