package uk.gov.communities.prsdb.webapp.examples

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.io.InputStream

@Service
@Profile("!local")
class ExampleFileUploader : FileUploader {
    override fun uploadFile(
        inputStream: InputStream,
        extension: String?,
    ): String =
        if (extension == "txt") {
            readTextStream(inputStream)
        } else {
            "Bytes read is ${countStreamBytes(inputStream)} bytes"
        }

    private fun countStreamBytes(inputStream: InputStream): Int {
        var countOfBytesRead = 0
        inputStream.buffered().use { bufferedInputStream ->
            while (bufferedInputStream.read() != -1) {
                countOfBytesRead++
            }
        }
        return countOfBytesRead
    }

    private fun readTextStream(textStream: InputStream) =
        textStream.bufferedReader().use {
            it.readText()
        }
}
