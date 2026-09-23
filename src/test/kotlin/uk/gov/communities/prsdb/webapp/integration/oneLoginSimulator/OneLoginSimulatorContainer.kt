package uk.gov.communities.prsdb.webapp.integration.oneLoginSimulator

import org.json.JSONObject
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import uk.gov.communities.prsdb.webapp.constants.OneLoginClaimKeys.Companion.ADDRESS
import uk.gov.communities.prsdb.webapp.constants.OneLoginClaimKeys.Companion.CORE_IDENTITY
import uk.gov.communities.prsdb.webapp.constants.OneLoginClaimKeys.Companion.RETURN_CODE
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class OneLoginSimulatorContainer : AutoCloseable {
    private val container: GenericContainer<*> =
        GenericContainer(DockerImageName.parse(IMAGE))
            .withExposedPorts(3000)
            .withEnv("INTERACTIVE_MODE", "true")
            .waitingFor(Wait.forHttp("/").forPort(3000).forStatusCode(200))

    val clientId = "prsdb-test-client"
    val baseUrl: String
        get() = "http://${container.host.replace("localhost", "127.0.0.1")}:${container.firstMappedPort}"

    val issuerUrl: String
        get() = "$baseUrl/"

    fun start() {
        container.start()
    }

    fun configure(
        clientId: String,
        publicKey: String,
        redirectUrl: String,
        postLogoutRedirectUrl: String,
    ) {
        val body =
            JSONObject()
                .put("simulatorUrl", baseUrl)
                .put(
                    "clientConfiguration",
                    JSONObject()
                        .put("clientId", clientId)
                        .put("publicKeySource", "STATIC")
                        .put("publicKey", publicKey)
                        .put("scopes", listOf("openid"))
                        .put("redirectUrls", listOf(redirectUrl))
                        .put("postLogoutRedirectUrls", listOf(postLogoutRedirectUrl))
                        .put(
                            "claims",
                            listOf(
                                CORE_IDENTITY,
                                ADDRESS,
                                RETURN_CODE,
                            ),
                        )
                        .put("identityVerificationSupported", true)
                        .put("idTokenSigningAlgorithm", "ES256")
                        .put("clientLoCs", listOf("P0", "P2")),
                )

        val request =
            HttpRequest.newBuilder(URI.create("$baseUrl/config"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build()

        val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
        check(response.statusCode() in 200..299) {
            "Simulator configuration failed with status ${response.statusCode()}: ${response.body()}"
        }
    }

    override fun close() {
        container.stop()
    }

    companion object {
        // Release 26.09.4 is deliberately pinned for repeatable contract tests; update after reviewing simulator releases.
        private const val IMAGE =
            "ghcr.io/govuk-one-login/simulator@" +
                "sha256:0d5e62c1db1c400c4881be2270b3f08aeb55c72ca3d9eb9a6e5196becef6f5e5"
    }
}
