package uk.gov.communities.prsdb.webapp.exceptions

class PaymentCaptureOutcomeUnknownException(
    message: String,
    cause: Throwable,
) : PrsdbWebException(message, cause)
