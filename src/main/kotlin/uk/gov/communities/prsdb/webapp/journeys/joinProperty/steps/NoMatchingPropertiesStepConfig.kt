package uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.states.JoinPropertyAddressSearchState
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class NoMatchingPropertiesStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, JoinPropertyAddressSearchState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: JoinPropertyAddressSearchState): Map<String, Any?> {
        // TODO: PDJB-274 - Get postcode and houseNameOrNumber from journey state when FindProperty step is implemented
        val findPropertyData = state.getStepData(LookupAddressStep.ROUTE_SEGMENT)
        val postcode = findPropertyData?.get("postcode")?.toString() ?: "the postcode"
        val houseNameOrNumber = findPropertyData?.get("houseNameOrNumber")?.toString() ?: "the house name or number"

        return mapOf(
            "postcode" to postcode,
            "houseNameOrNumber" to houseNameOrNumber,
            "searchAgainUrl" to Destination.VisitableStep(state.lookupAddressStep, state.journeyId).toUrlStringOrNull(),
            // PRN search is in a different task, so build URL directly instead of using VisitableStep
            "findByPrnUrl" to JourneyStateService.urlWithJourneyState(FindPropertyByPrnStep.ROUTE_SEGMENT, state.journeyId),
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
