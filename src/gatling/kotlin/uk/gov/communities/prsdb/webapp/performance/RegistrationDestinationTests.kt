package uk.gov.communities.prsdb.webapp.performance

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RegistrationDestinationTests {
    private val base = "http://localhost:18080"
    private val route = "/landlord/register-property"

    @Test
    fun `retains exactly the current journey and permitted routing parameters`() {
        val url = "$route/property-type?journeyId=journey-a&withBackUrl=42"
        assertEquals("$base$url", RegistrationDestination.resolve(url, base, "journey-a"))
    }

    @Test
    fun `rejects external changed duplicated missing and unknown journey destinations`() {
        for (url in listOf(
            "https://example.com$route/task-list?journeyId=journey-a",
            "$route/property-type?journeyId=journey-b",
            "$route/property-type",
            "$route/property-type?journeyId=journey-a&journeyId=journey-a",
            "$route/property-type?journeyId=journey-a&redirect=https://example.com",
            "/landlord/property-details/1?journeyId=journey-a",
        )) {
            assertFailsWith<IllegalArgumentException>(url) { RegistrationDestination.resolve(url, base, "journey-a") }
        }
    }

    @Test
    fun `rejects absent duplicated or empty CSRF before posting`() {
        for ((names, values) in listOf(
            emptyList<String>() to emptyList(),
            listOf("version") to listOf("v1"),
            listOf("_csrf") to listOf(""),
            listOf("_csrf", "_csrf") to listOf("one", "two"),
            listOf("_csrf", "version") to listOf("one"),
        )) {
            assertFailsWith<IllegalArgumentException> { RegistrationDestination.hiddenFields(names, values) }
        }
        assertEquals(
            mapOf("_csrf" to "one", "version" to "v1"),
            RegistrationDestination.hiddenFields(listOf("_csrf", "version"), listOf("one", "v1")),
        )
    }

    @Test
    fun `NFT payment mock must be the configured app origin and exact mock path`() {
        val appOrigin = "https://nft.example"
        val paymentBase = "$appOrigin/local/gov-uk-pay"

        assertEquals(paymentBase, PaymentJourney.baseUrl(paymentBase, PerformanceTarget.NFT, appOrigin))
        for (destination in listOf(
            "https://payments.example/local/gov-uk-pay",
            "$appOrigin/local/other",
            "$appOrigin/local/gov-uk-pay?redirect=https://example.com",
            "http://nft.example/local/gov-uk-pay",
        )) {
            assertFailsWith<IllegalArgumentException>(destination) {
                PaymentJourney.baseUrl(destination, PerformanceTarget.NFT, appOrigin)
            }
        }
    }
}
