package uk.gov.communities.prsdb.webapp.testHelpers.builders

import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasSupplyStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSupplyFormModel

interface GasSafetyStateBuilder<SelfType : GasSafetyStateBuilder<SelfType>> {
    fun withSubmittedValue(
        key: String,
        value: FormModel,
    ): SelfType

    fun self(): SelfType

    fun withNoGasSupply(): SelfType {
        val hasGasSupplyFormModel =
            GasSupplyFormModel().apply {
                hasGasSupply = false
            }
        withSubmittedValue(HasGasSupplyStep.ROUTE_SEGMENT, hasGasSupplyFormModel)
        return self()
    }

    fun withGasSupply(): SelfType {
        val hasGasSupplyFormModel =
            GasSupplyFormModel().apply {
                hasGasSupply = true
            }
        withSubmittedValue(HasGasSupplyStep.ROUTE_SEGMENT, hasGasSupplyFormModel)
        return self()
    }
}
