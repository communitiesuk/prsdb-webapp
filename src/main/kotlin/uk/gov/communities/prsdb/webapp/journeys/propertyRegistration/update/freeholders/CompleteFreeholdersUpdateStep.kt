package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.freeholders

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.services.FreeholderService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class CompleteFreeholdersUpdateStepConfig(
    private val freeholderService: FreeholderService,
    private val propertyOwnershipService: PropertyOwnershipService,
) : AbstractInternalStepConfig<Complete, UpdateFreeholdersJourneyState>() {
    override fun mode(state: UpdateFreeholdersJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: UpdateFreeholdersJourneyState) {
        try {
            freeholderService.updateFreeholders(
                propertyOwnership = propertyOwnershipService.getPropertyOwnership(state.propertyId),
                freeholders =
                    state.freeholdersMap
                        ?.values
                        ?.toList()
                        ?: throw PrsdbWebException(
                            "Freeholder update journey for property ${state.propertyId} has no freeholder list.",
                        ),
                expectedSnapshot = state.freeholderSnapshot,
            )
        } catch (ex: UpdateConflictException) {
            state.deleteJourney()
            throw ex
        }
        // TODO PDJB-1987, PDJB-1988: send creation notifications to newly added freeholders once the email template is available.
        //  Match the returned entities to the submitted journey data to use postal addresses, which are not persisted.
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
