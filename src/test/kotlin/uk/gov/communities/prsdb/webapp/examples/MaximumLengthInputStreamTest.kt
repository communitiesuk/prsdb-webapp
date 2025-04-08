package uk.gov.communities.prsdb.webapp.examples

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.gov.communities.prsdb.webapp.examples.MaximumLengthInputStream.Companion.withMaxLength
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import kotlin.test.assertEquals

class MaximumLengthInputStreamTest {
    @Nested
    inner class MaxSizeTests {
        @Test
        fun `reading from a max size stream that is large enough allows the full stream to be read`() {
            // Arrange
            val testInput = "the quick brown fox jumps over the lazy dog"
            val maxLengthStream = testInput.byteInputStream().withMaxLength(10000)

            // Act
            val testOutput = maxLengthStream.reader().use { it.readText() }

            // Assert
            assertEquals(testInput, testOutput)
        }

        @Test
        fun `reading from a max size stream that is too small throws an exception once too much is read`() {
            // Arrange
            val testInput = "the quick brown fox jumps over the lazy dog"
            val maxLengthStream = testInput.byteInputStream().withMaxLength(10)

            // Act & assert
            assertThrows<PrsdbWebException> { val testOutput = maxLengthStream.reader().use { it.readText() } }
        }

        @Test
        fun `reading from a max size stream that is too small allows bytes before the limit to be read`() {
            // Arrange
            val testInput = "the quick brown fox jumps over the lazy dog"
            val maxLengthStream = testInput.byteInputStream().withMaxLength(20)

            // Act
            val outputArray = ByteArray(20)
            maxLengthStream.read(outputArray, 0, outputArray.size)

            assertEquals(testInput.substring(0, 20), outputArray.toString(Charsets.UTF_8))
        }
    }
}
