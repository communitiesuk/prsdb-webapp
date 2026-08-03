package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.phoneNumber

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.InternalStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PhoneNumberFormModel
import uk.gov.communities.prsdb.webapp.services.LandlordService

@JourneyFrameworkComponent
class CompletePhoneNumberUpdateStepConfig(
    private val landlordService: LandlordService,
) : AbstractInternalStepConfig<Complete, UpdatePhoneNumberJourneyState>() {
    override fun mode(state: UpdatePhoneNumberJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: UpdatePhoneNumberJourneyState) {
        val newPhoneNumber = state.phoneNumberStep.formModel.notNullValue(PhoneNumberFormModel::phoneNumber)
        landlordService.updateLandlordPhoneNumber(newPhoneNumber)
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
