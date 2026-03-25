package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals

class CheckAnswersFormModelTests {
    @Test
    fun `serializeJourneyData turns journeyData's values into strings, then serializes it`() {
        val journeyDataWithStringValues = testJourneyData.mapValues { (_, value) -> value.toString() }
        val expectedSerializedData = Json.encodeToString(journeyDataWithStringValues)

        val returnedSerializedData = CheckAnswersFormModel.serializeJourneyData(testJourneyData)

        assertEquals(expectedSerializedData, returnedSerializedData)
    }

    companion object {
        val testJourneyData =
            mapOf(
                "stringKey" to "stringValue",
                "numberKey" to 123,
                "booleanKey" to true,
                "dateKey" to LocalDate.of(2021, 1, 1),
                "mapKey" to
                    mapOf(
                        "stringKey" to "stringValue",
                        "numberKey" to 123,
                        "booleanKey" to true,
                        "dateKey" to LocalDate.of(2021, 1, 1),
                    ),
            )
    }
}
