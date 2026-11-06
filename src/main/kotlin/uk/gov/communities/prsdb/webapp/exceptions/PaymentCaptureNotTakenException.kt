package uk.gov.communities.prsdb.webapp.exceptions

import uk.gov.communities.prsdb.webapp.constants.enums.PaymentStatus

class PaymentCaptureNotTakenException(
    message: String,
    val govUkPayStatus: PaymentStatus,
    cause: Throwable,
) : PrsdbWebException(message, cause)
