package uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.exceptions.CyaDataHasChangedException
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStepConfig.Companion.checkJourneyNotModifiedSincePageLoad
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel

class AbstractCheckYourAnswersStepConfigTests {
    @Nested
    inner class CheckJourneyNotModifiedSincePageLoad {
        @Test
        fun `throws CyaDataHasChangedException when journey data has changed`() {
            val state = mock<JourneyState>()
            val originalStepData = mapOf("step1" to mapOf("field" to "original"))
            val modifiedStepData = mapOf("step1" to mapOf("field" to "modified"))
            whenever(state.getSubmittedStepData()).thenReturn(modifiedStepData)

            val formData = mapOf("submittedFilteredJourneyData" to CheckAnswersFormModel.serializeJourneyData(originalStepData))

            assertThrows<CyaDataHasChangedException> {
                checkJourneyNotModifiedSincePageLoad(state, formData)
            }
        }

        @Test
        fun `does not throw when journey data is unchanged`() {
            val state = mock<JourneyState>()
            val stepData = mapOf("step1" to mapOf("field" to "value"))
            whenever(state.getSubmittedStepData()).thenReturn(stepData)

            val formData = mapOf("submittedFilteredJourneyData" to CheckAnswersFormModel.serializeJourneyData(stepData))

            assertDoesNotThrow {
                checkJourneyNotModifiedSincePageLoad(state, formData)
            }
        }

        @Test
        fun `does not throw when submittedFilteredJourneyData is absent from form data`() {
            val state = mock<JourneyState>()

            val formData = emptyMap<String, Any?>()

            assertDoesNotThrow {
                checkJourneyNotModifiedSincePageLoad(state, formData)
            }
        }
    }
}
