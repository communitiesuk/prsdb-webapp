package uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig

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

class AbstractCheckYourAnswersStepConfigTests {
    private val mockState = mock<CheckYourAnswersJourneyState>()
    private val stepConfig =
        object : AbstractCheckYourAnswersStepConfig<CheckYourAnswersJourneyState>() {
            override fun getStepSpecificContent(state: CheckYourAnswersJourneyState) = emptyMap<String, Any?>()
        }

    @Nested
    inner class SerializeJourneyData {
        @Test
        fun `converts non-string values to strings`() {
            val data = mapOf("num" to 42, "bool" to true)
            val result = CheckAnswersFormModel.serializeJourneyData(data)
            assertEquals("""{"num":"42","bool":"true"}""", result)
        }
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
