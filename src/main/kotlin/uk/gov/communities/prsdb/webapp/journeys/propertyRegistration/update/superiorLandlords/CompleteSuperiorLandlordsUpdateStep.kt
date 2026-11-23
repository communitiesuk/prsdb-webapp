package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.superiorLandlords

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
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.getSuperiorLandlordsOrNull
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel
import uk.gov.communities.prsdb.webapp.services.AssociatedPartyNotificationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyUpdateEmailService

@JourneyFrameworkComponent
class CompleteSuperiorLandlordsUpdateStepConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val associatedPartyNotificationService: AssociatedPartyNotificationService,
    private val propertyUpdateEmailService: PropertyUpdateEmailService,
) : AbstractInternalStepConfig<Complete, UpdateSuperiorLandlordsJourneyState>() {
    override fun mode(state: UpdateSuperiorLandlordsJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: UpdateSuperiorLandlordsJourneyState) {
        val superiorLandlordsTask = state.superiorLandlordsTask
        val superiorLandlords =
            if (superiorLandlordsTask.anyoneInOwnershipChain == true) {
                superiorLandlordsTask.getSuperiorLandlordsOrNull()?.values?.toList()
                    ?: throw UnrecoverableJourneyStateException(
                        state.journeyId,
                        "Superior landlords update completed with anyone in ownership chain answered yes but no superior landlords",
                    )
            } else {
                emptyList()
            }

        val addedSuperiorLandlords =
            try {
                propertyOwnershipService.updateSuperiorLandlords(
                    id = state.propertyId,
                    superiorLandlords = superiorLandlords,
                    initialLastModifiedDate = Instant.parse(state.lastModifiedDate).toJavaInstant(),
                )
            } catch (ex: UpdateConflictException) {
                state.deleteJourney()
                throw ex
            }

        sendAddedSuperiorLandlordNotifications(state.propertyId, addedSuperiorLandlords)
        propertyUpdateEmailService.sendUpdateEmails(state.propertyId, listOf("People in the property’s ownership chain"))
    }

    // Use the submitted details for notifications: postal addresses aren't saved and can't be retrieved from the database.
    private fun sendAddedSuperiorLandlordNotifications(
        propertyId: Long,
        addedSuperiorLandlords: List<SuperiorLandlordDataModel>,
    ) {
        if (addedSuperiorLandlords.isEmpty()) return

        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(propertyId)
        NotificationHelper.sendAddedToPropertyNotifications("superior landlord", addedSuperiorLandlords, propertyId) { superiorLandlord ->
            associatedPartyNotificationService.sendNotificationToSuperiorLandlordAddedToProperty(superiorLandlord, propertyOwnership)
        }
    }

    override fun resolveNextDestination(
        state: UpdateSuperiorLandlordsJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent
class CompleteSuperiorLandlordsUpdateStep(
    stepConfig: CompleteSuperiorLandlordsUpdateStepConfig,
) : JourneyStep.InternalStep<Complete, UpdateSuperiorLandlordsJourneyState>(stepConfig)
