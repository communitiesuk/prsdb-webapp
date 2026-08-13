package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.organisationPhoneNumber

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.InternalStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgPhoneNumberFormModel
import uk.gov.communities.prsdb.webapp.services.LandlordService

@JourneyFrameworkComponent
class CompleteOrganisationPhoneNumberUpdateStepConfig(
    private val landlordService: LandlordService,
) : AbstractInternalStepConfig<Complete, UpdateOrganisationPhoneNumberJourneyState>() {
    override fun mode(state: UpdateOrganisationPhoneNumberJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: UpdateOrganisationPhoneNumberJourneyState) {
        val newPhoneNumber = state.orgPhoneNumberStep.formModel.notNullValue(OrgPhoneNumberFormModel::phoneNumber)
        landlordService.updateOrganisationLandlordPhoneNumber(newPhoneNumber)
    }

    override fun resolveNextDestination(
        state: UpdateOrganisationPhoneNumberJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent
class CompleteOrganisationPhoneNumberUpdateStep(
    stepConfig: CompleteOrganisationPhoneNumberUpdateStepConfig,
) : InternalStep<Complete, UpdateOrganisationPhoneNumberJourneyState>(stepConfig)
