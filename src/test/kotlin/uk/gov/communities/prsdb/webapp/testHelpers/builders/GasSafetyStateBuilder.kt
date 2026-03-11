package uk.gov.communities.prsdb.webapp.testHelpers.builders

import uk.gov.communities.prsdb.webapp.constants.CONTINUE_BUTTON_ACTION_NAME
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckGasSafetyAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasCertStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasSupplyStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GasSupplyFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HasGasCertFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

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

    fun withGasSafetyTaskCompletedWithNoGasSupply(): SelfType {
        withNoGasSupply()
        withSubmittedValue(CheckGasSafetyAnswersStep.ROUTE_SEGMENT, NoInputFormModel())
        return self()
    }

    fun withGasCertificate(): SelfType {
        val hasGasCertificateFormModel =
            HasGasCertFormModel().apply {
                hasCert = true
                action = CONTINUE_BUTTON_ACTION_NAME
            }
        withSubmittedValue(HasGasCertStep.ROUTE_SEGMENT, hasGasCertificateFormModel)
        return self()
    }
}
