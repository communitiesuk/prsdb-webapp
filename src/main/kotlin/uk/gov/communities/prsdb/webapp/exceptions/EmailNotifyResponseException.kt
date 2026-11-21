package uk.gov.communities.prsdb.webapp.exceptions

import kotlinx.serialization.SerializationException

class EmailNotifyResponseException(
    val reason: EmailSendFailureReason,
    message: String,
    cause: Throwable,
) : SerializationException(message, cause)
