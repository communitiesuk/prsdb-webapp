package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.freeholders

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.helpers.NotificationHelper
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.UnrecoverableJourneyStateException
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel
import uk.gov.communities.prsdb.webapp.services.AssociatedPartyNotificationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class CompleteFreeholdersUpdateStepConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val associatedPartyNotificationService: AssociatedPartyNotificationService,
) : AbstractInternalStepConfig<Complete, UpdateFreeholdersJourneyState>() {
    override fun mode(state: UpdateFreeholdersJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: UpdateFreeholdersJourneyState) {
        val freeholders =
            state.freeholdersMap?.values?.toList()
                ?: throw UnrecoverableJourneyStateException(state.journeyId, "Freeholders update completed without any freeholders")

        val addedFreeholders =
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

        sendAddedFreeholderNotifications(state.propertyId, addedFreeholders)
    }

    // Use the submitted details for notifications: postal addresses aren't saved and can't be retrieved from the database.
    private fun sendAddedFreeholderNotifications(
        propertyId: Long,
        addedFreeholders: List<FreeholderDataModel>,
    ) {
        if (addedFreeholders.isEmpty()) return

        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(propertyId)
        NotificationHelper.sendAddedToPropertyNotifications("freeholder", addedFreeholders, propertyId) { freeholder ->
            associatedPartyNotificationService.sendNotificationToFreeholderAddedToProperty(freeholder, propertyOwnership)
        }
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
