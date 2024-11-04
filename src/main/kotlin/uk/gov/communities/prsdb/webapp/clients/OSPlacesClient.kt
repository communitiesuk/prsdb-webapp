package uk.gov.communities.prsdb.webapp.clients

import org.apache.http.HttpException
import org.json.JSONException
import org.json.JSONObject
import uk.gov.communities.prsdb.webapp.exceptions.RateLimitExceededException
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class OSPlacesClient(
    private val client: HttpClient,
    private val baseURL: String,
    private val apiKey: String,
) {
    fun search(
        buildingNameOrNumber: String,
        postcode: String,
    ): String {
        val query = URLEncoder.encode("$buildingNameOrNumber $postcode", "UTF-8")
        return getResponse("/find?minmatch=0.4&maxresults=10&query=$query")
    }

    private fun getResponse(endpoint: String): String {
        val request: HttpRequest =
            HttpRequest
                .newBuilder()
                .header("key", apiKey)
                .uri(URI.create(baseURL + endpoint))
                .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        when (response.statusCode()) {
            200 -> return response.body()
            429 -> throw RateLimitExceededException("Rate limit exceeded for OS Places requests")
            else -> throw HttpException(getErrorMessage(response))
        }
    }

    private fun getErrorMessage(response: HttpResponse<String>): String {
        var errorMessage = "Error ${response.statusCode()}: "
        try {
            errorMessage += JSONObject(response.body()).getJSONObject("error").getString("message")
        } catch (e: JSONException) {
            println("Warn: Unexpected error response format from OS Places - ${e.message}")
            errorMessage += response.body()
        }
        return errorMessage
    }
}
