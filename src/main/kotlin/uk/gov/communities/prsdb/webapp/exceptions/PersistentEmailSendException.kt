package uk.gov.communities.prsdb.webapp.exceptions

open class PersistentEmailSendException : PrsdbWebException {
    val reason: NotificationSendFailureReason

    constructor(reason: NotificationSendFailureReason, message: String, cause: Throwable) : super(message, cause) {
        this.reason = reason
    }

    constructor(reason: NotificationSendFailureReason, message: String) : super(message) {
        this.reason = reason
    }
}
