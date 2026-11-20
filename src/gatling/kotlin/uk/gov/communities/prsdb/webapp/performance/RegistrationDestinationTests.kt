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
        assertEquals(
            "$base$route/payment-return?journeyId=journey-a&paymentReference=payment-reference",
            RegistrationDestination.resolve(
                "$route/payment-return?journeyId=journey-a&paymentReference=payment-reference",
                base,
                "journey-a",
            ),
        )
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
    fun `payment completion may end at confirmation without carrying the deleted journey`() {
        assertEquals(
            "$base$route/confirmation",
            RegistrationDestination.resolve("$route/confirmation", base, "journey-a"),
        )
        for (url in listOf(
            "$route/confirmation?journeyId=journey-a",
            "$route/confirmation?paymentReference=payment-reference",
        )) {
            assertFailsWith<IllegalArgumentException>(url) { RegistrationDestination.resolve(url, base, "journey-a") }
        }
    }

    @Test
    fun `rejects absent duplicated or empty CSRF before posting`() {
        for ((names, values) in listOf(
            emptyList<String>() to emptyList(),
            listOf("_csrf") to listOf(""),
            listOf("_csrf", "_csrf") to listOf("one", "two"),
            listOf("_csrf", "version") to listOf("one"),
        )) {
            assertFailsWith<IllegalArgumentException> { RegistrationDestination.hiddenFields(names, values) }
        }
        assertEquals(
            mapOf("_csrf" to "one"),
            RegistrationDestination.hiddenFields(listOf("_csrf"), listOf("one")),
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

    @Test
    fun `hosted payment destination is only the local mock card page`() {
        val base = "http://127.0.0.1:18080/local/gov-uk-pay"
        assertEquals(base, PaymentJourney.baseUrl(base, PerformanceTarget.LOCAL, "http://127.0.0.1:18080"))
        assertFailsWith<IllegalArgumentException> {
            PaymentJourney.baseUrl("http://127.0.0.1:18080/local/other", PerformanceTarget.LOCAL, "http://127.0.0.1:18080")
        }
        assertEquals("$base/mock-card-page/payment-123", PaymentJourney.hostedUrl("$base/mock-card-page/payment-123", base))
        for (destination in listOf(
            "https://payments.example/local/gov-uk-pay/mock-card-page/payment-123",
            "$base/card/payment-123",
            "$base/mock-card-page/payment-123?redirect=https://example.com",
            "$base/mock-card-page/../v1/payments/payment-123",
        )) {
            assertFailsWith<IllegalArgumentException>(destination) { PaymentJourney.hostedUrl(destination, base) }
        }
    }
}
