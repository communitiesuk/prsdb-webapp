package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.ownership

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.helpers.NotificationHelper
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.UnrecoverableJourneyStateException
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.anyoneInOwnershipChain
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.getFreeholdersOrNull
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.getSuperiorLandlordsOrNull
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel
import uk.gov.communities.prsdb.webapp.services.AssociatedPartyNotificationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyUpdateEmailService

@JourneyFrameworkComponent
class CompleteOwnershipUpdateStepConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val propertyUpdateEmailService: PropertyUpdateEmailService,
    private val associatedPartyNotificationService: AssociatedPartyNotificationService,
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

        val (addedFreeholders, addedSuperiorLandlords) =
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

        sendAddedAssociatedPartyNotifications(state.propertyId, addedFreeholders, addedSuperiorLandlords)
        propertyUpdateEmailService.sendUpdateEmails(state.propertyId, listOf("The property’s ownership"))
    }

    // Use the submitted details for notifications: postal addresses aren't saved and can't be retrieved from the database.
    private fun sendAddedAssociatedPartyNotifications(
        propertyId: Long,
        addedFreeholders: List<FreeholderDataModel>,
        addedSuperiorLandlords: List<SuperiorLandlordDataModel>,
    ) {
        if (addedFreeholders.isEmpty() && addedSuperiorLandlords.isEmpty()) return

        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(propertyId)
        NotificationHelper.sendAddedToPropertyNotifications("freeholder", addedFreeholders, propertyId) { freeholder ->
            associatedPartyNotificationService.sendNotificationToFreeholderAddedToProperty(freeholder, propertyOwnership)
        }
        NotificationHelper.sendAddedToPropertyNotifications("superior landlord", addedSuperiorLandlords, propertyId) { superiorLandlord ->
            associatedPartyNotificationService.sendNotificationToSuperiorLandlordAddedToProperty(superiorLandlord, propertyOwnership)
        }
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
