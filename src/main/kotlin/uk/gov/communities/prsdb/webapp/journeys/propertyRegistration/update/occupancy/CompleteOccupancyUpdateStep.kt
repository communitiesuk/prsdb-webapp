package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.occupancy

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.database.entity.LettingAgentAccess
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OccupancyFormModel
import uk.gov.communities.prsdb.webapp.services.DelegateToLettingAgentEmailService
import uk.gov.communities.prsdb.webapp.services.LettingAgentAccessService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyUpdateEmailService

@JourneyFrameworkComponent
class CompleteOccupancyUpdateStepConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val propertyUpdateEmailService: PropertyUpdateEmailService,
    private val lettingAgentAccessService: LettingAgentAccessService,
    private val delegateToLettingAgentEmailService: DelegateToLettingAgentEmailService,
) : AbstractInternalStepConfig<Complete, UpdateOccupancyJourneyState>() {
    override fun mode(state: UpdateOccupancyJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: UpdateOccupancyJourneyState) {
        val isOccupied = state.occupied.formModel.notNullValue(OccupancyFormModel::occupied)
        val lettingAgentAccess = propertyOwnershipService.getLettingAgentAccess(state.propertyId)
        try {
            propertyOwnershipService.updateIsOccupied(
                id = state.propertyId,
                isOccupied = isOccupied,
                initialLastModifiedDate = Instant.parse(state.lastModifiedDate).toJavaInstant(),
            )
        } catch (ex: UpdateConflictException) {
            state.deleteJourney()
            throw ex
        }
        sendEmails(state, isOccupied, lettingAgentAccess)
    }

    private fun sendEmails(
        state: UpdateOccupancyJourneyState,
        isOccupied: Boolean,
        lettingAgentAccess: LettingAgentAccess?,
    ) {
        if (!isOccupied && lettingAgentAccess != null) {
            removeLettingAgentDelegationAndSendEmails(state.propertyId, lettingAgentAccess.invitedEmail)
        } else {
            propertyUpdateEmailService.sendUpdateEmails(state.propertyId, listOf("Whether the property is occupied by tenants"))
        }
    }

    private fun removeLettingAgentDelegationAndSendEmails(
        propertyId: Long,
        lettingAgentEmail: String,
    ) {
        lettingAgentAccessService.deleteDelegationByPropertyOwnershipId(propertyId)
        propertyUpdateEmailService.sendUpdateWithLettingAgentRemovedEmails(
            propertyId,
            "The property was made unoccupied",
            lettingAgentEmail,
        )
        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(propertyId)
        delegateToLettingAgentEmailService.sendLettingAgentCancellationEmail(propertyOwnership, lettingAgentEmail)
    }

    override fun resolveNextDestination(
        state: UpdateOccupancyJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent
class CompleteOccupancyUpdateStep(
    stepConfig: CompleteOccupancyUpdateStepConfig,
) : JourneyStep.InternalStep<Complete, UpdateOccupancyJourneyState>(stepConfig)
