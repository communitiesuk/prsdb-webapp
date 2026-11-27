package uk.gov.communities.prsdb.webapp.journeys.systemOperator.notes

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete

abstract class AbstractCompleteSystemOperatorNotesUpdateStepConfig :
    AbstractInternalStepConfig<Complete, SystemOperatorNotesJourneyState>() {
    protected abstract fun updateNotes(
        recordId: Long,
        notes: String,
        initialLastModifiedDate: java.time.Instant,
    )

    override fun mode(state: SystemOperatorNotesJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: SystemOperatorNotesJourneyState) {
        try {
            updateNotes(
                recordId = state.recordId,
                notes = state.notesStep.formModel.notes,
                initialLastModifiedDate = Instant.parse(state.lastModifiedDate).toJavaInstant(),
            )
        } catch (ex: UpdateConflictException) {
            state.deleteJourney()
            throw ex
        }
    }

    override fun resolveNextDestination(
        state: SystemOperatorNotesJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

abstract class AbstractCompleteSystemOperatorNotesUpdateStep(
    stepConfig: AbstractCompleteSystemOperatorNotesUpdateStepConfig,
) : JourneyStep.InternalStep<Complete, SystemOperatorNotesJourneyState>(stepConfig)
