package uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.controllers.JoinPropertyController.Companion.JOIN_PROPERTY_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class NoMatchingPropertiesStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, JourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: JourneyState): Map<String, Any?> {
        // TODO: PDJB-274 - Get postcode and houseNameOrNumber from journey state when FindProperty step is implemented
        val findPropertyData = state.getStepData(FindPropertyStep.ROUTE_SEGMENT)
        val postcode = findPropertyData?.get("postcode")?.toString() ?: "the postcode"
        val houseNameOrNumber = findPropertyData?.get("houseNameOrNumber")?.toString() ?: "the house name or number"

        return mapOf(
            "postcode" to postcode,
            "houseNameOrNumber" to houseNameOrNumber,
            "searchAgainUrl" to "$JOIN_PROPERTY_ROUTE/${FindPropertyStep.ROUTE_SEGMENT}",
            "findByPrnUrl" to "$JOIN_PROPERTY_ROUTE/${FindPropertyByPrnStep.ROUTE_SEGMENT}",
        )
    }

    override fun chooseTemplate(state: JourneyState) = "forms/noMatchingPropertiesForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class NoMatchingPropertiesStep(
    stepConfig: NoMatchingPropertiesStepConfig,
) : RequestableStep<Complete, NoInputFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "no-matching-properties"
    }
}
