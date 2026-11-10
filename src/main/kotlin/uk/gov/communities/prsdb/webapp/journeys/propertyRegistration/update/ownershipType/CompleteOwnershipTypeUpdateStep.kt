package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.ownershipType

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.helpers.converters.OwnershipTypeConverter
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OwnershipTypeFormModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyUpdateEmailService

@JourneyFrameworkComponent
class CompleteOwnershipTypeUpdateStepConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
    private val propertyUpdateEmailService: PropertyUpdateEmailService,
) : AbstractInternalStepConfig<Complete, UpdateOwnershipTypeJourneyState>() {
    override fun mode(state: UpdateOwnershipTypeJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: UpdateOwnershipTypeJourneyState) {
        try {
            propertyOwnershipService.updateOwnershipType(
                state.propertyId,
                // TODO PDJB-1992: handle the ASSOCIATED_PARTIES flag here. Decide whether to make this
                //  step flag-aware or to replace this journey with a flag-on version, and make sure each
                //  version passes the right ownership type to updateOwnershipType: take it from the task
                //  when the flag is on, and keep converting the legacy answer with
                //  OwnershipTypeConverter.convert when it is off. Until that is done the legacy prefill
                //  means an ownership type with no legacy equivalent, such as TENANTS_IN_COMMON, is
                //  rewritten even when the user saves without changing their answer. Mark whatever
                //  flag-off code remains with TODO PDJB-2016, so it is deleted with the flag.
                OwnershipTypeConverter.convert(
                    state.ownershipTypeStep.formModel.notNullValue(OwnershipTypeFormModel::ownershipType),
                ),
                Instant.parse(state.lastModifiedDate).toJavaInstant(),
            )
        } catch (ex: UpdateConflictException) {
            state.deleteJourney()
            throw ex
        }
        sendUpdateConfirmationEmail(state)
    }

    private fun sendUpdateConfirmationEmail(state: UpdateOwnershipTypeJourneyState) {
        propertyUpdateEmailService.sendUpdateEmails(state.propertyId, listOf("The ownership type"))
    }

    override fun resolveNextDestination(
        state: UpdateOwnershipTypeJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent
class CompleteOwnershipTypeUpdateStep(
    stepConfig: CompleteOwnershipTypeUpdateStepConfig,
) : JourneyStep.InternalStep<Complete, UpdateOwnershipTypeJourneyState>(stepConfig)
