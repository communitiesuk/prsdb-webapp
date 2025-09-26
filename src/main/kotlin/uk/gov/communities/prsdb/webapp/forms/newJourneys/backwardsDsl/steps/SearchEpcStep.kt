package uk.gov.communities.prsdb.webapp.forms.newJourneys.backwardsDsl.steps

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.constants.FIND_EPC_URL
import uk.gov.communities.prsdb.webapp.constants.GET_NEW_EPC_URL
import uk.gov.communities.prsdb.webapp.forms.newJourneys.EpcSearchResult
import uk.gov.communities.prsdb.webapp.forms.newJourneys.shared.EpcJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcLookupFormModel
import uk.gov.communities.prsdb.webapp.services.EpcLookupService

@Scope("prototype")
@PrsdbWebComponent
class SearchEpcStep(
    private val epcLookupService: EpcLookupService,
) : BackwardsDslInitialisableStep<EpcSearchResult, EpcLookupFormModel, EpcJourneyState>() {
    override val formModelClazz = EpcLookupFormModel::class

    override fun getStepContent(state: EpcJourneyState) =
        mapOf(
            "title" to "propertyCompliance.title",
            "fieldSetHeading" to "forms.epcLookup.fieldSetHeading",
            "fieldSetHint" to "forms.epcLookup.fieldSetHint",
            "findEpcUrl" to FIND_EPC_URL,
            "getNewEpcUrl" to GET_NEW_EPC_URL,
        )

    override fun chooseTemplate(state: EpcJourneyState) = "forms/epcLookupForm"

    override fun mode(state: EpcJourneyState): EpcSearchResult? {
        val epc = state.searchedEpc
        return when {
            epc == null -> EpcSearchResult.NOT_FOUND
            epc.latestCertificateNumberForThisProperty == epc.certificateNumber -> EpcSearchResult.FOUND
            else -> EpcSearchResult.SUPERSEDED
        }
    }

    override fun afterUpdateJourneyState(state: EpcJourneyState) {
        super.afterUpdateJourneyState(state)
        val formModel = getFormModelFromState(state) ?: return

        val epc = epcLookupService.getEpcByCertificateNumber(formModel.certificateNumber)
        state.searchedEpc = epc
    }
}
