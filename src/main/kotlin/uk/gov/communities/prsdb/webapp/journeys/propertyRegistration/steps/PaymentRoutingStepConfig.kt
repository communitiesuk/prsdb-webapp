package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PaymentOutcomeFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

enum class PaymentOutcome {
    SUCCESS,
    RETRYABLE_FAILURE,
    NON_RETRYABLE_FAILURE,
}

// TODO PDJB-993: Make this an internal step that derives the outcome from the real payment status
//  (e.g. a GOV.UK Pay callback / payment status lookup) instead of asking the user to pick one. For now it is a stub
//  page with a radio for each outcome so the downstream routing can be exercised.
@JourneyFrameworkComponent
class PaymentRoutingStepConfig :
    AbstractRequestableStepConfig<PaymentOutcome, PaymentOutcomeFormModel, PropertyRegistrationJourneyState>() {
    override val formModelClass = PaymentOutcomeFormModel::class

    override fun getStepSpecificContent(state: PropertyRegistrationJourneyState): Map<String, Any?> =
        mapOf(
            "fieldName" to "paymentOutcome",
            "fieldSetHeading" to "registerProperty.paymentOutcome.fieldSetHeading",
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(value = PaymentOutcome.SUCCESS),
                    RadiosButtonViewModel(value = PaymentOutcome.RETRYABLE_FAILURE),
                    RadiosButtonViewModel(value = PaymentOutcome.NON_RETRYABLE_FAILURE),
                ),
        )

    override fun chooseTemplate(state: PropertyRegistrationJourneyState) = "forms/todoWithRadios"

    override fun mode(state: PropertyRegistrationJourneyState) = getFormModelFromStateOrNull(state)?.paymentOutcome
}

@JourneyFrameworkComponent
final class PaymentRoutingStep(
    stepConfig: PaymentRoutingStepConfig,
) : RequestableStep<PaymentOutcome, PaymentOutcomeFormModel, PropertyRegistrationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "payment-routing"
    }
}
