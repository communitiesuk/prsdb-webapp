package uk.gov.communities.prsdb.webapp.journeys.shared.payment

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.PaymentState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class PaymentFailedStepConfig : AbstractRequestableStepConfig<Nothing, NoInputFormModel, PaymentState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: PaymentState): Map<String, Any?> {
        val contentKeys = state.dependencies.paymentContentKeys
        return mapOf(
            "tryAgainUrl" to Destination(state.paymentSummaryStep).toUrlStringOrNull(),
            "failedOutcomeText" to contentKeys.failedOutcomeText,
            "notCompletedOutcomeText" to contentKeys.notCompletedOutcomeText,
            "notCompletedWhatYouNeedToDoText" to contentKeys.notCompletedWhatYouNeedToDoText,
        )
    }

    override fun chooseTemplate(state: PaymentState) =
        if (state.paymentOutcome == PaymentOutcome.NOT_COMPLETED) {
            "forms/paymentNotCompletedForm"
        } else {
            "forms/paymentFailedForm"
        }

    override fun mode(state: PaymentState) = null
}

@JourneyFrameworkComponent
final class PaymentFailedStep(
    stepConfig: PaymentFailedStepConfig,
) : RequestableStep<Nothing, NoInputFormModel, PaymentState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "payment-failed"
    }
}
