package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.mock.web.MockHttpSession
import uk.gov.communities.prsdb.webapp.journeys.JourneyStatePersistenceService
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyRegistrationPaymentQuote
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PropertyRegistrationPaymentQuoteStateTests {
    @Test
    fun `payment quote is absent for journeys without a stored quote`() {
        // Arrange
        val journey = createJourney(createStateService(MockHttpSession(), "journey-1"))

        // Act, Assert
        assertNull(journey.paymentQuote)
    }

    @Test
    fun `payment quote is replaced within journey state without a separate session attribute`() {
        // Arrange
        val session = MockHttpSession()
        val journey = createJourney(createStateService(session, "journey-1"))
        journey.paymentQuote = createQuote(LocalDate.of(2027, 10, 10))
        val refreshedQuote = createQuote(LocalDate.of(2027, 10, 11))

        // Act
        journey.paymentQuote = refreshedQuote

        // Assert
        assertEquals(refreshedQuote, journey.paymentQuote)
        assertEquals(setOf("journey-1", "journeyStateKeyStore"), session.attributeNames.toList().toSet())
    }

    @Test
    fun `payment quotes are isolated between journeys in the same session`() {
        // Arrange
        val session = MockHttpSession()
        val firstJourney = createJourney(createStateService(session, "journey-1"))
        val secondJourney = createJourney(createStateService(session, "journey-2"))
        val firstQuote = createQuote(LocalDate.of(2027, 10, 10))
        val secondQuote = createQuote(LocalDate.of(2027, 10, 11))

        // Act
        firstJourney.paymentQuote = firstQuote
        secondJourney.paymentQuote = secondQuote

        // Assert
        assertEquals(firstQuote, firstJourney.paymentQuote)
        assertEquals(secondQuote, secondJourney.paymentQuote)
    }

    @Test
    fun `payment quotes are isolated between sessions`() {
        // Arrange
        val firstJourney = createJourney(createStateService(MockHttpSession(), "journey-1"))
        val secondJourney = createJourney(createStateService(MockHttpSession(), "journey-1"))

        // Act
        firstJourney.paymentQuote = createQuote(LocalDate.of(2027, 10, 10))

        // Assert
        assertNull(secondJourney.paymentQuote)
    }

    @Test
    fun `payment quote survives saving and restoring journey state in a new session`() {
        // Arrange
        val persistenceService = mock<JourneyStatePersistenceService>()
        val journey = createJourney(createStateService(MockHttpSession(), "journey-1", persistenceService))
        val quote = createQuote(LocalDate.of(2027, 10, 10))
        journey.paymentQuote = quote
        whenever(persistenceService.saveJourneyStateData(any(), eq("journey-1"))).thenReturn(mock())

        // Act
        journey.save()
        val stateCaptor = argumentCaptor<Any>()
        verify(persistenceService).saveJourneyStateData(stateCaptor.capture(), eq("journey-1"))
        val objectMapper = ObjectMapper()
        val serializedState = objectMapper.writeValueAsString(stateCaptor.firstValue)
        val restoredState = objectMapper.readValue(serializedState, Any::class.java)
        whenever(persistenceService.retrieveJourneyStateData("journey-1")).thenReturn(restoredState)
        val restoredService = JourneyStateService(MockHttpSession(), mock(), persistenceService)
        restoredService.setJourneyId("journey-1")
        val restoredJourney = createJourney(restoredService)

        // Assert
        assertTrue(serializedState.contains("paymentQuote"))
        assertEquals(quote, restoredJourney.paymentQuote)
    }

    @Test
    fun `deleting a journey removes its quote without affecting another journey`() {
        // Arrange
        val session = MockHttpSession()
        val persistenceService = mock<JourneyStatePersistenceService>()
        val journey = createJourney(createStateService(session, "journey-1", persistenceService))
        val otherJourney = createJourney(createStateService(session, "journey-2", persistenceService))
        val quote = createQuote(LocalDate.of(2027, 10, 10))
        journey.paymentQuote = quote
        otherJourney.paymentQuote = quote

        // Act
        journey.deleteJourney()

        // Assert
        assertNull(session.getAttribute("journey-1"))
        assertNull(journey.paymentQuote)
        assertEquals(quote, otherJourney.paymentQuote)
        verify(persistenceService).deleteJourneyStateData("journey-1")
    }

    @Test
    fun `deleting a base journey also removes dependent journey quotes`() {
        // Arrange
        val session = MockHttpSession()
        val persistenceService = mock<JourneyStatePersistenceService>()
        val baseService = createStateService(session, "journey-1", persistenceService)
        val journey = createJourney(baseService)
        journey.paymentQuote = createQuote(LocalDate.of(2027, 10, 10))
        baseService.copyJourneyTo("journey-2")

        // Act
        journey.deleteJourney()

        // Assert
        assertNull(session.getAttribute("journey-1"))
        assertNull(session.getAttribute("journey-2"))
        verify(persistenceService).deleteJourneyStateData("journey-1")
        verify(persistenceService).deleteJourneyStateData("journey-2")
    }

    private fun createStateService(
        session: MockHttpSession,
        journeyId: String,
        persistenceService: JourneyStatePersistenceService = mock(),
    ): JourneyStateService =
        JourneyStateService(session, mock(), persistenceService).apply {
            initialiseJourneyWithId(journeyId)
            setJourneyId(journeyId)
        }

    private fun createJourney(service: JourneyStateService) =
        PropertyRegistrationJourney(
            taskListStep = mock(),
            licensingTask = mock(),
            occupied = mock(),
            propertyDetailsTask = mock(),
            ownershipAndLandlordsTask = mock(),
            correspondenceTask = mock(),
            tenancyDetailsTask = mock(),
            whoProvidesDetailsTask = mock(),
            gasSafetyTask = mock(),
            electricalSafetyTask = mock(),
            epcTask = mock(),
            cyaStep = mock(),
            finishCyaStep = mock(),
            occupancyChangeRoutingStep = mock(),
            occupancyChangeInterruptionStep = mock(),
            hasMissingComplianceStep = mock(),
            confirmMissingComplianceStep = mock(),
            whoProvidesUpdateRoutingStep = mock(),
            confirmChangeToLettingAgentStep = mock(),
            savePropertyRegistrationDataStep = mock(),
            paymentSummaryStep = mock(),
            paymentReturnStep = mock(),
            paymentRoutingStep = mock(),
            retryablePaymentFailedStep = mock(),
            nonRetryablePaymentFailedStep = mock(),
            journeyStateService = service,
            stateFactory = mock(),
        )

    private fun createQuote(quoteDate: LocalDate) =
        PropertyRegistrationPaymentQuote(
            amountInPence = 1234,
            quoteDate = quoteDate,
            renewalDate = LocalDate.of(2028, 3, 1),
        )
}
