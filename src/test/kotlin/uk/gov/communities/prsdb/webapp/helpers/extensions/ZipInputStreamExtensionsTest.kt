package uk.gov.communities.prsdb.webapp.helpers.extensions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.gov.communities.prsdb.webapp.helpers.extensions.ZipInputStreamExtensions.Companion.goToEntry
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class ZipInputStreamExtensionsTest {
    @Test
    fun `goToEntry finds the correct entry`() {
        // Arrange
        val desiredEntryAndContent = "desired.txt" to "def"
        val zipInputStream = createZipInputStream("other.txt" to "abc", desiredEntryAndContent)

        // Act
        zipInputStream.goToEntry(desiredEntryAndContent.first)

        // Assert
        val navigatedToEntryContent = zipInputStream.bufferedReader().readText()
        assertEquals(desiredEntryAndContent.second, navigatedToEntryContent)
    }

    @Test
    fun `goToEntry throws if entry not found`() {
        // Arrange
        val desiredEntryAndContent = "desired.txt" to "def"
        val zipInputStream = createZipInputStream("other.txt" to "abc")

        // Act & Assert
        assertThrows<ZipException> { zipInputStream.goToEntry(desiredEntryAndContent.first) }
    }

    companion object {
        private fun createZipInputStream(vararg entries: Pair<String, String>): ZipInputStream {
            val baos = ByteArrayOutputStream()
            ZipOutputStream(baos).use { zos ->
                entries.forEach { (name, data) ->
                    zos.putNextEntry(ZipEntry(name))
                    zos.write(data.toByteArray())
                    zos.closeEntry()
                }
            }
            return ZipInputStream(baos.toByteArray().inputStream())
        }
    }
}
