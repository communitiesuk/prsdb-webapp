package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.update.organisationCharity

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.CharityRegulator
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.InternalStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.OrgCharityTask
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CharityRegisteredWithFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgCharityNumberEnglandAndWalesFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgCharityNumberNorthernIrelandFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgCharityNumberScotlandFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgIsRegisteredCharityFormModel
import uk.gov.communities.prsdb.webapp.services.LandlordService

@JourneyFrameworkComponent
class CompleteOrganisationCharityUpdateStepConfig(
    private val landlordService: LandlordService,
) : AbstractInternalStepConfig<Complete, UpdateOrganisationCharityJourneyState>() {
    override fun mode(state: UpdateOrganisationCharityJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: UpdateOrganisationCharityJourneyState) {
        val task = state.charityTask
        val isRegisteredCharity = task.orgIsRegisteredCharityStep.formModel.notNullValue(OrgIsRegisteredCharityFormModel::charity)
        val charityRegisteredWith = if (isRegisteredCharity) task.getCharityRegisteredWith() else null

        landlordService.updateOrganisationLandlordCharity(
            isRegisteredCharity = isRegisteredCharity,
            charityRegisteredWith = charityRegisteredWith,
            charityNumber = charityRegisteredWith?.let { task.getCharityNumber(it) },
        )
    }

    override fun resolveNextDestination(
        state: UpdateOrganisationCharityJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }

    private fun OrgCharityTask.getCharityRegisteredWith() =
        orgCharityRegisteredWithStep.formModel.notNullValue(CharityRegisteredWithFormModel::charityRegisteredWith)

    private fun OrgCharityTask.getCharityNumber(charityRegisteredWith: CharityRegulator) =
        when (charityRegisteredWith) {
            CharityRegulator.ENGLAND_AND_WALES ->
                orgCharityNumberEnglandAndWalesStep.formModel.notNullValue(OrgCharityNumberEnglandAndWalesFormModel::charityNumber)
            CharityRegulator.NORTHERN_IRELAND ->
                orgCharityNumberNorthernIrelandStep.formModel.notNullValue(OrgCharityNumberNorthernIrelandFormModel::charityNumber)
            CharityRegulator.SCOTLAND ->
                orgCharityNumberScotlandStep.formModel.notNullValue(OrgCharityNumberScotlandFormModel::charityNumber)
            CharityRegulator.NONE -> null
        }
}

@JourneyFrameworkComponent
class CompleteOrganisationCharityUpdateStep(
    stepConfig: CompleteOrganisationCharityUpdateStepConfig,
) : InternalStep<Complete, UpdateOrganisationCharityJourneyState>(stepConfig)
