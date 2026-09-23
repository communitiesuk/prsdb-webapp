package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class NonRetryablePaymentFailedStepConfig :
    AbstractRequestableStepConfig<Nothing, NoInputFormModel, PropertyRegistrationJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    // TODO PDJB-1703: Replace this stub with the real non-retryable payment failed page (terminal, cannot retry).
    override fun getStepSpecificContent(state: PropertyRegistrationJourneyState): Map<String, Any?> =
        mapOf("todoComment" to "Payment failed - non-retryable (TODO PDJB-1703)")

    override fun chooseTemplate(state: PropertyRegistrationJourneyState) = "forms/todoNoButton"

    override fun mode(state: PropertyRegistrationJourneyState) = null
}

@JourneyFrameworkComponent
final class NonRetryablePaymentFailedStep(
    stepConfig: NonRetryablePaymentFailedStepConfig,
) : RequestableStep<Nothing, NoInputFormModel, PropertyRegistrationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "payment-failed-contact-us"
    }
}
