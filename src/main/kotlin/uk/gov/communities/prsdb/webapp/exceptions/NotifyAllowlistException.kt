package uk.gov.communities.prsdb.webapp.exceptions

class NotifyAllowlistException : PersistentEmailSendException {
    constructor(reason: EmailSendFailureReason, message: String, cause: Throwable) : super(reason, message, cause)
    constructor(reason: EmailSendFailureReason, message: String) : super(reason, message)
}
