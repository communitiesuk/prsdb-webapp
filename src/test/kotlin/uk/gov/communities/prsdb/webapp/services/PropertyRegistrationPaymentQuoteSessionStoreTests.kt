package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer
import org.springframework.mock.web.MockHttpSession
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyRegistrationPaymentQuote
import java.time.LocalDate
import kotlin.test.assertIs

class PropertyRegistrationPaymentQuoteSessionStoreTests {
    @Test
    fun `returns null when the journey has no quote`() {
        // Arrange
        val store = PropertyRegistrationPaymentQuoteSessionStore(MockHttpSession())

        // Act, Assert
        assertNull(store.getQuote("journey-1"))
    }

    @Test
    fun `stores quotes separately for each journey in the session`() {
        // Arrange
        val store = PropertyRegistrationPaymentQuoteSessionStore(MockHttpSession())
        val firstQuote = createQuote(LocalDate.of(2027, 10, 10))
        val secondQuote = createQuote(LocalDate.of(2027, 10, 11))

        // Act
        store.storeQuote("journey-1", firstQuote)
        store.storeQuote("journey-2", secondQuote)

        // Assert
        assertEquals(firstQuote, store.getQuote("journey-1"))
        assertEquals(secondQuote, store.getQuote("journey-2"))
        assertNull(store.getQuote("journey-3"))
    }

    @Test
    fun `replaces the quote without modifying persisted journey data`() {
        // Arrange
        val session = MockHttpSession()
        val store = PropertyRegistrationPaymentQuoteSessionStore(session)
        val journeyData = mapOf("journeyData" to mapOf("payment-summary" to emptyMap<String, String>()))
        session.setAttribute("journey-1", journeyData)
        store.storeQuote("journey-1", createQuote(LocalDate.of(2027, 10, 10)))
        val refreshedQuote = createQuote(LocalDate.of(2027, 10, 11))

        // Act
        store.storeQuote("journey-1", refreshedQuote)

        // Assert
        assertEquals(refreshedQuote, store.getQuote("journey-1"))
        assertEquals(journeyData, session.getAttribute("journey-1"))
        assertEquals(2, session.attributeNames.toList().size)
    }

    @Test
    fun `quotes are isolated between sessions`() {
        // Arrange
        val firstStore = PropertyRegistrationPaymentQuoteSessionStore(MockHttpSession())
        val secondStore = PropertyRegistrationPaymentQuoteSessionStore(MockHttpSession())

        // Act
        firstStore.storeQuote("journey-1", createQuote(LocalDate.of(2027, 10, 10)))

        // Assert
        assertNull(secondStore.getQuote("journey-1"))
    }

    @Test
    fun `quote survives the Redis session serializer`() {
        // Arrange
        val quote = createQuote(LocalDate.of(2027, 10, 10))
        val serializer = JdkSerializationRedisSerializer()

        // Act
        val deserializedQuote = serializer.deserialize(serializer.serialize(quote))
        val store = PropertyRegistrationPaymentQuoteSessionStore(MockHttpSession())
        store.storeQuote("journey-1", assertIs<PropertyRegistrationPaymentQuote>(deserializedQuote))

        // Assert
        assertEquals(quote, store.getQuote("journey-1"))
    }

    @Test
    fun `fails explicitly when the session attribute is not a quote`() {
        // Arrange
        val session = MockHttpSession()
        session.setAttribute("propertyRegistrationPaymentQuote:journey-1", "invalid")
        val store = PropertyRegistrationPaymentQuoteSessionStore(session)

        // Act, Assert
        assertThrows<IllegalStateException> { store.getQuote("journey-1") }
    }

    private fun createQuote(quoteDate: LocalDate) =
        PropertyRegistrationPaymentQuote(
            amountInPence = 1234,
            quoteDate = quoteDate,
            renewalDate = LocalDate.of(2028, 3, 1),
        )
}
