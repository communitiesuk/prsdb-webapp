package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.propertyManager

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyManagerDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyManagerDetailsFormModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyUpdateEmailService

@JourneyFrameworkComponent
class CompletePropertyManagerUpdateStepConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val propertyUpdateEmailService: PropertyUpdateEmailService,
) : AbstractInternalStepConfig<Complete, UpdatePropertyManagerJourneyState>() {
    override fun mode(state: UpdatePropertyManagerJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: UpdatePropertyManagerJourneyState) {
        val updatedPropertyManager = getUpdatedPropertyManager(state)

        try {
            val lastModifiedDate = Instant.parse(state.lastModifiedDate).toJavaInstant()
            if (updatedPropertyManager == null) {
                propertyOwnershipService.removePropertyManager(state.propertyId, lastModifiedDate)
            } else {
                propertyOwnershipService.updatePropertyManager(state.propertyId, updatedPropertyManager, lastModifiedDate)
            }
        } catch (ex: UpdateConflictException) {
            state.deleteJourney()
            throw ex
        }
        propertyUpdateEmailService.sendUpdateEmails(state.propertyId, listOf("The property manager responsible for the property"))
        // TODO PDJB-1991: when updatedPropertyManager is not null and differs from state.originalPropertyManager, send the new
        //  property manager email using AssociatedPartyNotificationService.sendCreationNotificationForPropertyManager once it is implemented
        // TODO PDJB-2171: when state.originalPropertyManager is not null and differs from updatedPropertyManager, send the removed
        //  property manager email
    }

    // The details form can still hold answers after the landlord has answered "No", so the yes or no answer decides
    private fun getUpdatedPropertyManager(state: UpdatePropertyManagerJourneyState): PropertyManagerDataModel? {
        val propertyManagerTask = state.propertyManagerTask
        if (propertyManagerTask.propertyManagerStep.outcome != YesOrNo.YES) return null

        val detailsFormModel = propertyManagerTask.propertyManagerDetailsStep.formModel
        return PropertyManagerDataModel(
            name = detailsFormModel.notNullValue(PropertyManagerDetailsFormModel::name),
            email = detailsFormModel.notNullValue(PropertyManagerDetailsFormModel::emailAddress),
        )
    }

    override fun resolveNextDestination(
        state: UpdatePropertyManagerJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent
class CompletePropertyManagerUpdateStep(
    stepConfig: CompletePropertyManagerUpdateStepConfig,
) : JourneyStep.InternalStep<Complete, UpdatePropertyManagerJourneyState>(stepConfig)
