package uk.gov.communities.prsdb.webapp.journeys.systemOperator.landlordNotes

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.services.LandlordService

@JourneyFrameworkComponent
class CompleteSystemOperatorLandlordNotesUpdateStepConfig(
    private val landlordService: LandlordService,
) : AbstractInternalStepConfig<Complete, UpdateSystemOperatorLandlordNotesJourneyState>() {
    override fun mode(state: UpdateSystemOperatorLandlordNotesJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: UpdateSystemOperatorLandlordNotesJourneyState) {
        try {
            landlordService.updateSystemOperatorNotes(
                landlordId = state.landlordId,
                notes = state.notesStep.formModel.notes,
                initialLastModifiedDate = Instant.parse(state.lastModifiedDate).toJavaInstant(),
            )
        } catch (ex: UpdateConflictException) {
            state.deleteJourney()
            throw ex
        }
    }

    override fun resolveNextDestination(
        state: UpdateSystemOperatorLandlordNotesJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent
class CompleteSystemOperatorLandlordNotesUpdateStep(
    stepConfig: CompleteSystemOperatorLandlordNotesUpdateStepConfig,
) : JourneyStep.InternalStep<Complete, UpdateSystemOperatorLandlordNotesJourneyState>(stepConfig)
