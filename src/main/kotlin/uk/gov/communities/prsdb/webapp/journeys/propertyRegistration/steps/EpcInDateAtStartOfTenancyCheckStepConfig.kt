package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcExpiryCheckFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel.Companion.yesOrNoRadios

// TODO PDJB-665: Implement step - this was the EpcExpiryCheckStep so might need some renaming
@JourneyFrameworkComponent("propertyRegistrationEpcExpiryCheckStepConfig")
class EpcInDateAtStartOfTenancyCheckStepConfig :
    AbstractRequestableStepConfig<EpcInDateAtStartOfTenancyCheckMode, EpcExpiryCheckFormModel, JourneyState>() {
    override val formModelClass = EpcExpiryCheckFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "fieldSetHeading" to "propertyCompliance.epcTask.epcInDateAtStartOfTenancy.fieldSetHeading",
            "fieldName" to "tenancyStartedBeforeExpiry",
            "radioOptions" to yesOrNoRadios(),
        )

    override fun chooseTemplate(state: JourneyState) = "forms/todoWithRadios"

    override fun mode(state: JourneyState) =
        getFormModelFromStateOrNull(state)?.let {
            when (it.tenancyStartedBeforeExpiry) {
                true -> EpcInDateAtStartOfTenancyCheckMode.IN_DATE
                false -> EpcInDateAtStartOfTenancyCheckMode.NOT_IN_DATE
                null -> null
            }
        }
}

@JourneyFrameworkComponent("propertyRegistrationEpcExpiryCheckStep")
final class EpcInDateAtStartOfTenancyCheckStep(
    stepConfig: EpcInDateAtStartOfTenancyCheckStepConfig,
) : RequestableStep<EpcInDateAtStartOfTenancyCheckMode, EpcExpiryCheckFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "epc-in-date-at-start-of-tenancy-check"
    }
}

enum class EpcInDateAtStartOfTenancyCheckMode {
    IN_DATE,
    NOT_IN_DATE,
}
