package uk.gov.communities.prsdb.webapp.exceptions

enum class SmsSendFailureReason {
    INVALID_REQUEST,
    TECHNICAL,
    DAILY_LIMIT,
    MINUTE_LIMIT,
    UNKNOWN,
}

class SmsSendException : PrsdbWebException {
    val reason: SmsSendFailureReason

    constructor(reason: SmsSendFailureReason, message: String) : super(message) {
        this.reason = reason
    }

    constructor(reason: SmsSendFailureReason, message: String, cause: Throwable) : super(message, cause) {
        this.reason = reason
    }
}
