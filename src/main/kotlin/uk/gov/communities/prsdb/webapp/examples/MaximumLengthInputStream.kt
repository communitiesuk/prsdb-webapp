package uk.gov.communities.prsdb.webapp.examples

import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import java.io.InputStream
import java.io.OutputStream

class MaximumLengthInputStream(
    private val innerInputStream: InputStream,
    private val maxLength: Long,
) : InputStream() {
    private var sizeSoFar: Long = 0

    override fun read(): Int {
        val byteRead = innerInputStream.read()
        if (byteRead >= 0) {
            recordReadBytes(1)
        }
        return byteRead
    }

    override fun read(buffer: ByteArray): Int = read(buffer, 0, buffer.size)

    override fun read(
        buffer: ByteArray,
        offset: Int,
        length: Int,
    ): Int {
        val i = innerInputStream.read(buffer, offset, length)
        if (i >= 0) recordReadBytes(i)
        return i
    }

    private fun recordReadBytes(numberOfBytesRead: Int) {
        sizeSoFar += numberOfBytesRead
        if (sizeSoFar > maxLength) {
            throw PrsdbWebException("Stream too long: $sizeSoFar read so far; max size: $maxLength")
        }
    }

    override fun readNBytes(
        b: ByteArray,
        off: Int,
        len: Int,
    ): Int = innerInputStream.readNBytes(b, off, len)

    override fun readNBytes(len: Int): ByteArray = innerInputStream.readNBytes(len)

    override fun readAllBytes(): ByteArray = innerInputStream.readAllBytes()

    override fun skip(n: Long): Long = innerInputStream.skip(n)

    override fun skipNBytes(n: Long) = innerInputStream.skipNBytes(n)

    override fun mark(readlimit: Int) = innerInputStream.mark(readlimit)

    override fun reset() = innerInputStream.reset()

    override fun transferTo(out: OutputStream): Long = innerInputStream.transferTo(out)

    override fun markSupported() = innerInputStream.markSupported()

    override fun available() = innerInputStream.available()

    override fun close() = innerInputStream.close()

    companion object {
        fun InputStream.withMaxLength(maxLength: Long): InputStream = MaximumLengthInputStream(this, maxLength)
    }
}
