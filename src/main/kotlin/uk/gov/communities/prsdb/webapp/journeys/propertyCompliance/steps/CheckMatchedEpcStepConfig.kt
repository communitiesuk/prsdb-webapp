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
class CheckMatchedEpcStepConfig(
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

    override fun mode(state: EpcState): CheckMatchedEpcMode? {
        val formModelOrNull = getFormModelFromStateOrNull(state)
        if (formModelOrNull?.matchedEpcIsCorrect == null) return null
        if (formModelOrNull.matchedEpcIsCorrect == false) return CheckMatchedEpcMode.EPC_INCORRECT

        val epcDetails = getReleventEpc(state)
        val energyRatingIsGood = epcDetails?.isEnergyRatingEOrBetter() ?: return null
        val epcExpired = epcDetails.isPastExpiryDate()

        if (epcExpired) {
            return if (energyRatingIsGood) {
                CheckMatchedEpcMode.EPC_EXPIRED_WITH_GOOD_ENERGY_RATING
            } else {
                CheckMatchedEpcMode.EPC_EXPIRED_WITH_LOW_ENERGY_RATING
            }
        }
        if (!energyRatingIsGood) return CheckMatchedEpcMode.EPC_IN_DATE_BUT_LOW_ENERGY_RATING
        return CheckMatchedEpcMode.EPC_COMPLIANT
    }

    override fun isSubClassInitialised(): Boolean = ::getReleventEpc.isInitialized

    private lateinit var getReleventEpc: (EpcState) -> EpcDataModel?

    fun usingEpc(getReleventEpc: EpcState.() -> EpcDataModel?): CheckMatchedEpcStepConfig {
        this.getReleventEpc = getReleventEpc
        return this
    }
}

@JourneyFrameworkComponent
final class CheckMatchedEpcStep(
    stepConfig: CheckMatchedEpcStepConfig,
) : RequestableStep<CheckMatchedEpcMode, CheckMatchedEpcFormModel, EpcState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "check-matched-epc"
        const val AUTOMATCHED_ROUTE_SEGMENT = "check-automatched-epc"
    }
}

enum class CheckMatchedEpcMode {
    EPC_COMPLIANT,
    EPC_EXPIRED_WITH_LOW_ENERGY_RATING,
    EPC_EXPIRED_WITH_GOOD_ENERGY_RATING,
    EPC_IN_DATE_BUT_LOW_ENERGY_RATING,
    EPC_INCORRECT,
}
