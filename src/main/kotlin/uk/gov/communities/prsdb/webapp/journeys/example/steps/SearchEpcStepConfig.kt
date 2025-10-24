package uk.gov.communities.prsdb.webapp.journeys.example.steps

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.constants.FIND_EPC_URL
import uk.gov.communities.prsdb.webapp.constants.GET_NEW_EPC_URL
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.example.EpcJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcLookupFormModel
import uk.gov.communities.prsdb.webapp.services.EpcLookupService

@Scope("prototype")
@PrsdbWebComponent
class SearchEpcStepConfig(
    private val epcLookupService: EpcLookupService,
) : AbstractGenericStepConfig<EpcSearchResult, EpcLookupFormModel, EpcJourneyState>() {
    override val formModelClass = EpcLookupFormModel::class

    override fun getStepSpecificContent(state: EpcJourneyState) =
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

    override fun afterSubmitFormData(state: EpcJourneyState) {
        super.afterSubmitFormData(state)
        val formModel = getFormModelFromState(state) ?: return

        val epc = epcLookupService.getEpcByCertificateNumber(formModel.certificateNumber)
        state.searchedEpc = epc
    }
}

@Scope("prototype")
@PrsdbWebComponent
final class SearchEpcStep(
    stepConfig: SearchEpcStepConfig,
) : JourneyStep<EpcSearchResult, EpcLookupFormModel, EpcJourneyState>(stepConfig)
