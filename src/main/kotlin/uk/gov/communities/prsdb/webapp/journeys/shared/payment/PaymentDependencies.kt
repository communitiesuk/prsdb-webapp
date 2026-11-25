package uk.gov.communities.prsdb.webapp.journeys.shared.payment

import uk.gov.communities.prsdb.webapp.journeys.JourneyStep

interface PaymentDependencies {
    val paymentHandler: PaymentHandler
    val paymentSummaryAddressLines: List<String>
    val answersChangedSinceConfirmation: Boolean
    var isPaymentAddressTaken: Boolean?
    val addressTakenStep: JourneyStep<*, *, *>
}
