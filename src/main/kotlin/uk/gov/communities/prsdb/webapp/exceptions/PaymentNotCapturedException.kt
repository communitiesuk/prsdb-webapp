package uk.gov.communities.prsdb.webapp.exceptions

import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus

class PaymentNotCapturedException(
    paymentId: String,
    val govUkPayStatus: PaymentStatus,
    captureFailure: Throwable,
) : PrsdbWebException("GOV.UK Pay reports payment $paymentId as $govUkPayStatus after it failed to capture", captureFailure)
