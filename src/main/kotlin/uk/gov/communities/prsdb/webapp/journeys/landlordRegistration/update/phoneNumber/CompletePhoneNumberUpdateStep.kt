package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.phoneNumber

import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.InternalStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.dataModels.updateModels.LandlordUpdateModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PhoneNumberFormModel
import uk.gov.communities.prsdb.webapp.services.LandlordService

@JourneyFrameworkComponent
class CompletePhoneNumberUpdateStepConfig(
    private val landlordService: LandlordService,
) : AbstractInternalStepConfig<Complete, UpdatePhoneNumberJourneyState>() {
    override fun mode(state: UpdatePhoneNumberJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: UpdatePhoneNumberJourneyState) {
        val baseUserId = SecurityContextHolder.getContext().authentication.name
        val newPhoneNumber = state.phoneNumberStep.formModel.notNullValue(PhoneNumberFormModel::phoneNumber)

        landlordService.updateLandlordForBaseUserId(
            baseUserId,
            LandlordUpdateModel(email = null, name = null, phoneNumber = newPhoneNumber, address = null, dateOfBirth = null),
        ) {}
    }

    override fun resolveNextDestination(
        state: UpdatePhoneNumberJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent
class CompletePhoneNumberUpdateStep(
    stepConfig: CompletePhoneNumberUpdateStepConfig,
) : InternalStep<Complete, UpdatePhoneNumberJourneyState>(stepConfig)
