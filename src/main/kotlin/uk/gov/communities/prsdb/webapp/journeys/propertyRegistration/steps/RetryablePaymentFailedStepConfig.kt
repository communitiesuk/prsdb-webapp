package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class RetryablePaymentFailedStepConfig :
    AbstractRequestableStepConfig<Complete, NoInputFormModel, PropertyRegistrationJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    // TODO PDJB-1703: Replace this stub with the real retryable payment failed page. Continuing retries the payment.
    override fun getStepSpecificContent(state: PropertyRegistrationJourneyState) =
        mapOf("todoComment" to "Payment failed - retryable (TODO PDJB-1703)")

    override fun chooseTemplate(state: PropertyRegistrationJourneyState) = "forms/todo"

    override fun mode(state: PropertyRegistrationJourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class RetryablePaymentFailedStep(
    stepConfig: RetryablePaymentFailedStepConfig,
) : RequestableStep<Complete, NoInputFormModel, PropertyRegistrationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "payment-failed"
    }
}
