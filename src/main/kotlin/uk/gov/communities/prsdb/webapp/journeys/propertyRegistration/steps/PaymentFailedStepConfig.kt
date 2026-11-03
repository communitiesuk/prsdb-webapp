package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@JourneyFrameworkComponent
class PaymentFailedStepConfig : AbstractRequestableStepConfig<Nothing, NoInputFormModel, PropertyRegistrationJourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: PropertyRegistrationJourneyState): Map<String, Any?> =
        mapOf("tryAgainUrl" to Destination(state.paymentSummaryStep).toUrlStringOrNull())

    override fun chooseTemplate(state: PropertyRegistrationJourneyState) = "forms/paymentFailedForm"

    override fun mode(state: PropertyRegistrationJourneyState) = null
}

@JourneyFrameworkComponent
final class PaymentFailedStep(
    stepConfig: PaymentFailedStepConfig,
) : RequestableStep<Nothing, NoInputFormModel, PropertyRegistrationJourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "payment-failed"
    }
}
