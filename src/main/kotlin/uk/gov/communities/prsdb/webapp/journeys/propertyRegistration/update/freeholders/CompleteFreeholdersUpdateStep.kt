package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.freeholders

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.UnrecoverableJourneyStateException
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class CompleteFreeholdersUpdateStepConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
) : AbstractInternalStepConfig<Complete, UpdateFreeholdersJourneyState>() {
    override fun mode(state: UpdateFreeholdersJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: UpdateFreeholdersJourneyState) {
        val freeholders =
            state.freeholdersMap?.values?.toList()
                ?: throw UnrecoverableJourneyStateException(state.journeyId, "Freeholders update completed without any freeholders")

        try {
            propertyOwnershipService.updateFreeholders(
                id = state.propertyId,
                freeholders = freeholders,
                initialLastModifiedDate = Instant.parse(state.lastModifiedDate).toJavaInstant(),
            )
        } catch (ex: UpdateConflictException) {
            state.deleteJourney()
            throw ex
        }

        // TODO PDJB-1987, PDJB-1988: notify the added freeholders returned by updateFreeholders here.
        //  Their postal addresses only exist in journey state (PDJB-2226).
    }

    override fun resolveNextDestination(
        state: UpdateFreeholdersJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent
class CompleteFreeholdersUpdateStep(
    stepConfig: CompleteFreeholdersUpdateStepConfig,
) : JourneyStep.InternalStep<Complete, UpdateFreeholdersJourneyState>(stepConfig)
