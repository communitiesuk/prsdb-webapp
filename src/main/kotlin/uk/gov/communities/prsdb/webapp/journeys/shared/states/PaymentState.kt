package uk.gov.communities.prsdb.webapp.journeys.shared.states

import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.payment.PaymentDependencies
import uk.gov.communities.prsdb.webapp.journeys.shared.payment.PaymentFailedStep
import uk.gov.communities.prsdb.webapp.journeys.shared.payment.PaymentOutcome
import uk.gov.communities.prsdb.webapp.journeys.shared.payment.PaymentReturnStep
import uk.gov.communities.prsdb.webapp.journeys.shared.payment.PaymentStatusCheckStep
import uk.gov.communities.prsdb.webapp.journeys.shared.payment.PaymentSummaryStep
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyPaymentQuote

interface PaymentState : JourneyState {
    val dependencies: PaymentDependencies

    val paymentSummaryStep: PaymentSummaryStep
    val paymentReturnStep: PaymentReturnStep
    val paymentStatusCheckStep: PaymentStatusCheckStep
    val paymentFailedStep: PaymentFailedStep

    var paymentQuote: PropertyPaymentQuote?
    var paymentReference: String?
    var paymentOutcome: PaymentOutcome?
}
