package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.FIND_EPC_URL
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FindEpcByCertificateNumberFormModel
import uk.gov.communities.prsdb.webapp.services.EpcLookupService

@JourneyFrameworkComponent
class FindYourEpcStepConfig(
    private val epcLookupService: EpcLookupService,
) : AbstractRequestableStepConfig<FindYourEpcMode, FindEpcByCertificateNumberFormModel, EpcState>() {
    override val formModelClass = FindEpcByCertificateNumberFormModel::class

    override fun getStepSpecificContent(state: EpcState) =
        mapOf(
            "fieldSetHeading" to "propertyCompliance.epcTask.epcSearch.fieldSetHeading",
            "findEpcUrl" to FIND_EPC_URL,
        )

    override fun chooseTemplate(state: EpcState) = "forms/epcSearchForm"

    override fun mode(state: EpcState): FindYourEpcMode? {
        val epc = state.epcRetrievedByCertificateNumber
        return when {
            epc == null -> FindYourEpcMode.NOT_FOUND
            epc.isLatestCertificateForThisProperty() -> FindYourEpcMode.LATEST_EPC_FOUND
            else -> FindYourEpcMode.SUPERSEDED_EPC_FOUND
        }
    }

    override fun afterStepDataIsAdded(state: EpcState) {
        val formModel = getFormModelFromState(state)
        state.epcRetrievedByCertificateNumber = epcLookupService.getEpcByCertificateNumber(formModel.certificateNumber)
    }
}

@JourneyFrameworkComponent
final class FindYourEpcStep(
    stepConfig: FindYourEpcStepConfig,
) : RequestableStep<FindYourEpcMode, FindEpcByCertificateNumberFormModel, EpcState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "find-your-epc"
    }
}

enum class FindYourEpcMode {
    LATEST_EPC_FOUND,
    SUPERSEDED_EPC_FOUND,
    NOT_FOUND,
}
