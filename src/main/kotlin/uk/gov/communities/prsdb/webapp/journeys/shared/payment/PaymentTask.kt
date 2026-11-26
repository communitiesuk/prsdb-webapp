package uk.gov.communities.prsdb.webapp.journeys.shared.payment

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.shared.states.PaymentState
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyPaymentQuote

@JourneyFrameworkComponent
class PaymentTask(
    journeyStateService: JourneyStateService,
    override val paymentSummaryStep: PaymentSummaryStep,
    override val paymentReturnStep: PaymentReturnStep,
    override val paymentStatusCheckStep: PaymentStatusCheckStep,
    override val paymentFailedStep: PaymentFailedStep,
) : Task<PaymentState, PaymentDependencies>(journeyStateService),
    PaymentState {
    override var paymentQuote: PropertyPaymentQuote? by delegateProvider.nullableDelegate("paymentQuote")
    override var paymentReference: String? by delegateProvider.nullableDelegate("paymentReference")
    override var paymentOutcome: PaymentOutcome? by delegateProvider.nullableDelegate("paymentOutcome")

    override val taskState get() = this

    override fun makeSubJourney(state: PaymentState) =
        subJourney(state) {
            step(journey.paymentSummaryStep) {
                routeSegment(PaymentSummaryStep.ROUTE_SEGMENT)
                nextStep { journey.paymentReturnStep }
                savable()
            }
            step(journey.paymentReturnStep) {
                routeSegment(PaymentReturnStep.ROUTE_SEGMENT)
                parents { journey.paymentSummaryStep.isComplete() }
                nextStep { journey.paymentStatusCheckStep }
            }
            step(journey.paymentStatusCheckStep) {
                parents { journey.paymentReturnStep.isComplete() }
                nextDestination { mode ->
                    when (mode) {
                        PaymentOutcome.SUCCESS -> Destination(exitStep)
                        PaymentOutcome.FAILURE, PaymentOutcome.NOT_COMPLETED -> Destination(journey.paymentFailedStep)
                        PaymentOutcome.IN_PROGRESS -> Destination(journey.paymentReturnStep)
                        PaymentOutcome.QUOTE_EXPIRED -> Destination(journey.paymentSummaryStep)
                    }
                }
            }
            step(journey.paymentFailedStep) {
                routeSegment(PaymentFailedStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.paymentStatusCheckStep.hasOutcome(PaymentOutcome.FAILURE),
                        journey.paymentStatusCheckStep.hasOutcome(PaymentOutcome.NOT_COMPLETED),
                    )
                }
                noNextDestination()
                backUrl { null }
            }
            exitStep {
                parents { journey.paymentStatusCheckStep.hasOutcome(PaymentOutcome.SUCCESS) }
            }
        }
}
