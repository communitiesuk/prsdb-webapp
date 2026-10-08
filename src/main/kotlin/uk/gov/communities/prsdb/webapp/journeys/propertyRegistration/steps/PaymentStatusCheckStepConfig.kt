package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStatePersistenceService
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationDataModelFactory
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.services.PaymentService

enum class PaymentOutcome {
    SUCCESS,
    FAILURE,
    IN_PROGRESS,
}

@JourneyFrameworkComponent
class PaymentStatusCheckStepConfig(
    private val paymentService: PaymentService,
    private val propertyRegistrationDataModelFactory: PropertyRegistrationDataModelFactory,
    private val journeyStatePersistenceService: JourneyStatePersistenceService,
) : AbstractInternalStepConfig<PaymentOutcome, PropertyRegistrationJourneyState>() {
    override fun afterStepIsReached(state: PropertyRegistrationJourneyState) {
        val govUkPayStatus = paymentService.getPropertyRegistrationPaymentStatus(state.journeyId)
        if (govUkPayStatus.isCreated()) {
            state.paymentOutcome = PaymentOutcome.IN_PROGRESS
            return
        }

        // Only the saved copy is deleted here, because finalisePayment rolls this back if it fails but can't restore the session
        val finalisedStatus =
            paymentService.finalisePayment(
                govUkPayStatus.paymentId,
                govUkPayStatus.status,
                propertyRegistrationDataModelFactory.fromJourneyState(state),
            ) { journeyStatePersistenceService.deleteJourneyStateData(state.journeyId) }

        state.paymentOutcome =
            when (finalisedStatus) {
                PaymentStatus.SUCCEEDED -> PaymentOutcome.SUCCESS
                PaymentStatus.FAILED, PaymentStatus.CANCELLED -> PaymentOutcome.FAILURE
                PaymentStatus.CREATED, PaymentStatus.CAPTURABLE -> PaymentOutcome.IN_PROGRESS
            }
    }

    override fun mode(state: PropertyRegistrationJourneyState) = state.paymentOutcome

    override fun resolveNextDestination(
        state: PropertyRegistrationJourneyState,
        defaultDestination: Destination,
    ): Destination {
        if (state.paymentOutcome == PaymentOutcome.SUCCESS) {
            state.deleteJourney()
        }
        return defaultDestination
    }
}

@JourneyFrameworkComponent
final class PaymentStatusCheckStep(
    stepConfig: PaymentStatusCheckStepConfig,
) : JourneyStep.InternalStep<PaymentOutcome, PropertyRegistrationJourneyState>(stepConfig)
