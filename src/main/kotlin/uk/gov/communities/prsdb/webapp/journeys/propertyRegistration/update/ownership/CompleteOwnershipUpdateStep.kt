package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.ownership

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.UnrecoverableJourneyStateException
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.anyoneInOwnershipChain
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.getFreeholdersOrNull
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.getSuperiorLandlordsOrNull
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyUpdateEmailService

@JourneyFrameworkComponent
class CompleteOwnershipUpdateStepConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val propertyUpdateEmailService: PropertyUpdateEmailService,
) : AbstractInternalStepConfig<Complete, UpdateOwnershipJourneyState>() {
    override fun mode(state: UpdateOwnershipJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: UpdateOwnershipJourneyState) {
        val ownershipStructureTask = state.ownershipStructureTask
        val ownershipType =
            ownershipStructureTask.ownershipTypeTask.ownershipType
                ?: throw UnrecoverableJourneyStateException(state.journeyId, "Ownership update completed without an ownership type")

        val freeholders =
            if (ownershipType.requiresFreeholderRecords()) {
                ownershipStructureTask.freeholdersTask.getFreeholdersOrNull()?.values?.toList()
                    ?: throw UnrecoverableJourneyStateException(
                        state.journeyId,
                        "Ownership update completed without the required freeholders",
                    )
            } else {
                emptyList()
            }

        val superiorLandlordsTask = ownershipStructureTask.superiorLandlordsTask
        val superiorLandlords =
            if (ownershipType.requiresSuperiorLandlordInfo() && superiorLandlordsTask.anyoneInOwnershipChain == true) {
                superiorLandlordsTask.getSuperiorLandlordsOrNull()?.values?.toList()
                    ?: throw UnrecoverableJourneyStateException(
                        state.journeyId,
                        "Ownership update completed with anyone in ownership chain answered yes but no superior landlords",
                    )
            } else {
                emptyList()
            }

        try {
            propertyOwnershipService.updateOwnershipStructure(
                id = state.propertyId,
                ownershipType = ownershipType,
                freeholders = freeholders,
                superiorLandlords = superiorLandlords,
                initialLastModifiedDate = Instant.parse(state.lastModifiedDate).toJavaInstant(),
            )
        } catch (ex: UpdateConflictException) {
            state.deleteJourney()
            throw ex
        }

        // TODO PDJB-1992: notify the added freeholders and superior landlords returned by updateOwnershipStructure here.
        //  Their postal addresses only exist in journey state (PDJB-2226).
        propertyUpdateEmailService.sendUpdateEmails(state.propertyId, listOf("The property’s ownership"))
    }

    override fun resolveNextDestination(
        state: UpdateOwnershipJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent
class CompleteOwnershipUpdateStep(
    stepConfig: CompleteOwnershipUpdateStepConfig,
) : JourneyStep.InternalStep<Complete, UpdateOwnershipJourneyState>(stepConfig)
