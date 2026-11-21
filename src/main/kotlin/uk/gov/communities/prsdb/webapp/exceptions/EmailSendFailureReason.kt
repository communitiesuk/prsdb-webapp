package uk.gov.communities.prsdb.webapp.exceptions

enum class EmailSendFailureReason {
    INVALID_REQUEST,
    TECHNICAL,
    DAILY_LIMIT,
    MINUTE_LIMIT,
    UNKNOWN,
}
