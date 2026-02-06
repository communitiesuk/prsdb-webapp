package uk.gov.communities.prsdb.webapp.journeys.example.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.FIND_EPC_URL
import uk.gov.communities.prsdb.webapp.constants.GET_NEW_EPC_URL
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.example.ExampleEpcJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcLookupFormModel
import uk.gov.communities.prsdb.webapp.services.EpcLookupService

@JourneyFrameworkComponent
class SearchEpcStepConfig(
    private val epcLookupService: EpcLookupService,
) : AbstractRequestableStepConfig<EpcSearchResult, EpcLookupFormModel, ExampleEpcJourneyState>() {
    override val formModelClass = EpcLookupFormModel::class

    override fun getStepSpecificContent(state: ExampleEpcJourneyState) =
        mapOf(
            "title" to "propertyCompliance.title",
            "fieldSetHeading" to "forms.epcLookup.fieldSetHeading",
            "fieldSetHint" to "forms.epcLookup.fieldSetHint",
            "findEpcUrl" to FIND_EPC_URL,
            "getNewEpcUrl" to GET_NEW_EPC_URL,
        )

    override fun chooseTemplate(state: ExampleEpcJourneyState): String = "forms/epcLookupForm"

    override fun mode(state: ExampleEpcJourneyState): EpcSearchResult? {
        val epc = state.searchedEpc
        return when {
            epc == null -> EpcSearchResult.NOT_FOUND
            epc.latestCertificateNumberForThisProperty == epc.certificateNumber -> EpcSearchResult.FOUND
            else -> EpcSearchResult.SUPERSEDED
        }
    }

    override fun afterStepDataIsAdded(state: ExampleEpcJourneyState) {
        val formModel = getFormModelFromStateOrNull(state) ?: return
        val epc = epcLookupService.getEpcByCertificateNumber(formModel.certificateNumber)
        state.searchedEpc = epc
    }
}

@JourneyFrameworkComponent
final class SearchEpcStep(
    stepConfig: SearchEpcStepConfig,
) : RequestableStep<EpcSearchResult, EpcLookupFormModel, ExampleEpcJourneyState>(stepConfig)
