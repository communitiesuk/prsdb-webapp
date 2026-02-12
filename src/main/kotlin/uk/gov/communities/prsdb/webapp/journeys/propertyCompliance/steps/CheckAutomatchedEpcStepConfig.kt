package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.EpcState
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckMatchedEpcFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider

@JourneyFrameworkComponent
class CheckAutomatchedEpcStepConfig(
    private val epcCertificateUrlProvider: EpcCertificateUrlProvider,
) : AbstractRequestableStepConfig<CheckMatchedEpcMode, CheckMatchedEpcFormModel, EpcState>() {
    override val formModelClass = CheckMatchedEpcFormModel::class

    override fun getStepSpecificContent(state: EpcState) =
        getReleventEpc(state)?.let { epcDetails ->
            mapOf(
                "title" to "propertyCompliance.title",
                "epcDetails" to epcDetails,
                "epcCertificateUrl" to epcDetails.certificateNumber.let { epcCertificateUrlProvider.getEpcCertificateUrl(it) },
                "radioOptions" to
                    listOf(
                        RadiosButtonViewModel(
                            value = true,
                            valueStr = "yes",
                            labelMsgKey = "forms.radios.option.yes.label",
                        ),
                        RadiosButtonViewModel(
                            value = false,
                            valueStr = "no",
                            labelMsgKey = "forms.checkMatchedEpc.radios.no.label",
                        ),
                    ),
            )
        } ?: emptyMap()

    override fun chooseTemplate(state: EpcState): String = "forms/checkMatchedEpcForm"

    override fun mode(state: EpcState): CheckMatchedEpcMode? =
        getFormModelFromStateOrNull(state)?.let {
            when (it.matchedEpcIsCorrect) {
                true -> CheckMatchedEpcMode.EPC_CORRECT
                false -> CheckMatchedEpcMode.EPC_INCORRECT
                null -> null
            }
        }

    override fun isSubClassInitialised(): Boolean = ::getReleventEpc.isInitialized

    private lateinit var getReleventEpc: (EpcState) -> EpcDataModel?

    fun usingEpc(getReleventEpc: EpcState.() -> EpcDataModel?): CheckAutomatchedEpcStepConfig {
        this.getReleventEpc = getReleventEpc
        return this
    }
}

@JourneyFrameworkComponent
final class CheckAutomatchedEpcStep(
    stepConfig: CheckAutomatchedEpcStepConfig,
) : RequestableStep<CheckMatchedEpcMode, CheckMatchedEpcFormModel, EpcState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "check-automatched-epc"
    }
}

enum class CheckMatchedEpcMode {
    EPC_CORRECT,
    EPC_INCORRECT,
}
