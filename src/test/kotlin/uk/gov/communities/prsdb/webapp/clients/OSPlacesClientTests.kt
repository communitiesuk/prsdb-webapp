package uk.gov.communities.prsdb.webapp.clients

import org.apache.http.HttpException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import uk.gov.communities.prsdb.webapp.exceptions.RateLimitExceededException
import java.net.http.HttpClient
import java.net.http.HttpResponse
import kotlin.test.assertEquals

class OSPlacesClientTests {
    private lateinit var mockHttpClient: HttpClient
    private lateinit var osPlacesClient: OSPlacesClient

    @BeforeEach
    fun setup() {
        mockHttpClient = mock()
        osPlacesClient = OSPlacesClient(mockHttpClient, "http://test/base/url", "test-api-key")
    }

    @Test
    fun `OSPlacesClient returns the response body when the response's status code is 200`() {
        val expectedResponseBody = "body"

        `when`(
            mockHttpClient.send(any(), any<HttpResponse.BodyHandler<String>>()),
        ).thenReturn(MockHttpResponse(body = expectedResponseBody))

        val responseBody = osPlacesClient.searchByPostcode("")
        assertEquals(expectedResponseBody, responseBody)
    }

    @Test
    fun `OSPlacesClient throws a RateLimitExceededException when the response's status code is 429`() {
        `when`(mockHttpClient.send(any(), any<HttpResponse.BodyHandler<String>>())).thenReturn(MockHttpResponse(429))

        assertThrows<RateLimitExceededException> { osPlacesClient.searchByPostcode("") }
    }

    @Test
    fun `OSPlacesClient throws a HttpException when the response has an error status code and is in the expected format`() {
        val expectedErrorBody = "{'error':{'message':'example error message','statuscode':'400'}}"
        val expectedErrorMessage = "Error 400: example error message"

        `when`(mockHttpClient.send(any(), any<HttpResponse.BodyHandler<String>>())).thenReturn(
            MockHttpResponse(
                400,
                expectedErrorBody,
            ),
        )

        val thrownException = assertThrows<HttpException> { osPlacesClient.searchByPostcode("") }
        assertEquals(expectedErrorMessage, thrownException.message)
    }

    @Test
    fun `OSPlacesClient throws a HttpException when the response has an error status code and is not in the expected format`() {
        val unexpectedErrorBody = "error"
        val expectedErrorMessage = "Error 400"

        `when`(mockHttpClient.send(any(), any<HttpResponse.BodyHandler<String>>())).thenReturn(
            MockHttpResponse(
                400,
                unexpectedErrorBody,
            ),
        )

        val thrownException = assertThrows<HttpException> { osPlacesClient.searchByPostcode("") }
        assertEquals(expectedErrorMessage, thrownException.message)
    }
}
