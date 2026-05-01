package uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.exceptions.CyaDataHasChangedException
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel
import java.time.LocalDate

class AbstractCheckYourAnswersStepConfigTests {
    private val mockState = mock<CheckYourAnswersJourneyState>()
    private val stepConfig =
        object : AbstractCheckYourAnswersStepConfig<CheckYourAnswersJourneyState>() {
            override fun getStepSpecificContent(state: CheckYourAnswersJourneyState) = emptyMap<String, Any?>()
        }

    @Nested
    inner class SerializeJourneyData {
        private val testJourneyData =
            mapOf(
                "stringKey" to "stringValue",
                "numberKey" to 123,
                "booleanKey" to true,
                "dateKey" to LocalDate.of(2021, 1, 1),
                "nullKey" to null,
                "enumKey" to TestEnum.TEST_VALUE,
                "listKey" to listOf("a", "b", "c"),
                "mapKey" to
                    mapOf(
                        "stringKey" to "stringValue",
                        "numberKey" to 123,
                        "booleanKey" to true,
                        "dateKey" to LocalDate.of(2021, 1, 1),
                    ),
            )

        @Test
        fun `turns journeyData's values into strings, then serializes it`() {
            val journeyDataWithStringValues = testJourneyData.mapValues { (_, value) -> value.toString() }
            val expectedSerializedData = Json.encodeToString(journeyDataWithStringValues)

            val returnedSerializedData = CheckAnswersFormModel.serializeJourneyData(testJourneyData)

            assertEquals(expectedSerializedData, returnedSerializedData)
        }
    }

    private enum class TestEnum {
        TEST_VALUE,
    }

    @Nested
    inner class EnrichSubmittedDataBeforeValidation {
        @Test
        fun `throws CyaDataHasChangedException when journey data has changed`() {
            val originalStepData = mapOf("step1" to mapOf("field" to "original"))
            val modifiedStepData = mapOf("step1" to mapOf("field" to "modified"))
            whenever(mockState.getSubmittedStepData()).thenReturn(modifiedStepData)

            val formData = mapOf("submittedFilteredJourneyData" to CheckAnswersFormModel.serializeJourneyData(originalStepData))

            assertThrows<CyaDataHasChangedException> {
                stepConfig.enrichSubmittedDataBeforeValidation(mockState, formData)
            }
        }

        @Test
        fun `does not throw when journey data is unchanged`() {
            val stepData = mapOf("step1" to mapOf("field" to "value"))
            whenever(mockState.getSubmittedStepData()).thenReturn(stepData)

            val formData = mapOf("submittedFilteredJourneyData" to CheckAnswersFormModel.serializeJourneyData(stepData))

            assertDoesNotThrow {
                stepConfig.enrichSubmittedDataBeforeValidation(mockState, formData)
            }
        }

        @Test
        fun `does not throw when data was changed and page was re-rendered with the new data`() {
            val updatedStepData = mapOf("step1" to mapOf("field" to "newValue"))
            whenever(mockState.getSubmittedStepData()).thenReturn(updatedStepData)

            val formData = mapOf("submittedFilteredJourneyData" to CheckAnswersFormModel.serializeJourneyData(updatedStepData))

            assertDoesNotThrow {
                stepConfig.enrichSubmittedDataBeforeValidation(mockState, formData)
            }
        }
    }
}
