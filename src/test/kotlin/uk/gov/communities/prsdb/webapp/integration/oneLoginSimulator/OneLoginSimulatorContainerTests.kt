package uk.gov.communities.prsdb.webapp.integration.oneLoginSimulator

import org.json.JSONObject
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.test.Test
import kotlin.test.assertEquals

class OneLoginSimulatorContainerTests {
    @Test
    fun `simulator exposes its configured issuer through discovery`() {
        OneLoginSimulatorClientKeys.create().use { clientKeys ->
            OneLoginSimulatorContainer().use { simulator ->
                simulator.start()
                simulator.configure(
                    clientId = simulator.clientId,
                    publicKey = clientKeys.publicKeyPem,
                    redirectUrl = "http://localhost:8080/login/oauth2/code/one-login",
                    postLogoutRedirectUrl = "http://localhost:8080/signout",
                )

                val client = HttpClient.newHttpClient()
                val rootResponse =
                    client.send(
                        HttpRequest.newBuilder(URI.create("${simulator.baseUrl}/")).GET().build(),
                        HttpResponse.BodyHandlers.ofString(),
                    )
                val discoveryResponse =
                    client.send(
                        HttpRequest
                            .newBuilder(URI.create("${simulator.baseUrl}/.well-known/openid-configuration"))
                            .GET()
                            .build(),
                        HttpResponse.BodyHandlers.ofString(),
                    )

                assertEquals(200, rootResponse.statusCode())
                assertEquals("GOV.UK One Login Simulator", rootResponse.body())
                assertEquals(200, discoveryResponse.statusCode())
                assertEquals(simulator.issuerUrl, JSONObject(discoveryResponse.body()).getString("issuer"))
            }
        }
    }
}
