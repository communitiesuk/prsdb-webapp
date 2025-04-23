package uk.gov.communities.prsdb.webapp.helpers

import org.apache.tomcat.util.http.fileupload.util.LimitedInputStream
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import java.io.InputStream

class MaximumLengthInputStream(
    innerInputStream: InputStream,
    maxLength: Long,
) : LimitedInputStream(innerInputStream, maxLength) {
    override fun raiseError(
        maxLength: Long,
        sizeSoFar: Long,
    ): Unit = throw PrsdbWebException("Stream too long: $sizeSoFar read so far; max size: $maxLength")

    companion object {
        fun InputStream.withMaxLength(maxLength: Long): MaximumLengthInputStream = MaximumLengthInputStream(this, maxLength)
    }
}
