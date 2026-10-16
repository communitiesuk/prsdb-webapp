package uk.gov.communities.prsdb.webapp.testHelpers

import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.journeys.FormData
import uk.gov.communities.prsdb.webapp.testHelpers.api.requestModels.SetJourneyStateRequestModel
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertIs

class JourneyStateDeserializerTests {
    @Test
    fun `journey state serialization preserves nested values and dates`() {
        val date = LocalDate.of(2026, 10, 12)
        val state: FormData =
            mapOf(
                "date" to date,
                "text" to "not a date",
                "missing" to null,
                "nested" to
                    mapOf(
                        "values" to listOf(1, true, null, "text", date),
                        "objects" to listOf(mapOf("date" to date, "values" to listOf(1, null, true))),
                        "emptyObject" to emptyMap<String, Any?>(),
                        "amount" to 12.5,
                        "largeNumber" to 4_000_000_000L,
                    ),
            )

        val request = SetJourneyStateRequestModel("journey", state)

        assertEquals(state, request.getJourneyState())
    }

    @Test
    fun `empty and singleton journey arrays remain growable for checkbox binding`() {
        listOf(emptyList<String>(), listOf("COMPANY")).forEach { selection ->
            val request = SetJourneyStateRequestModel("journey", mapOf("selection" to selection))
            val restoredSelection = assertIs<MutableList<Any?>>(request.getJourneyState()["selection"])

            restoredSelection.add(null)

            assertEquals(selection.size + 1, restoredSelection.size)
        }
    }
}
