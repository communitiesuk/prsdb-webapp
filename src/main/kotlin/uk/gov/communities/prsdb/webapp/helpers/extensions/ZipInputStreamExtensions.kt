package uk.gov.communities.prsdb.webapp.helpers.extensions

import java.util.zip.ZipException
import java.util.zip.ZipInputStream

class ZipInputStreamExtensions {
    companion object {
        fun ZipInputStream.goToEntry(entryName: String) {
            var currentEntry = this.nextEntry
            while (currentEntry != null && currentEntry.name != entryName) {
                currentEntry = this.nextEntry
            }
            if (currentEntry == null) {
                throw ZipException("Zip file does not contain entry $entryName")
            }
        }
    }
}
