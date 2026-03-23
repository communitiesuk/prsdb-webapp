package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcExpiryCheckFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel.Companion.yesOrNoRadios

@JourneyFrameworkComponent("propertyRegistrationEpcExpiryCheckStepConfig")
class EpcExpiryCheckStepConfig : AbstractRequestableStepConfig<EpcExpiryCheckMode, EpcExpiryCheckFormModel, JourneyState>() {
    override val formModelClass = EpcExpiryCheckFormModel::class

    // TODO PDJB-665: Provide actual expiry date from EPC state
    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "title" to "forms.epcExpiryCheck.heading",
            "expiryDate" to null,
            "radioOptions" to yesOrNoRadios(),
        )

    override fun chooseTemplate(state: JourneyState) = "forms/epcExpiryCheckForm"

    override fun mode(state: JourneyState) =
        getFormModelFromStateOrNull(state)?.let {
            when (it.tenancyStartedBeforeExpiry) {
                true -> EpcExpiryCheckMode.IN_DATE
                false -> EpcExpiryCheckMode.NOT_IN_DATE
                null -> null
            }
        }
}

@JourneyFrameworkComponent("propertyRegistrationEpcExpiryCheckStep")
final class EpcExpiryCheckStep(
    stepConfig: EpcExpiryCheckStepConfig,
) : RequestableStep<EpcExpiryCheckMode, EpcExpiryCheckFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "epc-expiry-check"
    }
}

enum class EpcExpiryCheckMode {
    IN_DATE,
    NOT_IN_DATE,
}
