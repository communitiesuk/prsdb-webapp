package uk.gov.communities.prsdb.webapp.examples

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.examples.MaximumLengthInputStream.Companion.withMaxLength
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import java.io.InputStream
import java.io.OutputStream
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

    @Nested
    inner class DelegationTests {
        private lateinit var testStream: InputStream
        private lateinit var sizeLimitedStream: MaximumLengthInputStream

        @BeforeEach
        fun setUp() {
            testStream = mock()
            sizeLimitedStream = testStream.withMaxLength(100)
        }

        @Test
        fun `readNBytes calls delegated method`() {
            // Arrange
            whenever(testStream.readNBytes(any())).thenReturn(byteArrayOf())
            val bytesToBeRead = 177

            // Act
            sizeLimitedStream.readNBytes(bytesToBeRead)

            // Assert
            verify(testStream).readNBytes(bytesToBeRead)
        }

        @Test
        fun `readNBytes with byte array, offset, and length calls delegated method`() {
            // Arrange
            val byteArray = ByteArray(10)
            val offset = 2
            val length = 5
            whenever(testStream.readNBytes(any(), any(), any())).thenReturn(5)

            // Act
            sizeLimitedStream.readNBytes(byteArray, offset, length)

            // Assert
            verify(testStream).readNBytes(byteArray, offset, length)
        }

        @Test
        fun `readAllBytes calls delegated method`() {
            // Arrange
            whenever(testStream.readAllBytes()).thenReturn(byteArrayOf())

            // Act
            sizeLimitedStream.readAllBytes()

            // Assert
            verify(testStream).readAllBytes()
        }

        @Test
        fun `skip calls delegated method`() {
            // Arrange
            val bytesToSkip = 50L
            whenever(testStream.skip(any())).thenReturn(50L)

            // Act
            sizeLimitedStream.skip(bytesToSkip)

            // Assert
            verify(testStream).skip(bytesToSkip)
        }

        @Test
        fun `skipNBytes calls delegated method`() {
            // Arrange
            val bytesToSkip = 50L

            // Act
            sizeLimitedStream.skipNBytes(bytesToSkip)

            // Assert
            verify(testStream).skipNBytes(bytesToSkip)
        }

        @Test
        fun `available calls delegated method`() {
            // Arrange
            whenever(testStream.available()).thenReturn(10)

            // Act
            sizeLimitedStream.available()

            // Assert
            verify(testStream).available()
        }

        @Test
        fun `close calls delegated method`() {
            // Act
            sizeLimitedStream.close()

            // Assert
            verify(testStream).close()
        }

        @Test
        fun `mark calls delegated method`() {
            // Arrange
            val readLimit = 20

            // Act
            sizeLimitedStream.mark(readLimit)

            // Assert
            verify(testStream).mark(readLimit)
        }

        @Test
        fun `reset calls delegated method`() {
            // Act
            sizeLimitedStream.reset()

            // Assert
            verify(testStream).reset()
        }

        @Test
        fun `markSupported calls delegated method`() {
            // Arrange
            whenever(testStream.markSupported()).thenReturn(true)

            // Act
            sizeLimitedStream.markSupported()

            // Assert
            verify(testStream).markSupported()
        }

        @Test
        fun `transferTo calls delegated method`() {
            // Arrange
            val outputStream: OutputStream = mock()
            whenever(testStream.transferTo(any())).thenReturn(100L)

            // Act
            sizeLimitedStream.transferTo(outputStream)

            // Assert
            verify(testStream).transferTo(outputStream)
        }
    }
}
