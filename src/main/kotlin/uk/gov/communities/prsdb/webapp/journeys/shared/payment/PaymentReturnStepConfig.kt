package uk.gov.communities.prsdb.webapp.journeys.shared.payment

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.GovUkPayException
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.journeys.shared.states.PaymentState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.services.PaymentReferenceParameterService

@JourneyFrameworkComponent
class PaymentReturnStepConfig(
    private val paymentReferenceParameterService: PaymentReferenceParameterService,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, PaymentState>() {
    override val formModelClass = NoInputFormModel::class

    override val changesAnswers = false

    override fun getStepSpecificContent(state: PaymentState): Map<String, Any?> = emptyMap()

    override fun chooseTemplate(state: PaymentState) = "forms/paymentProcessingForm"

    override fun resolveChosenTemplate(
        state: PaymentState,
        templateName: String,
    ): Destination {
        markStepCompleteSoPaymentStatusCanBeChecked(state)
        return getPaymentOutcomeDestinationIfPaymentHasFinished(state) ?: super.resolveChosenTemplate(state, templateName)
    }

    private fun markStepCompleteSoPaymentStatusCanBeChecked(state: PaymentState) =
        state.addStepData(stepDataKey, NoInputFormModel().toPageData())

    private fun getPaymentOutcomeDestinationIfPaymentHasFinished(state: PaymentState): Destination? {
        val paymentStatusCheckStep = state.paymentStatusCheckStep
        return try {
            if (paymentStatusCheckStep.attemptToReachStep() && state.paymentOutcome != PaymentOutcome.IN_PROGRESS) {
                paymentStatusCheckStep.getNextDestination()
            } else {
                null
            }
        } catch (exception: GovUkPayException) {
            println("Failed to check payment status for journey ${state.journeyId}: ${exception.stackTraceToString()}")
            null
        }
    }

    override fun beforeAttemptingToReachStep(state: PaymentState): Boolean =
        paymentReferenceParameterService.getParameterOrNull() != null || state.paymentReference != null

    override fun afterStepIsReached(state: PaymentState) {
        val paymentReference = paymentReferenceParameterService.getParameterOrNull() ?: return
        state.paymentReference = paymentReference
    }

    override fun mode(state: PaymentState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class PaymentReturnStep(
    stepConfig: PaymentReturnStepConfig,
) : RequestableStep<Complete, NoInputFormModel, PaymentState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "payment-return"
    }
}
