package uk.gov.communities.prsdb.webapp.journeys.systemOperator.notes

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import java.time.Instant

abstract class AbstractSystemOperatorNotesJourneyFactory<TJourney : AbstractSystemOperatorNotesJourney>(
    private val stateFactory: ObjectFactory<TJourney>,
) {
    protected abstract fun getRecordLastModifiedDate(recordId: Long): Instant

    protected abstract fun getRecordRoute(recordId: Long): String

    fun createJourneySteps(recordId: Long): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()
        if (!state.isStateInitialized) {
            state.recordId = recordId
            state.lastModifiedDate = getRecordLastModifiedDate(recordId).toString()
            state.isStateInitialized = true
        }
        if (state.recordId != recordId) {
            throw PrsdbWebException("Journey state recordId ${state.recordId} does not match provided recordId $recordId")
        }

        val recordRoute = getRecordRoute(recordId)

        return journey(state) {
            unreachableStepUrl { recordRoute }
            step(journey.notesStep) {
                routeSegment(AbstractSystemOperatorNotesStep.ROUTE_SEGMENT)
                backUrl { recordRoute }
                nextStep { journey.completeNotesUpdateStep }
                initialStep()
            }
            step(journey.completeNotesUpdateStep) {
                parents { journey.notesStep.hasOutcome(Complete.COMPLETE) }
                nextUrl { recordRoute }
            }
        }
    }

    fun initializeJourneyState(
        seed: Any?,
        currentLastModifiedDate: Instant,
    ): String = stateFactory.getObject().initialiseOrRestoreStateReinitialisingIfOutdated(seed, currentLastModifiedDate)
}
