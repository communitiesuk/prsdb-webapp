package uk.gov.communities.prsdb.webapp.exceptions

class NotifyAllowlistException : PersistentEmailSendException {
    constructor(reason: NotificationSendFailureReason, message: String, cause: Throwable) : super(reason, message, cause)
    constructor(reason: NotificationSendFailureReason, message: String) : super(reason, message)
}
