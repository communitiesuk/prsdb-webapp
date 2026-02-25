package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.EpcLookupService

@JourneyFrameworkComponent
class EpcSupersededStepConfig(
    private val epcLookupService: EpcLookupService,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, EpcState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: EpcState) =
        mapOf(
            "certificateNumber" to state.searchedEpc?.latestCertificateNumberForThisProperty,
        )

    override fun chooseTemplate(state: EpcState): String = "forms/epcSupersededForm"

    override fun mode(state: EpcState): Complete? = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }

    override fun afterStepDataIsAdded(state: EpcState) {
        val certificateNumber =
            state.searchedEpc?.latestCertificateNumberForThisProperty
                ?: throw PrsdbWebException("latestCertificateNumberForThisProperty should not be null when searchedEpc is superseded")
        val latestEpc = epcLookupService.getEpcByCertificateNumber(certificateNumber)
        // reset CheckMatchedEpcInSession if changed EPC details, as the user will need to check the new EPC details
        if (latestEpc != state.searchedEpc) {
            state.checkMatchedEpcStep.formModelOrNull?.matchedEpcIsCorrect = null
        }
        state.searchedEpc = latestEpc
    }
}

@JourneyFrameworkComponent
final class EpcSupersededStep(
    stepConfig: EpcSupersededStepConfig,
) : RequestableStep<Complete, NoInputFormModel, EpcState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "epc-superseded"
    }
}
