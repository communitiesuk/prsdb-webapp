package uk.gov.communities.prsdb.webapp.exceptions

import kotlinx.serialization.SerializationException

class EmailNotifyResponseException(
    val reason: NotificationSendFailureReason,
    message: String,
    cause: Throwable,
) : SerializationException(message, cause)
