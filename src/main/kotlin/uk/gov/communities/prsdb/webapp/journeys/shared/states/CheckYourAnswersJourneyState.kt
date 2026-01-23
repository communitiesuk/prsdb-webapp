package uk.gov.communities.prsdb.webapp.journeys.shared.states

import uk.gov.communities.prsdb.webapp.constants.ReservedTagValues
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.builders.ConfigurableElement
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep

interface CheckYourAnswersJourneyState : JourneyState {
    val cyaStep: AbstractCheckYourAnswersStep<*>

    var cyaChildJourneyIdIfInitialized: String?

    val baseJourneyId: String
        get() = journeyMetadata.baseJourneyId ?: journeyId

    val isCheckingAnswers: Boolean
        get() = journeyMetadata.childJourneyName == CHECK_ANSWERS_JOURNEY_NAME

    fun initialiseCyaChildJourney() {
        cyaChildJourneyIdIfInitialized = initializeChildState(CHECK_ANSWERS_JOURNEY_NAME)
    }

    companion object {
        fun <T> JourneyBuilder<T>.checkYourAnswersJourney() where T : CheckYourAnswersJourneyState {
            configureTagged(CHECKABLE) {
                if (journey.isCheckingAnswers) {
                    modifyNextDestination {
                        { Destination.VisitableStep(journey.cyaStep, journey.baseJourneyId) }
                    }
                    backDestination { Destination.VisitableStep(journey.cyaStep, journey.baseJourneyId) }
                }
            }
        }

        fun ConfigurableElement<*>.checkable() = taggedWith(CHECKABLE)

        private const val CHECKABLE = ReservedTagValues.CHECKABLE
        private const val CHECK_ANSWERS_JOURNEY_NAME = "checkYourAnswers"
    }
}
