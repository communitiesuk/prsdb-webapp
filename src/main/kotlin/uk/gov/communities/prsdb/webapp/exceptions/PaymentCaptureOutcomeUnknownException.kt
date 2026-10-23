package uk.gov.communities.prsdb.webapp.exceptions

class PaymentCaptureOutcomeUnknownException(
    paymentId: String,
    captureFailure: Throwable,
) : PrsdbWebException("Could not confirm whether GOV.UK Pay captured payment $paymentId", captureFailure)
