package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.FIND_EPC_URL
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcLookupFormModel
import uk.gov.communities.prsdb.webapp.services.EpcLookupService

@JourneyFrameworkComponent
class EpcSearchStepConfig(
    private val epcLookupService: EpcLookupService,
) : AbstractRequestableStepConfig<EpcSearchMode, EpcLookupFormModel, EpcState>() {
    override val formModelClass = EpcLookupFormModel::class

    override fun getStepSpecificContent(state: EpcState) =
        mapOf(
            "fieldSetHeading" to "propertyCompliance.epcTask.epcSearch.fieldSetHeading",
            "findEpcUrl" to FIND_EPC_URL,
        )

    override fun chooseTemplate(state: EpcState) = "forms/epcSearchForm"

    override fun mode(state: EpcState): EpcSearchMode? {
        val epc = state.epcRetrievedByCertificateNumber
        return when {
            epc == null -> EpcSearchMode.NOT_FOUND
            epc.isLatestCertificateForThisProperty() -> EpcSearchMode.CURRENT_EPC_FOUND
            else -> EpcSearchMode.SUPERSEDED_EPC_FOUND
        }
    }

    override fun afterStepDataIsAdded(state: EpcState) {
        val formModel = getFormModelFromState(state)
        state.epcRetrievedByCertificateNumber = epcLookupService.getEpcByCertificateNumber(formModel.certificateNumber)
    }
}

@JourneyFrameworkComponent
final class EpcSearchStep(
    stepConfig: EpcSearchStepConfig,
) : RequestableStep<EpcSearchMode, EpcLookupFormModel, EpcState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "search-for-epc"
    }
}

enum class EpcSearchMode {
    CURRENT_EPC_FOUND,
    SUPERSEDED_EPC_FOUND,
    NOT_FOUND,
}
