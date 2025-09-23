package uk.gov.communities.prsdb.webapp.clients

import org.apache.http.HttpException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.exceptions.RateLimitExceededException
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockHttpResponse
import java.net.http.HttpClient
import java.net.http.HttpResponse
import kotlin.test.assertEquals

class OsDownloadsClientTests {
    private lateinit var mockHttpClient: HttpClient
    private lateinit var osDownloadsClient: OsDownloadsClient

    @BeforeEach
    fun setup() {
        mockHttpClient = mock()
        osDownloadsClient = OsDownloadsClient(mockHttpClient, "http://test/base/url", "test-api-key")
    }

    @Test
    fun `OsDownloadsClient throws a RateLimitExceededException when the response's status code is 429`() {
        whenever(mockHttpClient.send(any(), any<HttpResponse.BodyHandler<String>>())).thenReturn(MockHttpResponse(429))

        assertThrows<RateLimitExceededException> { osDownloadsClient.getDataPackageVersionHistory("dataPackageId") }
    }

    @Test
    fun `OsDownloadsClient throws a HttpException when the response has a non-429 error status code and is in the expected format`() {
        val errorBody = "{'message':'example error message'}"
        whenever(mockHttpClient.send(any(), any<HttpResponse.BodyHandler<String>>())).thenReturn(MockHttpResponse(400, errorBody))

        val thrownException = assertThrows<HttpException> { osDownloadsClient.getDataPackageVersionHistory("dataPackageId") }

        val expectedErrorMessage = "Error 400: example error message"
        assertEquals(expectedErrorMessage, thrownException.message)
    }

    @Test
    fun `OsDownloadsClient throws a HttpException when the response has a non-429 error status code and is in an unexpected format`() {
        val errorBody = "wrong error format"
        whenever(mockHttpClient.send(any(), any<HttpResponse.BodyHandler<String>>())).thenReturn(MockHttpResponse(400, errorBody))

        val thrownException = assertThrows<HttpException> { osDownloadsClient.getDataPackageVersionHistory("dataPackageId") }

        val expectedErrorMessage = "Error 400: wrong error format"
        assertEquals(expectedErrorMessage, thrownException.message)
    }

    @Test
    fun `getDataPackageVersionHistory returns the response body as a string when the response's status code is 200`() {
        val responseBody = "body"
        whenever(mockHttpClient.send(any(), any<HttpResponse.BodyHandler<String>>())).thenReturn(MockHttpResponse(body = responseBody))

        val returnedResponseBody = osDownloadsClient.getDataPackageVersionHistory("dataPackageId")

        assertEquals(responseBody, returnedResponseBody)
    }

    @Test
    fun `getDataPackageVersionDetails returns the response body as a string when the response's status code is 200`() {
        val responseBody = "body"
        whenever(mockHttpClient.send(any(), any<HttpResponse.BodyHandler<String>>())).thenReturn(MockHttpResponse(body = responseBody))

        val returnedResponseBody = osDownloadsClient.getDataPackageVersionDetails("dataPackageId", "versionId")

        assertEquals(responseBody, returnedResponseBody)
    }

    @Test
    fun `getDataPackageVersionFile throws a HttpException when the response doesn't contain a redirect location`() {
        whenever(mockHttpClient.send(any(), any<HttpResponse.BodyHandler<String>>())).thenReturn(MockHttpResponse(statusCode = 307))

        val thrownException =
            assertThrows<HttpException> { osDownloadsClient.getDataPackageVersionFile("dataPackageId", "versionId", "fileName") }

        val expectedErrorMessage = "Error: 307 response from OS Downloads missing 'location' header"
        assertEquals(expectedErrorMessage, thrownException.message)
    }

    @Test
    fun `getDataPackageVersionFile throws a HttpException when the redirect's response's status code isn't 200`() {
        whenever(mockHttpClient.send(any(), any<HttpResponse.BodyHandler<Any>>()))
            .thenReturn(MockHttpResponse(statusCode = 307, headers = mapOf("location" to listOf("http://redirect.uri"))))
            .thenReturn(MockHttpResponse(401))

        val thrownException =
            assertThrows<HttpException> { osDownloadsClient.getDataPackageVersionFile("dataPackageId", "versionId", "fileName") }

        val expectedErrorMessage = "Error 401: Unable to download file from OS Downloads"
        assertEquals(expectedErrorMessage, thrownException.message)
    }

    @Test
    fun `getDataPackageVersionFile returns the response body as an input stream when the response's status code is 200`() {
        val responseBody = "body".byteInputStream()
        whenever(mockHttpClient.send(any(), any<HttpResponse.BodyHandler<Any>>()))
            .thenReturn(MockHttpResponse(statusCode = 307, headers = mapOf("location" to listOf("http://redirect.uri"))))
            .thenReturn(MockHttpResponse(body = responseBody))

        val returnedResponseBody = osDownloadsClient.getDataPackageVersionFile("dataPackageId", "versionId", "fileName")
        assertEquals(responseBody, returnedResponseBody)
    }
}
