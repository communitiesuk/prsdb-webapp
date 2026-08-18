package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.GET_NEW_EPC_URL
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcDetailState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class ProvideEpcLaterStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, EpcDetailState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: EpcDetailState) =
        mapOf(
            "getNewEpcUrl" to GET_NEW_EPC_URL,
            "submitButtonText" to "forms.buttons.saveAndContinue",
        )

    override fun chooseTemplate(state: EpcDetailState): String =
        state.isOccupied?.let { isOccupied ->
            if (isOccupied) "forms/provideEpcLaterOccupiedForm" else "forms/provideEpcLaterUnoccupiedForm"
        } ?: throw IllegalStateException("ProvideEpcLaterStep should not be reachable before isOccupied is set")

    override fun mode(state: EpcDetailState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class ProvideEpcLaterStep(
    stepConfig: ProvideEpcLaterStepConfig,
) : RequestableStep<Complete, NoInputFormModel, EpcDetailState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "provide-epc-later"
    }
}
