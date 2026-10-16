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
import java.security.MessageDigest
import java.time.LocalDate
import java.util.Base64

class AbstractCheckYourAnswersStepConfigTests {
    private val mockState = mock<CheckYourAnswersJourneyState>()
    private val stepConfig =
        object : AbstractCheckYourAnswersStepConfig<CheckYourAnswersJourneyState>() {
            override fun getStepSpecificContent(state: CheckYourAnswersJourneyState) = emptyMap<String, Any?>()
        }

    @Nested
    inner class SerializeJourneyDataHashes {
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
        fun `replaces journeyData's values with SHA-256 hashes of them as strings, then serializes it`() {
            val journeyDataHashes =
                testJourneyData.mapValues { (_, value) ->
                    Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(value.toString().toByteArray()))
                }
            val expectedSerializedData = Json.encodeToString(journeyDataHashes)

            val returnedSerializedData = CheckAnswersFormModel.serializeJourneyDataHashes(testJourneyData)

            assertEquals(expectedSerializedData, returnedSerializedData)
        }
    }

    private enum class TestEnum {
        TEST_VALUE,
    }

    @Nested
    inner class ResolvePageContent {
        @Test
        fun `adds the snapshot of the answers shown on the CYA page as submittedFilteredJourneyData`() {
            whenever(mockState.getCyaAnswersSnapshot()).thenReturn("cya-answers-snapshot")

            val content = stepConfig.resolvePageContent(mockState, mapOf("existingKey" to "existingValue"))

            assertEquals(
                mapOf("existingKey" to "existingValue", "submittedFilteredJourneyData" to "cya-answers-snapshot"),
                content,
            )
        }
    }

    @Nested
    inner class EnrichSubmittedDataBeforeValidation {
        @Test
        fun `throws CyaDataHasChangedException when the answers shown on the CYA page have changed since it loaded`() {
            whenever(mockState.getCyaAnswersSnapshot()).thenReturn("current-snapshot")
            val formData = mapOf("submittedFilteredJourneyData" to "snapshot-when-page-loaded")

            assertThrows<CyaDataHasChangedException> {
                stepConfig.enrichSubmittedDataBeforeValidation(mockState, formData)
            }
        }

        @Test
        fun `does not throw when the answers shown on the CYA page are unchanged`() {
            whenever(mockState.getCyaAnswersSnapshot()).thenReturn("snapshot")
            val formData = mapOf("submittedFilteredJourneyData" to "snapshot")

            assertDoesNotThrow {
                stepConfig.enrichSubmittedDataBeforeValidation(mockState, formData)
            }
        }

        @Test
        fun `does not throw when no snapshot was submitted`() {
            assertDoesNotThrow {
                stepConfig.enrichSubmittedDataBeforeValidation(mockState, emptyMap())
            }
        }
    }
}
