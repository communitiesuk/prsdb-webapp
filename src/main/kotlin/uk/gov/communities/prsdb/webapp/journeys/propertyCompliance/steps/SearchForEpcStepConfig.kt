package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.FIND_EPC_URL
import uk.gov.communities.prsdb.webapp.constants.GET_NEW_EPC_URL
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.EpcState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcLookupFormModel
import uk.gov.communities.prsdb.webapp.services.EpcLookupService

@JourneyFrameworkComponent
class SearchForEpcStepConfig(
    private val epcLookupService: EpcLookupService,
) : AbstractRequestableStepConfig<EpcSearchResult, EpcLookupFormModel, EpcState>() {
    override val formModelClass = EpcLookupFormModel::class

    override fun getStepSpecificContent(state: EpcState) =
        mapOf(
            "title" to "propertyCompliance.title",
            "fieldSetHeading" to "forms.epcLookup.fieldSetHeading",
            "fieldSetHint" to "forms.epcLookup.fieldSetHint",
            "findEpcUrl" to FIND_EPC_URL,
            "getNewEpcUrl" to GET_NEW_EPC_URL,
        )

    override fun chooseTemplate(state: EpcState): String = "forms/epcLookupForm"

    override fun mode(state: EpcState): EpcSearchResult? {
        val epc = state.searchedEpc
        return when {
            epc == null -> EpcSearchResult.NOT_FOUND
            epc.latestCertificateNumberForThisProperty == epc.certificateNumber -> EpcSearchResult.FOUND
            else -> EpcSearchResult.SUPERSEDED
        }
    }

    override fun afterStepDataIsAdded(state: EpcState) {
        val formModel =
            getFormModelFromState(state)
        val epc = epcLookupService.getEpcByCertificateNumber(formModel.certificateNumber)
        state.searchedEpc = epc
    }
}

@JourneyFrameworkComponent
final class SearchForEpcStep(
    stepConfig: SearchForEpcStepConfig,
) : RequestableStep<EpcSearchResult, EpcLookupFormModel, EpcState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "search-for-epc"
    }
}

enum class EpcSearchResult {
    FOUND,
    SUPERSEDED,
    NOT_FOUND,
}
