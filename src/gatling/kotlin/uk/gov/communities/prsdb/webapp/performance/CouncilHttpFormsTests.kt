package uk.gov.communities.prsdb.webapp.performance

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CouncilHttpFormsTests {
    private val config =
        BasicRunConfig.from(
            mapOf(
                "gatling.target" to "local",
                "gatling.basic.mode" to "baseline",
                "gatling.baseUrl" to "http://localhost:18080",
                "gatling.simulatorUrl" to "http://localhost:19090",
                "gatling.basic.landlordSubject" to "contract-only",
            ),
        )

    @Test
    fun `relative confirmation redirects resolve against the submitted action and remain on the target`() {
        val path = "/local-council/2/cancel-invitation/7"
        assertEquals(
            config.baseUrl + "$path/confirmation",
            CouncilHttpForms.destination("../cancel-invitation/7/confirmation", config, "$path/confirmation", config.baseUrl + path),
        )
        assertFailsWith<IllegalArgumentException> {
            CouncilHttpForms.destination("http://example.invalid$path", config, path)
        }
        assertFailsWith<IllegalArgumentException> {
            CouncilHttpForms.destination("/local-council/2/cancel-invitation/8", config, path)
        }
    }

    @Test
    fun `empty actions preserve the current journey and duplicate or empty IDs are rejected`() {
        val path = "/local-council/register-local-council-user/name"
        val current = config.baseUrl + "$path?journeyId=one%2Btwo"
        assertEquals("one+two", CouncilHttpForms.journeyId(CouncilHttpForms.destination("", config, path, current)))
        for (query in listOf("", "?journeyId=", "?journeyId=first&journeyId=second")) {
            assertFailsWith<IllegalArgumentException> { CouncilHttpForms.journeyId(config.baseUrl + path + query) }
        }
    }
}
