package uk.gov.communities.prsdb.webapp.exceptions

open class PersistentEmailSendException : PrsdbWebException {
    val reason: EmailSendFailureReason

    constructor(reason: EmailSendFailureReason, message: String, cause: Throwable) : super(message, cause) {
        this.reason = reason
    }

    constructor(reason: EmailSendFailureReason, message: String) : super(message) {
        this.reason = reason
    }
}
