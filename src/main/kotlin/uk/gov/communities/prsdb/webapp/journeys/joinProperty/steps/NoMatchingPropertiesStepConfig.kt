package uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.states.JoinPropertyAddressSearchState
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class NoMatchingPropertiesStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, JoinPropertyAddressSearchState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: JoinPropertyAddressSearchState): Map<String, Any?> {
        val findPropertyData = state.getStepData(LookupAddressStep.ROUTE_SEGMENT)
        val postcode = findPropertyData?.get("postcode")?.toString() ?: "the postcode"
        val houseNameOrNumber = findPropertyData?.get("houseNameOrNumber")?.toString() ?: "the house name or number"

        return mapOf(
            "postcode" to postcode,
            "houseNameOrNumber" to houseNameOrNumber,
            "searchAgainUrl" to Destination(state.lookupAddressStep).toUrlStringOrNull(),
            "findByPrnUrl" to Destination(state.findPropertyByPrnStep).toUrlStringOrNull(),
        )
    }

    override fun chooseTemplate(state: JoinPropertyAddressSearchState) = "forms/noMatchingPropertiesForm"

    override fun mode(state: JoinPropertyAddressSearchState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class NoMatchingPropertiesStep(
    stepConfig: NoMatchingPropertiesStepConfig,
) : RequestableStep<Complete, NoInputFormModel, JoinPropertyAddressSearchState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "no-matching-properties"
    }
}
