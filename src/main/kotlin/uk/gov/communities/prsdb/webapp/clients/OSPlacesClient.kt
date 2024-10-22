package uk.gov.communities.prsdb.webapp.clients

import org.apache.http.HttpException
import org.json.JSONException
import org.json.JSONObject
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class OSPlacesClient(
    private val baseURL: String,
    private val apiKey: String,
) {
    private val client = HttpClient.newHttpClient()

    fun searchByPostcode(postcode: String): String = getResponse("/postcode?postcode=${URLEncoder.encode(postcode, "UTF-8")}")

    private fun getResponse(endpoint: String): String {
        val request: HttpRequest =
            HttpRequest
                .newBuilder()
                .header("key", apiKey)
                .uri(URI.create(baseURL + endpoint))
                .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        val responseBody = response.body()

        if (response.statusCode() != 200) {
            var errorMessage = "Error ${response.statusCode()}"
            try {
                errorMessage += JSONObject(responseBody).getJSONObject("error").getString("message")
            } catch (e: JSONException) {
                println("Warn: Unexpected error response format from OS Places - ${e.message}")
            }
            throw HttpException(errorMessage)
        }

        return responseBody
    }
}
