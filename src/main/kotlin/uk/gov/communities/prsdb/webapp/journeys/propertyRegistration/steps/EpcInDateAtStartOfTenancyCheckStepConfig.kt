package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcInDateAtStartOfTenancyCheckFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

@JourneyFrameworkComponent("propertyRegistrationEpcInDateAtStartOfTenancyCheckStepConfig")
class EpcInDateAtStartOfTenancyCheckStepConfig :
    AbstractRequestableStepConfig<EpcInDateAtStartOfTenancyCheckMode, EpcInDateAtStartOfTenancyCheckFormModel, EpcState>() {
    override val formModelClass = EpcInDateAtStartOfTenancyCheckFormModel::class

    override fun getStepSpecificContent(state: EpcState): Map<String, Any?> {
        val expiryDate = state.getNotNullAcceptedEpc().expiryDateAsJavaLocalDate
        return mapOf(
            "expiryDate" to expiryDate,
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(
                        value = true,
                        valueStr = "yes",
                        labelMsgKey = "forms.radios.option.yes.label",
                        hintMsgKey = "propertyCompliance.epcTask.epcInDateAtStartOfTenancy.yes.hint",
                        hintMsgArg = expiryDate,
                    ),
                    RadiosButtonViewModel(
                        value = false,
                        valueStr = "no",
                        labelMsgKey = "forms.radios.option.no.label",
                    ),
                ),
        )
    }

    override fun chooseTemplate(state: EpcState) = "forms/epcInDateAtStartOfTenancyCheckForm"

    override fun mode(state: EpcState) =
        getFormModelFromStateOrNull(state)?.let {
            when (it.tenancyStartedBeforeExpiry) {
                true -> EpcInDateAtStartOfTenancyCheckMode.IN_DATE
                false -> EpcInDateAtStartOfTenancyCheckMode.NOT_IN_DATE
                null -> null
            }
        }
}

@JourneyFrameworkComponent("propertyRegistrationEpcInDateAtStartOfTenancyCheckStep")
final class EpcInDateAtStartOfTenancyCheckStep(
    stepConfig: EpcInDateAtStartOfTenancyCheckStepConfig,
) : RequestableStep<EpcInDateAtStartOfTenancyCheckMode, EpcInDateAtStartOfTenancyCheckFormModel, EpcState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "epc-in-date-at-start-of-tenancy-check"
    }
}

enum class EpcInDateAtStartOfTenancyCheckMode {
    IN_DATE,
    NOT_IN_DATE,
}
