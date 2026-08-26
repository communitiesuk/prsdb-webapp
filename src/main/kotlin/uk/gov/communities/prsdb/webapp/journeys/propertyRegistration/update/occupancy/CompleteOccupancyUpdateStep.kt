package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.occupancy

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OccupancyFormModel
import uk.gov.communities.prsdb.webapp.services.LettingAgentAccessService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyUpdateEmailService

@JourneyFrameworkComponent
class CompleteOccupancyUpdateStepConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val propertyUpdateEmailService: PropertyUpdateEmailService,
    private val lettingAgentAccessService: LettingAgentAccessService,
    private val featureFlagManager: FeatureFlagManager,
) : AbstractInternalStepConfig<Complete, UpdateOccupancyJourneyState>() {
    override fun mode(state: UpdateOccupancyJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: UpdateOccupancyJourneyState) {
        val isOccupied = state.occupied.formModel.notNullValue(OccupancyFormModel::occupied)
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
        sendEmails(state, isOccupied)
    }

    private fun sendEmails(
        state: UpdateOccupancyJourneyState,
        isOccupied: Boolean,
    ) {
        val lettingAgentAccess = lettingAgentAccessService.getInvitationByPropertyOwnershipId(state.propertyId)

        if (featureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT) && !isOccupied && lettingAgentAccess != null) {
            // TODO: PDJB-1633: Delete the LettingAgentAccess row when becoming unoccupied
            propertyUpdateEmailService.sendUpdateWithLettingAgentRemovedEmails(
                state.propertyId,
                "The property was made unoccupied",
                lettingAgentAccess.invitedEmail,
            )
        } else {
            propertyUpdateEmailService.sendUpdateEmails(state.propertyId, listOf("Whether the property is occupied by tenants"))
        }
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
