package uk.gov.communities.prsdb.webapp.journeys.shared

import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateDelegateProvider
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.builders.ConfigurableElement
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckAnswersFormModel

interface CheckYourAnswersPartialJourneyState {
    val cyaStep: JourneyStep.RequestableStep<Complete, CheckAnswersFormModel, *>
    val baseJourneyId: String
    val cyaChildJourneyId: String?
    val isCheckingAnswers: Boolean

    companion object {
        fun <T> JourneyBuilder<T>.checkYourAnswersJourney() where T : JourneyState, T : CheckYourAnswersPartialJourneyState {
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

        private const val CHECKABLE = "checkable"
    }
}

class CheckYourAnswersPartialJourneyStateProxy(
    delegateProvider: JourneyStateDelegateProvider,
    private val journeyStateService: JourneyStateService,
    override val cyaStep: JourneyStep.RequestableStep<Complete, CheckAnswersFormModel, *>,
) : CheckYourAnswersPartialJourneyState {
    override var cyaChildJourneyId: String? by delegateProvider.mutableDelegate("cyaChildJourneyId")

    override val baseJourneyId: String
        get() = journeyStateService.journeyMetadata.baseJourneyId ?: journeyStateService.journeyId

    override val isCheckingAnswers: Boolean
        get() = journeyStateService.journeyMetadata.childJourneyName != null
}
