package uk.gov.communities.prsdb.webapp.helpers.extensions

import org.apache.commons.fileupload2.core.FileItemInput
import org.apache.commons.fileupload2.core.FileItemInputIterator

class FileItemInputIteratorExtensions {
    companion object {
        fun FileItemInputIterator.getFirstFileField(): FileItemInput? {
            while (hasNext()) {
                val nextField = next()
                if (!nextField.isFormField) return nextField
            }
            return null
        }

        fun FileItemInputIterator.discardRemainingFields() = forEachRemaining {}
    }
}
