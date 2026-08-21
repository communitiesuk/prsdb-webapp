package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class ConfirmChangeToLettingAgentStepConfig :
    AbstractRequestableStepConfig<Complete, NoInputFormModel, PropertyRegistrationJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: PropertyRegistrationJourneyState) = emptyMap<String, Any?>()

    override fun chooseTemplate(state: PropertyRegistrationJourneyState) = "forms/whoProvidesChangeAreYouSureForm"

    override fun mode(state: PropertyRegistrationJourneyState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    override fun afterStepDataIsAdded(state: PropertyRegistrationJourneyState) {
        state.licensingTask.clearFormData()
        state.tenancyDetailsTask.clearFormData()
        state.gasSafetyTask.clearFormData()
        state.electricalSafetyTask.clearFormData()
        state.epcTask.clearFormData()
        state.confirmMissingComplianceStep.clearFormData()
    }
}

@JourneyFrameworkComponent
final class ConfirmChangeToLettingAgentStep(
    stepConfig: ConfirmChangeToLettingAgentStepConfig,
) : RequestableStep<Complete, NoInputFormModel, PropertyRegistrationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "confirm-change-to-letting-agent"
    }
}
