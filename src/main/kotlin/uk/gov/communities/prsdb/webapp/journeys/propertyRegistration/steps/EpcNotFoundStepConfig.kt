package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent("propertyRegistrationEpcNotFoundStepConfig")
class EpcNotFoundStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, EpcState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: EpcState) =
        mapOf(
            "certificateNumber" to state.findYourEpcStep.formModelOrNull?.certificateNumber,
            "searchAgainUrl" to Destination.VisitableStep(state.findYourEpcStep, state.journeyId).toUrlStringOrNull(),
            "submitButtonText" to "forms.buttons.iDoNotHaveAnEpc",
        )

    override fun chooseTemplate(state: EpcState) = "forms/epcNotFoundPropertyRegistrationForm"

    override fun mode(state: EpcState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent("propertyRegistrationEpcNotFoundStep")
final class EpcNotFoundStep(
    stepConfig: EpcNotFoundStepConfig,
) : RequestableStep<Complete, NoInputFormModel, EpcState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "epc-not-found"
    }
}
