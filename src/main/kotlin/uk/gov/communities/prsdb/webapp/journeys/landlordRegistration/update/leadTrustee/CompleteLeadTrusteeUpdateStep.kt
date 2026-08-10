package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.leadTrustee

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.InternalStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LeadTrusteeEmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LeadTrusteeNameFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LeadTrusteePhoneFormModel
import uk.gov.communities.prsdb.webapp.services.LandlordService

@JourneyFrameworkComponent
class CompleteLeadTrusteeUpdateStepConfig(
    private val landlordService: LandlordService,
) : AbstractInternalStepConfig<Complete, UpdateLeadTrusteeJourneyState>() {
    override fun mode(state: UpdateLeadTrusteeJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: UpdateLeadTrusteeJourneyState) {
        val task = state.leadTrusteeTask
        val name = task.leadTrusteeNameStep.formModel.notNullValue(LeadTrusteeNameFormModel::name)
        val dateOfBirth =
            task.leadTrusteeDobStep.formModel.toLocalDateOrNull()
                ?: throw NotNullFormModelValueIsNullException("Lead trustee date of birth is null when it was expected to have a value")
        val email = task.leadTrusteeEmailStep.formModel.notNullValue(LeadTrusteeEmailFormModel::emailAddress)
        val phone = task.leadTrusteePhoneStep.formModel.notNullValue(LeadTrusteePhoneFormModel::phoneNumber)
        val address = task.trusteeAddressTask.getAddress()

        landlordService.updateOrganisationLandlordLeadTrustee(
            name = name,
            dateOfBirth = dateOfBirth,
            email = email,
            phone = phone,
            addressDataModel = address,
        )
    }

    override fun resolveNextDestination(
        state: UpdateLeadTrusteeJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent
class CompleteLeadTrusteeUpdateStep(
    stepConfig: CompleteLeadTrusteeUpdateStepConfig,
) : InternalStep<Complete, UpdateLeadTrusteeJourneyState>(stepConfig)
