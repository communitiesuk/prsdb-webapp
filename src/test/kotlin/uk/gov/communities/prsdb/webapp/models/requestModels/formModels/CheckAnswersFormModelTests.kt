package uk.gov.communities.prsdb.webapp.models.requestModels.formModels

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CheckAnswersFormModelTests {
    @Test
    fun `serializeJourneyData turns journeyData's values into strings, then serializes it`() {
        val journeyDataWithStringValues = testJourneyData.mapValues { (_, value) -> value.toString() }
        val expectedSerializedData = Json.encodeToString(journeyDataWithStringValues)

        val returnedSerializedData = CheckAnswersFormModel.serializeJourneyData(testJourneyData)

        assertEquals(expectedSerializedData, returnedSerializedData)
    }

    @Test
    fun `isFilteredJourneyDataUnchanged returns true if the submittedFilteredJourneyData values match the storedJourneyData`() {
        val submittedFilteredJourneyData = testJourneyData
        val storedJourneyData = submittedFilteredJourneyData + ("otherKey" to "otherValue")
        val formModel = createCheckAnswersFormModel(submittedFilteredJourneyData, storedJourneyData)

        assertTrue(formModel.isFilteredJourneyDataUnchanged())
    }

    @Test
    fun `isFilteredJourneyDataUnchanged returns false if the submittedFilteredJourneyData values don't match the storedJourneyData`() {
        val submittedFilteredJourneyData = testJourneyData
        val storedJourneyData = submittedFilteredJourneyData + (testJourneyData.keys.first() to "otherValue")
        val formModel = createCheckAnswersFormModel(submittedFilteredJourneyData, storedJourneyData)

        assertFalse(formModel.isFilteredJourneyDataUnchanged())
    }

    @Test
    fun `isFilteredJourneyDataUnchanged returns false if the submittedFilteredJourneyData values don't exist in the storedJourneyData`() {
        val submittedFilteredJourneyData = testJourneyData
        val storedJourneyData = submittedFilteredJourneyData - testJourneyData.keys.first()
        val formModel = createCheckAnswersFormModel(submittedFilteredJourneyData, storedJourneyData)

        assertFalse(formModel.isFilteredJourneyDataUnchanged())
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

        private fun createCheckAnswersFormModel(
            submittedFilteredJourneyData: JourneyData,
            storedJourneyData: JourneyData,
        ): CheckAnswersFormModel =
            CheckAnswersFormModel().apply {
                this.submittedFilteredJourneyData = CheckAnswersFormModel.serializeJourneyData(submittedFilteredJourneyData)
                this.storedJourneyData = storedJourneyData
            }
    }
}
