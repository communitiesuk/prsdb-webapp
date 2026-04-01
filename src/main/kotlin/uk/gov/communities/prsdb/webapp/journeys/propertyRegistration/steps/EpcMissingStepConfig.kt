package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.GET_NEW_EPC_URL
import uk.gov.communities.prsdb.webapp.constants.PRS_EXEMPTION_GUIDE_URL
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent("propertyRegistrationEpcMissingStepConfig")
class EpcMissingStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, EpcState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: EpcState) =
        mapOf(
            "getNewEpcUrl" to GET_NEW_EPC_URL,
            "prsExemptionGuideUrl" to PRS_EXEMPTION_GUIDE_URL,
            "submitButtonText" to
                if (state.isOccupied == true) "forms.buttons.continueAnyway" else "forms.buttons.continue",
        )

    override fun chooseTemplate(state: EpcState): String =
        state.isOccupied?.let { isOccupied ->
            if (isOccupied) "forms/epcMissingForOccupiedProperty" else "forms/epcMissingForUnoccupiedProperty"
        } ?: throw IllegalStateException("EpcMissingStep should not be reachable before isOccupied is set")

    override fun mode(state: EpcState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent("propertyRegistrationEpcMissingStep")
final class EpcMissingStep(
    stepConfig: EpcMissingStepConfig,
) : RequestableStep<Complete, NoInputFormModel, EpcState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "epc-missing"
    }
}
