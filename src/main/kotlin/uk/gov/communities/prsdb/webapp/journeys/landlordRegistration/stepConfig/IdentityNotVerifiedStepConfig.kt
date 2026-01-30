package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.IdentityState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class IdentityNotVerifiedStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, IdentityState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: IdentityState) = mapOf("submitButtonText" to "forms.buttons.continue")

    override fun chooseTemplate(state: IdentityState) = "forms/identityNotVerifiedForm"

    override fun mode(state: IdentityState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class IdentityNotVerifiedStep(
    stepConfig: IdentityNotVerifiedStepConfig,
) : RequestableStep<Complete, NoInputFormModel, IdentityState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "identity-not-verified"
    }
}
