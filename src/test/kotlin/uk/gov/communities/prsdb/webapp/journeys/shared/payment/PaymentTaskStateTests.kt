package uk.gov.communities.prsdb.webapp.journeys.shared.payment

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.mock.web.MockHttpSession
import tools.jackson.databind.ObjectMapper
import uk.gov.communities.prsdb.webapp.journeys.JourneyStatePersistenceService
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyPaymentQuote
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PaymentTaskStateTests {
    @Test
    fun `payment quote survives saving and restoring journey state in a new session`() {
        // Arrange
        val persistenceService = mock<JourneyStatePersistenceService>()
        val journeyStateService = JourneyStateService(MockHttpSession(), mock(), persistenceService)
        journeyStateService.initialiseJourneyWithId("journey-1")
        journeyStateService.setJourneyId("journey-1")
        val task = createTask(journeyStateService)
        val quote = createQuote(LocalDate.of(2027, 10, 10))
        task.paymentQuote = quote
        whenever(persistenceService.saveJourneyStateData(any(), eq("journey-1"))).thenReturn(mock())

        // Act
        task.save()
        val stateCaptor = argumentCaptor<Any>()
        verify(persistenceService).saveJourneyStateData(stateCaptor.capture(), eq("journey-1"))
        val objectMapper = ObjectMapper()
        val serializedState = objectMapper.writeValueAsString(stateCaptor.firstValue)
        val restoredState = objectMapper.readValue(serializedState, Any::class.java)
        whenever(persistenceService.retrieveJourneyStateData("journey-1")).thenReturn(restoredState)
        val restoredService = JourneyStateService(MockHttpSession(), mock(), persistenceService)
        restoredService.setJourneyId("journey-1")
        val restoredTask = createTask(restoredService)

        // Assert
        assertTrue(serializedState.contains("paymentQuote"))
        assertEquals(quote, restoredTask.paymentQuote)
    }

    @Test
    fun `payment quote saved without a chargeable period start date restores with the quote date as the chargeable period start date`() {
        // Arrange
        val savedQuote = """{"amountInPence":1234,"quoteDate":"2027-10-10","renewalDate":"2028-03-01"}"""

        // Act
        val restoredQuote = Json.decodeFromString<PropertyPaymentQuote>(savedQuote)

        // Assert
        assertEquals(LocalDate.of(2027, 10, 10), restoredQuote.chargeablePeriodStartDate)
    }

    private fun createTask(service: JourneyStateService) =
        PaymentTask(
            journeyStateService = service,
            paymentSummaryStep = mock(),
            paymentReturnStep = mock(),
            paymentStatusCheckStep = mock(),
            paymentFailedStep = mock(),
        )

    private fun createQuote(quoteDate: LocalDate) =
        PropertyPaymentQuote(
            amountInPence = 1234,
            quoteDate = quoteDate,
            renewalDate = LocalDate.of(2028, 3, 1),
        )
}
