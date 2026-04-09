package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.GET_NEW_EPC_URL
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class ProvideEpcLaterStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, EpcState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: EpcState) =
        mapOf(
            "getNewEpcUrl" to GET_NEW_EPC_URL,
            "submitButtonText" to "forms.buttons.saveAndContinue",
        )

    override fun chooseTemplate(state: EpcState): String =
        state.isOccupied?.let { isOccupied ->
            if (isOccupied) "forms/provideEpcLaterOccupiedForm" else "forms/provideEpcLaterUnoccupiedForm"
        } ?: throw IllegalStateException("ProvideEpcLaterStep should not be reachable before isOccupied is set")

    override fun mode(state: EpcState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class ProvideEpcLaterStep(
    stepConfig: ProvideEpcLaterStepConfig,
) : RequestableStep<Complete, NoInputFormModel, EpcState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "provide-epc-later"
    }
}
