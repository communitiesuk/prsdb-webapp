package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.InternalStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState

enum class PaymentOutcome {
    SUCCESS,
    RETRYABLE_FAILURE,
    NON_RETRYABLE_FAILURE,
}

@JourneyFrameworkComponent
class PaymentRoutingStepConfig : AbstractInternalStepConfig<PaymentOutcome, PropertyRegistrationJourneyState>() {
    // TODO PDJB-993: Stub. Always routes to SUCCESS. Replace with the real payment outcome
    //  (e.g. from a GOV.UK Pay callback / payment status lookup) when payments are implemented.
    override fun mode(state: PropertyRegistrationJourneyState): PaymentOutcome = PaymentOutcome.SUCCESS
}

@JourneyFrameworkComponent
class PaymentRoutingStep(
    stepConfig: PaymentRoutingStepConfig,
) : InternalStep<PaymentOutcome, PropertyRegistrationJourneyState>(stepConfig)
