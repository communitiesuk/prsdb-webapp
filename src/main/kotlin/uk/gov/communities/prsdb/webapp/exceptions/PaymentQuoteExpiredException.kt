package uk.gov.communities.prsdb.webapp.exceptions

class PaymentQuoteExpiredException(
    message: String,
) : PaidChangesRejectedException(message)
