package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.EpcState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcExpiryCheckFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel

@JourneyFrameworkComponent
class EpcExpiryCheckStepConfig : AbstractRequestableStepConfig<EpcExpiryCheckMode, EpcExpiryCheckFormModel, EpcState>() {
    override val formModelClass = EpcExpiryCheckFormModel::class

    override fun getStepSpecificContent(state: EpcState) =
        mapOf(
            "expiryDate" to (state.getNotNullAcceptedEpc().expiryDateAsJavaLocalDate),
            "radioOptions" to RadiosViewModel.yesOrNoRadios(),
        )

    override fun chooseTemplate(state: EpcState): String = "forms/epcExpiryCheckForm"

    override fun mode(state: EpcState): EpcExpiryCheckMode? {
        val tenancyStartedBeforeExpiry = getFormModelFromStateOrNull(state)?.tenancyStartedBeforeExpiry ?: return null
        if (!tenancyStartedBeforeExpiry) return EpcExpiryCheckMode.EPC_EXPIRED

        if (state.getNotNullAcceptedEpc().isEnergyRatingEOrBetter()) return EpcExpiryCheckMode.EPC_COMPLIANT
        return EpcExpiryCheckMode.EPC_LOW_ENERGY_RATING
    }
}

@JourneyFrameworkComponent
final class EpcExpiryCheckStep(
    stepConfig: EpcExpiryCheckStepConfig,
) : RequestableStep<EpcExpiryCheckMode, EpcExpiryCheckFormModel, EpcState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "epc-expiry-check"
    }
}

enum class EpcExpiryCheckMode {
    EPC_EXPIRED,
    EPC_COMPLIANT,
    EPC_LOW_ENERGY_RATING,
}
