package uk.gov.communities.prsdb.webapp.clients

import org.apache.http.HttpException
import org.json.JSONException
import org.json.JSONObject
import uk.gov.communities.prsdb.webapp.exceptions.RateLimitExceededException
import java.io.InputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class OsDownloadsClient(
    private val client: HttpClient,
    private val baseURL: String,
    private val apiKey: String,
) {
    fun getDataPackageVersionHistory(dataPackageId: String): String = getResponse("/dataPackages/$dataPackageId/versions").body()

    fun getDataPackageVersionDetails(
        dataPackageId: String,
        versionId: String,
    ): String = getResponse("/dataPackages/$dataPackageId/versions/$versionId").body()

    fun getDataPackageVersionFile(
        dataPackageId: String,
        versionId: String,
        fileName: String,
    ): InputStream {
        val response = getResponse("/dataPackages/$dataPackageId/versions/$versionId/downloads?fileName=$fileName")
        val redirectUri = getRedirectUri(response)
        return getFileDownloadResponse(redirectUri).body()
    }

    private fun getResponse(endpoint: String): HttpResponse<String> {
        val request: HttpRequest =
            HttpRequest
                .newBuilder()
                .header("key", apiKey)
                .uri(URI.create(baseURL + endpoint))
                .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        return when (response.statusCode()) {
            200 -> response
            307 -> response
            429 -> throw RateLimitExceededException("Rate limit exceeded for OS Downloads requests")
            else -> throw HttpException(getErrorMessage(response))
        }
    }

    private fun getFileDownloadResponse(uri: URI): HttpResponse<InputStream> {
        val request: HttpRequest =
            HttpRequest
                .newBuilder()
                .uri(uri)
                .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofInputStream())

        return when (response.statusCode()) {
            200 -> response
            else -> throw HttpException("Error ${response.statusCode()}: Unable to download file from OS Downloads")
        }
    }

    private fun getRedirectUri(response: HttpResponse<String>): URI {
        val redirectUriString =
            response.headers().firstValue("location").orElseThrow {
                HttpException("307 response from OS Downloads missing 'location' header")
            }
        return URI.create(redirectUriString)
    }

    private fun getErrorMessage(response: HttpResponse<String>): String {
        val errorMessage =
            try {
                JSONObject(response.body()).getString("message")
            } catch (exception: JSONException) {
                println("Warn: Unexpected error response format from OS Downloads - ${exception.message}")
                response.body()
            }

        return "Error ${response.statusCode()}: $errorMessage"
    }
}
