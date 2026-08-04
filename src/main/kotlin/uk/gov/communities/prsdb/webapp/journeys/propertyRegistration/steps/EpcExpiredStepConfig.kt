package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.GET_NEW_EPC_URL
import uk.gov.communities.prsdb.webapp.constants.REGISTERED_ENERGY_EXEMPTION_GUIDE_URL
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcDetailState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent("propertyRegistrationEpcExpiredStepConfig")
class EpcExpiredStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, EpcDetailState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: EpcDetailState) =
        state.isOccupied?.let { isOccupied ->
            mapOf(
                "expiryDate" to state.getNotNullAcceptedEpc().expiryDateAsJavaLocalDate,
                "getNewEpcUrl" to GET_NEW_EPC_URL,
                "registeredEnergyExemptionGuideUrl" to REGISTERED_ENERGY_EXEMPTION_GUIDE_URL,
                "submitButtonText" to
                    if (isOccupied) "forms.buttons.continueAnyway" else "forms.buttons.continue",
            )
        } ?: throw IllegalStateException("EpcExpiredStep should not be reachable before isOccupied is set")

    override fun chooseTemplate(state: EpcDetailState): String =
        state.isOccupied?.let { isOccupied ->
            if (isOccupied) {
                "forms/epcExpiredForOccupiedPropertyRegistration"
            } else {
                "forms/epcExpiredForUnoccupiedPropertyRegistration"
            }
        } ?: throw IllegalStateException("EpcExpiredStep should not be reachable before isOccupied is set")

    override fun mode(state: EpcDetailState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent("propertyRegistrationEpcExpiredStep")
final class EpcExpiredStep(
    stepConfig: EpcExpiredStepConfig,
) : RequestableStep<Complete, NoInputFormModel, EpcDetailState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "epc-expired"
    }
}
