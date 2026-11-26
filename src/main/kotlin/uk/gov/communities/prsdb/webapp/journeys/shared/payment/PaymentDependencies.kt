package uk.gov.communities.prsdb.webapp.journeys.shared.payment

import uk.gov.communities.prsdb.webapp.journeys.JourneyStep

interface PaymentDependencies {
    val paymentHandler: PaymentHandler
    val paymentSummaryAddressLines: List<String>
    val answersChangedSinceConfirmation: Boolean
    var isPropertyAlreadyRegistered: Boolean?
    val alreadyRegisteredStep: JourneyStep<*, *, *>
    val paymentContentKeys: PaymentContentKeys
}

data class PaymentContentKeys(
    val summaryInsetText: String,
    val failedOutcomeText: String,
    val notCompletedOutcomeText: String,
    val notCompletedWhatYouNeedToDoText: String,
)
