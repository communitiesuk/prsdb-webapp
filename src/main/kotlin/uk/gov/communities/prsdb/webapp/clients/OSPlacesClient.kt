package uk.gov.communities.prsdb.webapp.clients

import org.apache.http.HttpException
import org.json.JSONObject
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class OSPlacesClient(
    private val baseURL: String,
    private val apiKey: String,
) {
    private val client = HttpClient.newHttpClient()

    fun searchByPostcode(postcode: String): String = getResponse("/postcode?postcode=${postcode.replace(" ", "")}")

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
            val errorObject = JSONObject(responseBody).getJSONObject("error")
            throw HttpException("Error ${errorObject.getInt("statuscode")}: ${errorObject.getString("message")}")
        }

        return responseBody
    }
}
